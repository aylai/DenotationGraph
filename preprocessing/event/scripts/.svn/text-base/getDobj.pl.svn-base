#!/usr/bin/perl

# ./getDobj.pl <coref file> <NP file> <CONLL file>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use parse;

%pos = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/ /, $ax[7]);
	if ($#ay == 0) {
		@az = split(/\//, $ay[0]);
		if ($az[1] eq "POS") {
			$pos{$ax[0]} = 1;
		}
	}
}
close(file);

%dobj = ();
%iobj = ();
%pass = ();
open(file, $ARGV[2]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);

	if ($#ax == 4) {
		if ($ax[4] eq "dobj") {
			@np = split(/\#/, $ax[1]);
			@vp = split(/\#/, $ax[3]);
			$dobj{"$ax[0]#$vp[0]"} = "$ax[0]#$np[0]";
		} elsif ($ax[4] eq "iobj") {
			@np = split(/\#/, $ax[1]);
			@vp = split(/\#/, $ax[3]);
			$iobj{"$ax[0]#$vp[0]"} = "$ax[0]#$np[0]";
		} elsif ($ax[4] eq "nsubjpass") {
			@np = split(/\#/, $ax[1]);
			@vp = split(/\#/, $ax[3]);
			$pass{"$ax[0]#$vp[0]"} = "$ax[0]#$np[0]";
		}
	}
}
close(file);

%vps = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/ /, $ax[1]);

	$s = ();
	$i = 0;
	$n = 0;
	$p = 0;
	while ($i <= $#ay) {
		($x, $i, $p) = parse(\@ay, $i, $p);
		$s->[$n] = $x;
		$n++;
	}

	$np = 0;
	$vp = 0;
	for ($i = 0; $i < $n; $i++) {
		@az = split(/\//, $s->[$i]->[0]);
		if ($az[0] eq "VP") {
			$j = $i + 1;
			$pp = "";
			if ($j < $n) {
				@az = split(/\//, $s->[$i + 1]->[0]);
				if ($az[0] eq "PP" || $az[0] eq "PRT" || $az[0] eq "ADVP" || $az[0] eq "SBAR") {
					$pp = tokenize(flatten($s->[$i + 1]->[1]));
					$j++;
				}
			}

			$k = $np;
			if ($j < $n) {
				@az = split(/\//, $s->[$j]->[0]);
				if ($az[0] eq "EN") {
					while (($j + 1) < $n) {
						@az = split(/\//, $s->[$j + 1]->[0]);
						if ($az[0] eq "EN" && exists $pos{"$ax[0]#NP" . ($k + 1)}) {
							$j++;
							$k++;
						} else {
							last;
						}
					}
					if ($pp eq "") {
						if ($iobj{"$ax[0]#VP$vp"} eq "$ax[0]#NP$k") {
							$j1 = $j + 1;
							$k1 = $k + 1;
							@az = split(/\//, $s->[$j1]->[0]);
							if ($az[0] eq "EN") {
								while (($j1 + 1) < $n) {
									@az = split(/\//, $s->[$j1 + 1]->[0]);
									if ($az[0] eq "EN" && exists $pos{"$ax[0]#NP" . ($k1 + 1)}) {
										$j1++;
										$k1++;
									} else {
										last;
									}
								}

								if ($dobj{"$ax[0]#VP$vp"} eq "$ax[0]#NP$k1") {
									print "$ax[0]#VP$vp\t\t$ax[0]#NP$k1\n";
									$vp{"$ax[0]#VP$vp"} = 1;
								} else {
									print "$ax[0]#VP$vp\t\t$ax[0]#NP$k\n";
									$vp{"$ax[0]#VP$vp"} = 1;
								}
							} else {
								print "$ax[0]#VP$vp\t\t$ax[0]#NP$k\n";
								$vp{"$ax[0]#VP$vp"} = 1;
							}
						} else {
							print "$ax[0]#VP$vp\t\t$ax[0]#NP$k\n";
							$vp{"$ax[0]#VP$vp"} = 1;
						}
					} elsif ($dobj{"$ax[0]#VP$vp"} eq "$ax[0]#NP$k") {
						print "$ax[0]#VP$vp\t$pp\t$ax[0]#NP$k\n";
						$vp{"$ax[0]#VP$vp"} = 1;
					}
				}
			}
			$vp++;
		} elsif ($az[0] eq "EN") {
			$np++;
		}
	}
}
close(file);

foreach (keys %pass) {
	if (not exists $vp{$_}) {
#		print "$_\t$pass{$_}\n";
	}
}
