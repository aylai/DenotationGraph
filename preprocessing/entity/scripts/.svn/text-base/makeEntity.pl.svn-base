#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin/../../misc";
use parse;
use util;
use WordNet::QueryData;

sub isSynset($$) {
	local ($x, $y, $r, @hypes);

	$x = $_[0];
	$y = $_[1];
	$r = 0;

	if (not exists $y->{$x}) {
		@hypes = getHypes($x);
		foreach (@hypes) {
			$r = $r | isSynset($_, $y);
		}
		$y->{$x} = $r;
	}
	return $y->{$x};
}

$wn = WordNet::QueryData->new;

%X = ();
%Y = ();
%Xsyn = ();
%Ysyn = ();

$X{"table"} = 0;
$X{"tables"} = 0;

$X{"container#n#1"} = 1;
$X{"containerful#n#1"} = 1;

$X{"glass"} = 1;
$X{"glasses"} = 1;
$X{"pitcher"} = 1;
$X{"pitchers"} = 1;
$Xsyn{"glass"} = "glass#n#2";
$Xsyn{"glasses"} = "glass#n#2";
$Xsyn{"pitcher"} = "pitcher#n#2";
$Xsyn{"pitchers"} = "pitcher#n#2";

$Y{"liquid#n#1"} = 1;
$Y{"beverage#n#1"} = 1;

$X{"group#n#1"} = 2;
$Y{"person#n#1"} = 2;

$Y{"people"} = 2;
$Ysyn{"people"} = "person#n#1";

$X{"body"} = 4;
$Xsyn{"body"} = "body_of_water#n#1";

$Y{"water"} = 5;
$Ysyn{"water"} = "water#n#1";

$X{"sort"} = -1;
$X{"type"} = -1;
$X{"kind"} = -1;
$X{"variety"} = -1;
$Xsyn{"sort"} = "kind#n#1";
$Xsyn{"type"} = "type#n#1";
$Xsyn{"kind"} = "kind#n#1";
$Xsyn{"variety"} = "kind#n#1";

%head = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$head{$ax[0]} = tokenize($ax[9]);
}
close(file);

open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	@b = split(/ /, $a[1]);

	$s = ();
	$i = 0;
	$n = 0;
	$p = 0;
	while ($i <= $#b) {
		($x, $i, $p) = parse(\@b, $i, $p);
		$s->[$n] = $x;
		$n++;
	}

	print "$a[0]\t";

	$m = 0;
	for ($i = 0; $i < $n; $i++) {
		if ($i > 0) {
			print " ";
		}

		if ($s->[$i + 0]->[0] eq "NP" && $i < ($n - 2)) {
			$x = $head{"$a[0]#NP$m"};
			$m++;
			$y = $head{"$a[0]#NP$m"};
			if ($s->[$i + 1]->[0] eq "PP" &&
				lc($s->[$i + 1]->[1]) eq "of/in" &&
				$s->[$i + 2]->[0] eq "NP") {

				if (not exists $X{$x}) {
					$r = 0;
					@forms = $wn->validForms($x . "#n");
					foreach (@forms) {
						@sense = $wn->querySense($_);
#						foreach (@sense) {
#							@aw = $wn->querySense($_, "syns");
#					if ($#forms >= 0) {
#						@sense = $wn->querySense($forms[0]);
						if ($#sense >= 0) {
							@aw = $wn->querySense($sense[0], "syns");
							$z = isSynset($aw[0], \%X);
							if ($z > 0) {
								$r = $r | $z;
								$Xsyn{$x} = $aw[0];
							}
						}
					}
					$X{$x} = $r;
				}

				if (not exists $Y{$y}) {
					$r = 0;
					@forms = $wn->validForms($y . "#n");
					foreach (@forms) {
						@sense = $wn->querySense($_);
#						foreach (@sense) {
#							@aw = $wn->querySense($_, "syns");
#						if ($#forms >= 0) {
#							@sense = $wn->querySense($forms[0]);
						if ($#sense >= 0) {
							@aw = $wn->querySense($sense[0], "syns");
							$z = isSynset($aw[0], \%Y);
							if ($z > 0) {
								$r = $r | $z;
								$Ysyn{$y} = $aw[0];
							}
						}
					}
					$Y{$y} = $r;
				}

				if ($X{$x} == -1 || (($X{$x} & $Y{$y}) != 0)) {
					print STDERR "+$s->[$i + 0]->[1] $s->[$i + 1]->[1] $s->[$i + 2]->[1]\n";
					$xsyn = "";
					$ysyn = "";
					if (exists $Xsyn{$x}) {
						$xsyn = "/$Xsyn{$x}";
					}
					if (exists $Ysyn{$y}) {
						$ysyn = "/$Ysyn{$y}";
					}
					print "[EN [NP$xsyn $s->[$i + 0]->[1] ] " . unparse($s->[$i + 1]) . " [NP$ysyn $s->[$i + 2]->[1] ] ]";
					$i = $i + 2;
					$m++;
				} else {
					print STDERR "-$s->[$i + 0]->[1] $s->[$i + 1]->[1] $s->[$i + 2]->[1]\n";
					print "[EN " . unparse($s->[$i]) . " ]";
				}
			} else {
				print "[EN " . unparse($s->[$i]) . " ]";
			}
		} elsif ($s->[$i]->[0] eq "NP") {
			print "[EN " . unparse($s->[$i]) . " ]";
		} else {
			print unparse($s->[$i]);
		}
	}
	print "\n";
}
close(file);
