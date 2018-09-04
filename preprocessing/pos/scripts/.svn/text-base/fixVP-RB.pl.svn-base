#!/usr/bin/perl

use strict;
use warnings;

# fix cases where a VP chunk consists of only adverbs

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @aw = ();

LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		push(@aw, $ax[$i]);

		if ($ax[$i] eq "[VP") {
			# @az - contents of VP
			my $j;
			my @az = ();
			# process through the VP chunk - if you find any non-RB tokens, skip
			for ($j = $i + 1; $j <= $#ax; $j++) {
				if ($ax[$j] eq "]") {
					last;
				}

				my @ay = split(/\//, $ax[$j]);
				push(@az, $ay[0]);
				if ($ay[1] ne "RB") {
					next LOOP;
				}
			}

			# if this is the last chunk in the caption, we can't really do anything with it
			if ($j < $#ax) {
				# "back" gets mistagged as an adverb a lot.  May actually be a noun
				# check if the previous thing is a possessive (POS) or a possessive pronoun (PRP$)
				if (join(" ", @az) eq "back" &&
					$#aw > 3 && $aw[$#aw - 1] eq "]" && $aw[$#aw - 2] =~ /\/P[RO][PS]/ && $aw[$#aw - 3] eq "[NP") {
					pop(@aw);
					pop(@aw);
					push(@aw, "back/NN");
					$i++;
				# if the next thing is an NP, PRT, or ADVP junk, add the contents of the VP chunk to it
				} elsif ($ax[$j + 1] eq "[NP" || $ax[$j + 1] eq "[PRT" || $ax[$j + 1] eq "[ADVP") {
					pop(@aw);
					push(@aw, $ax[$j + 1]);
					$i++;
					while ($i < $j) {
						push(@aw, $ax[$i]);
						$i++;
					}
					$i = $j + 1;
				# rechunk it as a PP
				} else {
					pop(@aw);
					push(@aw, "[PP");
				}
			}
		}
	}

	print join(" ", @aw), "\n";
}
close($file);
