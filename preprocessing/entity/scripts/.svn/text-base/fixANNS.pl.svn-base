#!/usr/bin/perl

# usage: ./fixANNSs.pl <np file> <pos file>

use WordNet::QueryData;

%wnlookup = ();
%wnvlookup = ();

sub good_word($$) {
    local ($x, $y, $z, $r, @forms);
    $x = $_[0];
    $y = $_[1];
    $r = 0;
	
    if ($y =~ /^N/ || $y eq "CD") {
		$r = 1;
    } elsif ($y =~ /^JJ/ || $y eq "VBD" || $y eq "RB" || $y eq "UH" || $y eq "LS") {
		if (not exists $wnlookup{$x}) {
			$z = $x;
			$z =~ s/-/_/g;
			@forms = $wn->validForms($z . "#n");
			if ($#forms >= 0) {
				$wnlookup{$x} = 1;
			} else {
				$wnlookup{$x} = 0;
			}
		}
		$r = $wnlookup{$x};
    } elsif ($y eq "DT") {
		$x =~ tr/[A-Z]/[a-z]/;
		if ($x eq "another") {
			$r = 1;
		}
    }
	
    $r;
}

sub good_verb($) {
    local ($x);

	$x = $_[0];
	$r = 0;
	if (not exists $wnvlookup{$x}) {
		$z = $x;
		$z =~ s/-/_/g;
		@forms = $wn->validForms($z . "#v");
		if ($#forms >= 0) {
			$wnvlookup{$x} = 1;
		} else {
			$wnvlookup{$x} = 0;
		}
	}
	$r = $wnvlookup{$x};
	
    $r;
}

$wn = WordNet::QueryData->new;

%plurals = ();
$plurals{"boys"} = "boy";
$plurals{"girls"} = "girl";

# get the determiners for each entity mention
%article = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$article{$ax[0]} = $ax[7];
}
close(file);

