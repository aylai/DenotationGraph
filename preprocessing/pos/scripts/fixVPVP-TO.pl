#!/usr/bin/perl

use strict;
use warnings;

# join together VP chunks that are connected by a TO
# [VP jump ] [VP to catch ] -> [VP jump to catch ]

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);

	# @chunk - list of chunks in the caption
	# @pos - index of the starting boundary of the chunks
	# $j - total number of chunks
	my @chunk = ();
	my @pos = ();
	my $j = 0;
	my $state = 0;
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] =~ /^\[/) {
			$chunk[$j] = $ax[$i];
			$pos[$j] = $i;
			$j++;
			$state = 1;
		} elsif ($ax[$i] =~ /^\]/) {
			$state = 0;
		} elsif ($state == 0) {
			$chunk[$j] = "";
			$pos[$j] = $i;
			$j++;
			$state = 1;
		}
	}

	for (my $i = 1; $i < $j; $i++) {
		# see if we have a VP chunk that start with a TO
		if ($chunk[$i] eq "[VP") {
			my @ay = split(/\//, $ax[$pos[$i] + 1]);
			if ($ay[1] eq "TO") {
				my $k = $i - 1;

				# checking backwards...
				while ($k >= 0) {
					# any of these chunks can be combined into our new VP chunk
					if ($chunk[$k] eq "" || $chunk[$k] eq "[ADJP" || $chunk[$k] eq "ADVP" || $chunk[$k] eq "[PRT") {
						$k--;
					# we've encountered another VP chunk - we'll combine this with the current VP chunk
					} elsif ($chunk[$k] eq "[VP") {
						# count the number of tokens we'd be adding to the VP chunk ($n)
						# if there are too many (more than two), we don't want to combine them
						my $n = 0;
						for (my $l = $pos[$k + 1]; $l <= $pos[$i]; $l++) {
							my @ay = split(/\//, $ax[$l]);
							if ($#ay == 1) {
								$n++;
								if ($ay[1] =~ /[^A-Z]/ || $ay[1] eq "CC") {
									$n = 100;
									last;
								}
							}
						}

						if ($n > 2) {
							last;
						}

						# nuke all chunk boundaries between the two VP chunks
						for (my $l = $pos[$k] + 1; $l <= $pos[$i]; $l++) {
							if ($ax[$l] =~ /^[\[\]]/) {
								$ax[$l] = "";
							}
						}
						last;
					} else {
						last;
					}
				}
			}
		}
	}

	my @ay = ();
	foreach (@ax) {
		if ($_ ne "") {
			push(@ay, $_);
		}
	}
	print join(" ", @ay), "\n";
}
close($file);
