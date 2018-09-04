#!/usr/bin/perl

use strict;
use warnings;

# rechunk "while", "and", and "or"

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	my @ax = split(/ /, $_);

	# @chunk - type of chunks
	# @pos - index of chunks
	# $n - number of chunks
	my @chunk = ();
	my @pos = ();
	my $n = 0;
	my $state = 0;
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] =~ /^\[/) {
			$chunk[$n] = $ax[$i];
			$pos[$n] = $i;
			$n++;
			$state = 1;
		} elsif ($ax[$i] =~ /^\]/) {
			$state = 0;
		} elsif ($state == 0) {
			$chunk[$n] = "";
			$pos[$n] = $i;
			$n++;
			$state = 1;
		}
	}
	$pos[$n] = $#ax + 1;

	# @r - output caption
	my @r = ();
LOOP:
	for (my $i = 0; $i < $n; $i++) {
		# search for a "while"
		for (my $j = 0; $j < ($pos[$i + 1] - $pos[$i]); $j++) {
			my @ay = split(/\//, $ax[$pos[$i] + $j]);
			if (lc($ay[0]) eq "while") {
				# if the while is not in an SBAR (or outside of a chunk), we're going to look into rechunking it as an SBAR
				if ($chunk[$i] ne "[SBAR" && $chunk[$i] ne "") {
					# [XX while ] -> [SBAR while ]
					if (($pos[$i + 1] - $pos[$i]) == 3) {
						push(@r, "[SBAR");
						push(@r, $ax[$pos[$i] + $j]);
						push(@r, "]");
						next LOOP;
					# [XX while ... ] -> [SBAR while ] ([VP verb ]) [XX ... ]
					} elsif ($j == 1) {
						push(@r, "[SBAR");
						push(@r, $ax[$pos[$i] + $j]);
						push(@r, "]");
						$j++;
						my @az = split(/\//, $ax[$pos[$i] + $j]);
						if ($az[1] =~ /^V/) {
							push(@r, "[VP");
							push(@r, $ax[$pos[$i] + $j]);
							push(@r, "]");
							$j++;
						}
						if ($j < ($pos[$i + 1] - $pos[$i] - 1)) {
							push(@r, $ax[$pos[$i]]);
							for (; $j < ($pos[$i + 1] - $pos[$i]); $j++) {
								push(@r, $ax[$pos[$i] + $j]);
							}
						}
						next LOOP;
					# [PP ... while ] -> [ADVP ... ] [SBAR while ]
					# [XX ... while ] -> [XX ... ] [SBAR while ]
					} elsif ($j == ($pos[$i + 1] - $pos[$i] - 2)) {
						if ($ax[$pos[$i]] eq "[PP") {
							push(@r, "[ADVP");
						} else {
							push(@r, $ax[$pos[$i]]);
						}
						for (my $k = 1; $k < $j; $k++) {
							push(@r, $ax[$pos[$i] + $k]);
						}
						push(@r, "]");
						push(@r, "[SBAR");
						push(@r, $ax[$pos[$i] + $j]);
						push(@r, "]");
						next LOOP;
					# [XX RB while ... ] -> [ADVP RB ] [SBAR while ] ([VP verb]) [XX ... ]
					# [XX ... while ... ] -> [NP ... ] [SBAR while ] ([VP verb]) [XX ... ]
					} else {
						my @az = split(/\//, $ax[$pos[$i] + 1]);
						if ($j == 2 && $az[1] =~ /^R/) {
							push(@r, "[ADVP");
							push(@r, $ax[$pos[$i] + 1]);
							push(@r, "]");
						} else {
							push(@r, "[NP");
							for (my $k = 1; $k < $j; $k++) {
								push(@r, $ax[$pos[$i] + $k]);
							}
							push(@r, "]");
						}
						push(@r, "[SBAR");
						push(@r, $ax[$pos[$i] + $j]);
						push(@r, "]");
						$j++;
						@az = split(/\//, $ax[$pos[$i] + $j]);
						if ($az[1] =~ /^V/) {
							push(@r, "[VP");
							push(@r, $ax[$pos[$i] + $j]);
							push(@r, "]");
							$j++;
						}
						if ($j < ($pos[$i + 1] - $pos[$i] - 1)) {
							push(@r, $ax[$pos[$i]]);
							for (; $j < ($pos[$i + 1] - $pos[$i]); $j++) {
								push(@r, $ax[$pos[$i] + $j]);
							}
						}
						next LOOP;
					}
				}
			}
		}

		# [NP CC ] -> CC
		# [NP ... CC ] -> [NP ... ] CC
		# [NP ... verb CC ] -> [NP ... ] [VP verb ] CC
		if ($chunk[$i] eq "[NP") {
			my @ay = split(/\//, $ax[$pos[$i + 1] - 2]);
			if (lc($ay[0]) eq "or" || lc($ay[0]) eq "and") {
				if (($pos[$i + 1] - $pos[$i]) > 3) {
					my @az = split(/\//, $ax[$pos[$i + 1] - 3]);
					if ($az[1] =~ /^V/) {
						my $j = $pos[$i + 1] - 3;
						for (; $j >= $pos[$i]; $j--) {
							my @aq = split(/\//, $ax[$j]);
							if ($#aq == 1 && $aq[1] =~ /^[^A-Z]/) {
								last;
							}
						}

						if ($j > $pos[$i]) {
							for (my $k = $pos[$i]; $k < $j; $k++) {
								push(@r, $ax[$k]);
							}
							push(@r, "]");
							push(@r, $ax[$j]);
							push(@r, "[VP");
							$j++;
							while ($j < $pos[$i + 1] - 2) {
								push(@r, $ax[$j]);
								$j++;
							}
						} else {
							push(@r, "[VP");
							for (my $j = $pos[$i] + 1; $j < $pos[$i + 1] - 2; $j++) {
								push(@r, $ax[$j]);
							}
						}
					} else {
						for (my $j = $pos[$i]; $j < $pos[$i + 1] - 2; $j++) {
							push(@r, $ax[$j]);
						}
					}
					push(@r, "]");
				}
				push(@r, $ax[$pos[$i + 1] - 2]);
				next LOOP;
			}
		}

		for (my $j = 0; $j < ($pos[$i + 1] - $pos[$i]); $j++) {
			push(@r, $ax[$pos[$i] + $j]);
		}
	}

	print join(" ", @r), "\n";
}
close($file);
