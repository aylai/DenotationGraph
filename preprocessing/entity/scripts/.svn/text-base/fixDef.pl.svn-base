#!/usr/bin/perl

# usage: ./fixDef.pl <POS file> <groups> <global definites>

# 2 runners are ready for a/the track .
# A race car sits in /the pits
# A cowboy is thrown from a horse he was riding , while a judge stands by with his hand on a yellow flag .
# A big dog is biting a smaller dog on a leg
# A man wearing protective clothing is being bitten on an arm by a dog .
# corn on the cob
# at the door
# Inside quotes
# the starting blocks

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use WordNet::QueryData;

# horrible, horrible hack

sub color_word {
    my (@h);
	foreach ($wn->querySense($_[0] . "#n")) {
		@h = getHypes($_);
		if ($#h >= 0 && ($h[0] eq "chromatic_color#n#1" || $h[0] eq "achromatic_color#n#1")) {
			return 1;
		}
	}
    return 0;
}

$wn = WordNet::QueryData->new;

%plurals = ();

$plurals{"gear"} = 1;

# grab definite nouns
%global = ();
open(file, $ARGV[2]);
while (<file>) {
	chomp($_);
	$global{$_} = 1;
}
close(file);

%group = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);
	$group{$ai[0]} = {};
	for ($i = 1; $i <= $#ai; $i++) {
		$group{$ai[0]}->{$ai[1]} = 1;
	}
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);

	@a = split(/\t/, $_);
	@b = split(/ /, $a[1]);

	@clause = ();
	@head = ();
	@headpos = ();
	@article = ();
	@afterart = ();
	@afterartpos = ();
	@cc = ();
	@isof = ();
	$cl = 0;
	$n = -1;
	$in = 0;

	for ($i = 0; $i <= $#b; $i++) {
		if ($b[$i] eq "[NP") {
			$n++;
			$cc[$n] = 0;
			$isof[$n] = 0;
			$clause[$n] = $cl;
			@c = split(/\//, $b[$i + 1]);
		    if ($c[1] eq "DT") {
				$article[$n] = lc($c[0]);
			} else {
				$article[$n] = "";
			}
			@c = split(/\//, $b[$i + 2]);
			$afterart[$n] = lc($c[0]);
			$afterartpos[$n] = $c[1];
			for ($j = $i; $j <= $#b; $j++) {
				if ($b[$j] eq "]") {
					last;
				}
				@c = split(/\//, $b[$j]);
				$head[$n] = lc($c[0]);
				$headpos[$n] = $c[1];
				if ($c[1] eq "CC") {
					@d = split(/\//, $b[$j - 1]);
					@e = split(/\//, $b[$j + 1]);
					if ($d[1] ne "JJ" || $e[1] ne "JJ") {
						$cc[$n] = 1;
					}
				}
			}
			if ($b[$j + 1] eq "[PP" && lc($b[$j + 2]) eq "of/in") {
				$isof[$n] = 1;
			}
		}

		if ($b[$i] =~ /^\[/) {
			$in++;
		}
		if ($b[$i] eq "]") {
			$in--;
		}

		if ($b[$i] eq "[SBAR") {
#		if ($b[$i] eq "[SBAR" || ($in == 0 && $b[$i] eq ",/,") || ($in == 0 && lc($b[$i]) eq "and/cc")) {
			$cl++;
		}
	}

	print "$a[0]\t";
	$n = -1;
	$uppercase = 0;
	for ($i = 0; $i <= $#b; $i++) {
		if ($i > 0) {
			print " ";
		}
		if ($b[$i] eq "[NP") {
			$n++;
			if (not exists $plural{$head[$n]}) {
				$plural{$head[$n]} = color_word($head[$n]);
			}

			if ($isof[$n] == 1 || $cc[$n] == 1 || 
				$afterart[$n] eq "same" || $afterart[$n] eq "other" || $afterart[$n] eq "next" || 
				$afterart[$n] eq "number" || $afterart[$n] eq "first" || $afterart[$n] eq "second" || $afterart[$n] eq "third" || 
				$afterartpos[$n] eq "NNP") {
			} elsif (exists $group{$head[$n]} && $article[$n] eq "the") {
				for ($j = 0; $j < $n; $j++) {
					if ($clause[$j] < $clause[$n] && exists $group{$head[$n]}->{$head[$j]}) {
						last;
					}
				}

				if ($j == $n) {
					@c = split(/\//, $b[$i + 2]);
					if ($headpos[$n] eq "NNS" || $plural{$head[$n]}) {
						print "$b[$i]";
						if ($i == 0) {
							$uppercase = 1;
						}
					} elsif ($c[0] =~ /^[AEOIaeoi]/) {
						if ($i == 0) {
							print "$b[$i] An/DT";
						} else {
							print "$b[$i] an/DT";
						}
					} else {
						if ($i == 0) {
							print "$b[$i] A/DT";
						} else {
							print "$b[$i] a/DT";
						}
					}
					$i++;
					next;
				}
			} elsif ((not exists $global{$head[$n]}) && $article[$n] eq "the" && $headpos[$n] ne "CD") {
				@c = split(/\//, $b[$i + 2]);
				if ($headpos[$n] eq "NNS" || $plural{$head[$n]}) {
					print "$b[$i]";
					if ($i == 0) {
						$uppercase = 1;
					}
				} elsif ($c[0] =~ /^[AEOIaeoi]/) {
					if ($i == 0) {
						print "$b[$i] An/DT";
					} else {
						print "$b[$i] an/DT";
					}
				} else {
					if ($i == 0) {
						print "$b[$i] A/DT";
					} else {
						print "$b[$i] a/DT";
					}
				}
				$i++;
				next;
			}
		}

		if ($uppercase == 1) {
			$w = ucfirst($b[$i]);
			print "$w";
			$uppercase = 0;
		} else {
			print "$b[$i]";
		}
	}
	print "\n";
}
close(file);