open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	@b = split(/ /, $a[1]);

	print "$a[0]\t";

	$m = 0;
	for ($i = 0; $i <= $#b; $i++) {
		if ($i > 0) {
			print " ";
		}

		if ($b[$i] eq "[NP") {
			$art = $article{"$a[0]#NP$m"};
			$m++;
			if (lc($art) ne "a/dt" && lc($art) ne "an/dt") {
				print $b[$i];
				next;
			}

			# if we're dealing with an NP that has "a" or "an" as a determiner,
			# grab the rest of the NP
			for ($k = $i; $k <= $#b; $k++) {
				if ($b[$k] eq "]") {
					last;
				}
			}
			# if the next chunk is an NP that start with a possessive, we're
			# dealing with something like "a team 's ball" - the determiner
			# head noun mis-match isn't actually a mis-match.
			if ($k < $#b - 2 && $b[$k + 1] eq "[NP" && $b[$k + 2] =~ /POS$/) {
				print $b[$i];
				next;
			}

			# if there's an conjunction in the NP, we may be dealing
			# with an improprely chunked NP ("a hat and two gloves"),
			# in which case, ignore it
			for ($j = $i + 1; $j < $k; $j++) {
				@c = split(/\//, $b[$j]);
				if ($c[1] eq "CC") {
					@d = split(/\//, $b[$j - 1]);
					@e = split(/\//, $b[$j + 1]);
					if ($e[1] ne "JJ" || $d[1] ne "JJ") { 
						last;
					}
				}
			}
			if ($j < $k) {
				print $b[$i];
				next;
			}

			# if it's not a plural noun (or is hoodie, which is
			# mistagged as a plural noun), ignore it.
			@e = split(/\//, $b[$k - 1]);
			if ($e[1] ne "NNS" || lc($e[0]) eq "hoodie") {
				print $b[$i];
				next;
			}

			# look for a new head noun - "hackey sacks" is a compound
			# so "hackey" isn't a valid new head noun in that case
			for ($j = $k - 2; $j > $i; $j--) {
				@e = split(/\//, $b[$j]);
				if (lc($e[0]) eq "hackey") {
					@e = split(/\//, $b[$j + 1]);
					if (lc($e[0]) eq "sacks") {
						next;
					}
				}
				if ($e[1] ne "NNS" && good_word($e[0], $e[1]) == 1) {
					last;
				}
			}
			if ($j == $i) {
				print $b[$i];
				next;
			}

			@aout = ();

			# create the new NP chunk
			push(@aout, "[NP");
			for ($l = $i + 1; $l <= $j; $l++) {
				@e = split(/\//, $b[$l]);
				push(@aout, $b[$l]);
			}

			# add other words to the NP chunk - 
			# depluralize the new head noun if needed/possible
			while ($l < $k) {
				@e = split(/\//, $b[$l]);
				if ($e[1] eq "JJ") {
				} elsif ($e[0] =~ /men$/) {
					while ($j < $l) {
						$j++;
						if ($j == $l) {
							$e[0] =~ s/men$/man/;
							push(@aout, "$e[0]/NN");
						} else {
							push(@aout, $b[$j]);
						}
					}

					$l++;
					last;
				} elsif ($e[0] =~ /shirts$/) {
					while ($j < $l) {
						$j++;
						if ($j == $l) {
							$e[0] =~ s/shirts$/shirt/;
							push(@aout, "$e[0]/NN");
						} else {
							push(@aout, $b[$j]);
						}
					}

					$l++;
					last;
				} elsif (exists $plurals{lc($e[0])}) {
					while ($j < $l) {
						$j++;
						if ($j == $l) {
							$e[0] = $plurals{lc($e[0])};
							push(@aout, "$e[0]/NN");
						} else {
							push(@aout, $b[$j]);
						}
					}

					$l++;
					last;
				} elsif (lc($e[0]) eq "arts" || lc($e[0]) eq "gi") {
					while ($j < $l) {
						$j++;
						push(@aout, $b[$j]);
					}

					$l++;
					last;
				} else {
					$l = $j + 1;
					last;
				}
				$l++;
			}
			push(@aout, "]");

			# chunk the rest of the old NP chunk - a number of plurals "nouns"
			# may actually be the verbs
			$warn = 0;
			if ($l < $k) {
				@ax = ();
				while ($l < $k) {
					my @ay = split(/\//, $b[$l]);
					$ax[$#ax + 1] = \@ay;
					$l++;
				}

				if ($#ax == 0 && (lc($ax[0]->[0]) eq "outdoors" || lc($ax[0]->[0]) eq "indoors")) {
					push(@aout, "[NP");
					push(@aout, join("/", @{$ax[0]}));
					push(@aout, "]");
					goto done;
				}

				if ($#ax == 0 && lc($ax[0]->[0]) =~ /s$/ && lc($ax[0]->[0]) ne "dogs") {
					push(@aout, "[VP");
					push(@aout, "$ax[0]->[0]/VBZ");
					push(@aout, "]");
					goto done;
				}

				if ($#ax > 0 && $ax[0]->[1] eq "VBG" && $ax[$#ax]->[1] =~ /^N/) {
					push(@aout, "[VP");
					push(@aout, join("/", @{$ax[0]}));
					push(@aout, "]");
					push(@aout, "[NP");
					for ($l = 1; $l <= $#ax; $l++) {
						push(@aout, join("/", @{$ax[$l]}));
					}
					push(@aout, "]");
					goto done;
				}

				if ($#ax > 0 && $ax[0]->[1] eq "JJ" && good_verb($ax[0]->[0]) == 1 && $ax[$#ax]->[1] =~ /^N/) {
					push(@aout, "[VP");
					push(@aout, join("/", "$ax[0]->[0]/VBZ"));
					push(@aout, "]");
					push(@aout, "[NP");
					for ($l = 1; $l <= $#ax; $l++) {
						push(@aout, join("/", @{$ax[$l]}));
					}
					push(@aout, "]");
					goto done;
				}

				if ($#ax > 0 && lc($ax[0]->[0]) =~ /s$/ && lc($ax[0]->[0]) ne "dogs" && $ax[$#ax]->[1] =~ /^N/) {
					push(@aout, "[VP");
					push(@aout, "$ax[0]->[0]/VBZ");
					push(@aout, "]");
					push(@aout, "[NP");
					for ($l = 1; $l <= $#ax; $l++) {
						push(@aout, join("/", @{$ax[$l]}));
					}
					push(@aout, "]");
					goto done;
				}

				if ($#ax > 0 && $ax[0]->[1] eq "CC" && $ax[$#ax]->[1] =~ /^N/) {
					push(@aout, join("/", @{$ax[0]}));
					push(@aout, "[NP");
					for ($l = 1; $l <= $#ax; $l++) {
						push(@aout, join("/", @{$ax[$l]}));
					}
					push(@aout, "]");
					goto done;
				}

				foreach (@ax) {
					push(@aout, join("/", @{$_}));
				}
				$warn = 1;
			}

		  done:
			if ($warn == 0) {
				print STDERR join(" ", @aout), "\n";
			} else {
				print STDERR "CHECK: ", join(" ", @aout), "\n";
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
