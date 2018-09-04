#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin/../../misc";
use parse;
use util;
use WordNet::QueryData;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

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

sub chunkNP($) {
	local ($x, @ax, @ay);

	$x = $_[0];

	@ax = split(/ /, $x);
	$x = pop(@ax);
	@ay = split(/\//, $x);
	if (@ay[1] eq "IN") {
		if ($#ax == -1) {
			if (lc($ay[0]) eq "while") {
				return "[SBAR $x ]";
			} else {
				return "[PP $x ]";
			}
		} else {
			if (lc($ay[0]) eq "while") {
				return "[NP " . join(" ", @ax) . " ] [SBAR $x ]";
			} else {
				return "[NP " . join(" ", @ax) . " ] [PP $x ]";
			}
		}
	} else {
		push(@ax, $x);
		return "[NP " . join(" ", @ax) . " ]";
	}
}

$wn = WordNet::QueryData->new;

%synsets = ();
$synsets{"<food>"} = {};
$synsets{"<food>"}->{"something"} = 1;
$synsets{"<food>"}->{"food#n#1"} = 1;
$synsets{"<food>"}->{"food#n#2"} = 1;
$synsets{"<vehicle>"} = {};
$synsets{"<vehicle>"}->{"something"} = 1;
$synsets{"<vehicle>"}->{"vehicle#n#1"} = 1;

%verb = ();
open(file, "$sdir/../data/verbing.txt");
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$v = shift(@ax);
	$verb{$v} = {};
	foreach (@ax) {
		$verb{$v}->{$_} = 1;
	}
}
close(file);

%head = ();
%fix = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/ /, $ax[8]);
	@az = split(/\//, $ay[$#ay]);
	$v = lc($az[0]);
	if ($az[1] =~ /^V/ && exists $verb{$v}) {
		$w = tokenize($ax[9]);
		if ($w ne "machine" && $w ne "machines" && not exists $verb{$v}->{$w}) {
			$state = 0;
			foreach $s (keys %{$verb{$v}}) {
				if (exists $synsets{$s}) {
					$state = 1;
					if (not exists $synsets{$s}->{$w}) {
						$r = 0;
						@forms = $wn->validForms($w . "#n");
						foreach (@forms) {
							@sense = $wn->querySense($_);
							foreach (@sense) {
								@aw = $wn->querySense($_, "syns");
								$r = $r | isSynset($aw[0], $synsets{$s});
							}
						}
						$synsets{$s}->{$w} = $r;
					}

					if ($synsets{$s}->{$w} == 1) {
						$state = 2;
						last;
					}
				}
			}

			if ($state == 0 || $state == 2) {
				$fix{$ax[0]} = $ay[$#ay];
				@az = split(/ /, $ax[9]);
				$head{$ax[0]} = $#az + 1;
			}
		}
	}
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

	$m = 0;
	print "$a[0]\t";
	for ($i = 0; $i < $n; $i++) {
		if ($i > 0) {
			print " ";
		}
		if ($s->[$i]->[0] eq "NP") {
			if (exists $fix{"$a[0]#NP$m"}) {
				@ax = split(/ /, $s->[$i]->[1]);
				$c = $head{"$a[0]#NP$m"};
				@head = ();
				while ($c > 0) {
					push(@head, pop(@ax));
					$c--;
				}
				$head = join(" ", reverse(@head));

				if ($#ax == 0) {
					$o = "[VP $ax[0] ] [NP $head ]";
				} else {
					@az = split(/\//, $ax[$#ax - 1]);
					if ($az[1] eq "CC") {
						if ($#ax > 1) {
							@az = split(/\//, $ax[$#ax - 2]);
							if ($az[1] =~ /^V/) {
								@verb = ();
								push(@verb, pop(@ax));
								push(@verb, pop(@ax));
								push(@verb, pop(@ax));
								$v = join(" ", reverse(@verb));
								if ($#ax == -1) {
									$o = "[VP $v ] [NP $head ]";
								} else {
									$x = join(" ", @ax);
									$o = chunkNP($x) . " [VP $v ] [NP $head ]";
								}
							} else {
								$v = pop(@ax);
								$c = pop(@ax);
								$x = join(" ", @ax);
								$o = chunkNP($x) . " $c [VP $v ] [NP $head ]";
							}
						} else {
							$o = "$ax[0] [ VP $ax[1] ] [NP $head ]";
						}
					} else {
						$v = pop(@ax);
						$x = join(" ", @ax);
						$o = chunkNP($x) . " [VP $v ] [NP $head ]";
					}
				}
				print STDERR "$o\n";
				print "$o";
			} else {
				print unparse($s->[$i]);
			}

			$m++;
		} else {
			print unparse($s->[$i]);
		}
	}
	print "\n";
}
close(file);
