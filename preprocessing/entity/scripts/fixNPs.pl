#!/usr/bin/perl

# usage: ./fixNPs.pl <pos file>

use WordNet::QueryData;

%wnlookup = ();

sub good_word {
    local ($x, $y, $r, @forms);
    $x = $_[0];
    $y = $_[1];
    $r = 0;

    if ($y =~ /^N/ || $y eq "CD") {
		$r = 1;
    } elsif ($y =~ /^JJ/ || $y eq "VBD" || $y eq "RB" || $y eq "UH" || $y eq "LS") {
		if (not exists $wnlookup{$x}) {
			@forms = $wn->validForms($x . "#n");
			if ($#forms >= 0) {
				$wnlookup{$x} = 1;
			} else {
				$wnlookup{$x} = 0;
			}
		}
		if ($wnlookup{$x} == 1) {
			$r = 1;
		}
    } elsif ($y eq "DT") {
		$x =~ tr/[A-Z]/[a-z]/;
		if ($x eq "another") {
			$r = 1;
		}
    }
	
    return $r;
}

$wn = WordNet::QueryData->new;

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	@b = split(/ /, $a[1]);

	print "$a[0]\t";

	for ($i = 0; $i <= $#b; $i++) {
		if ($i > 0) {
			print " ";
		}

		# for each NP chunk
		if ($b[$i] eq "[NP") {
			# grab the NP chunk
			for ($k = $i + 1; $k <= $#b; $k++) {
				if ($b[$k] eq "]") {
					last;
				}
			}

			# see if there are any words in the NP chunk in Wordnet
			for ($j = $k - 1; $j >= $i; $j--) {
				@c = split(/\//, $b[$j]);
				if (good_word($c[0], $c[1]) == 1) {
					last;
				}

				# also see if we're dealing with "rock climbing" or "break dancing"
				@d = split(/\//, $b[$j - 1]);
				$w = lc($d[0]) . " " . lc($c[0]);
				if ($w eq "rock climbing" || $w eq "break dancing") {
					$j--;
				}
			}

			# if we haven't found any good words in the NP chunk
			# don't change teh NP chunk.
			if ($j < $i) {
				$j = $k - 1;
			}

			# otherwise, break the NP chunk at the good word that we found
			@aout = ();
			push(@aout, "[NP");
			for ($l = $i + 1; $l <= $j; $l++) {
				push(@aout, $b[$l]);
			}
			push(@aout, "]");

			# if there are remaining words, try to figure out how to chunk them
			if ($l < $k) {
				$warn = 0;

				# break the remaining words up into word + POS
				@ax = ();
				while ($l < $k) {
					my @ay = split(/\//, $b[$l]);
					$ax[$#ax + 1] = \@ay;
					$l++;
				}

				# if we have a single RP, make a PRT chunk
				if ($#ax == 0 && $ax[0]->[1] eq "RP") {
					push(@aout, "[PRT");
					push(@aout, join("/", @{$ax[0]}));
					push(@aout, "]");
					goto done;
				}

				# if we have a single preposition, make an SBAR or PP chunk
				if ($#ax == 0 && $ax[0]->[1] eq "IN") {
					if (lc($ax[0]->[0]) eq "while") {
						push(@aout, "[SBAR");
					} else {
						push(@aout, "[PP");
					}
					push(@aout, join("/", @{$ax[0]}));
					push(@aout, "]");
					goto done;
				}

				# see if it's a bunch of verbs (and maybe conjunctions left)
				# if so, make a VP chunks out of them
				for ($m = 0; $m <= $#ax; $m++) {
					if ($ax[$m]->[1] =~ /^[^A-Z]/ || $ax[$m]->[1] eq "CC" || $ax[$m]->[1] =~ /^V/) {
					} elsif ($m < $#ax && lc($ax[$m + 0]->[0]) eq "rock" && lc($ax[$m + 1]->[0]) eq "climbing") {
					} elsif ($m < $#ax && lc($ax[$m + 0]->[0]) eq "break" && lc($ax[$m + 1]->[0]) eq "dancing") {
					} else {
						last;
					}
				}
				if ($m > $#ax) {
					for ($m = 0; $m <= $#ax; $m++) {
						if ($ax[$m]->[1] =~ /^V/) {
							push(@aout, "[VP");
							push(@aout, join("/", @{$ax[$m]}));
							push(@aout, "]");
						} elsif ($m < $#ax && lc($ax[$m + 0]->[0]) eq "rock" && lc($ax[$m + 1]->[0]) eq "climbing") {
							push(@aout, "[VP");
							push(@aout, join("/", @{$ax[$m + 0]}));
							push(@aout, join("/", @{$ax[$m + 1]}));
							push(@aout, "]");
							$m++;
						} elsif ($m < $#ax && lc($ax[$m + 0]->[0]) eq "break" && lc($ax[$m + 1]->[0]) eq "dancing") {
							push(@aout, "[VP");
							push(@aout, join("/", @{$ax[$m + 0]}));
							push(@aout, join("/", @{$ax[$m + 1]}));
							push(@aout, "]");
							$m++;
						} else {
							push(@aout, join("/", @{$ax[$m]}));
						}
					}
					goto done;
				}

				# can't make a chunk
				foreach (@ax) {
					push(@aout, join("/", @{$_}));
				}
				$warn = 1;

			  done:
				if ($warn == 0) {
					print STDERR join(" ", @aout), "\n";
				} else {
					print STDERR "CHECK: ", join(" ", @aout), "\n";
				}
			}
			print join(" ", @aout);

			$i = $k;
		} else {
			print "$b[$i]";
		}
	}
	print "\n";
}
close(file);
