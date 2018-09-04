#!/usr/bin/perl

# retag watch as a verb, unless it really is a wristwatch

use strict;
use warnings;

# types of watches
my %valid = ();
$valid{"gold"} = 1;
$valid{"silver"} = 1;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		# identify instances of "watch"
		if (lc($ax[$i]) eq "watch/nn") {
			my $j = $i - 1;

			# backtrack, looking for indications that it is a wristwatch
			# some sort of determiner .  Ignore adjectives and "gold/silver",
			# assume anything else means it is a verb.
			while ($j >= 0) {
				my @ay = split(/\//, $ax[$j]);
				if ($ay[1] eq "DT" || $ay[1] eq "PRP\$" || $ay[1] eq "POS") {
					last;
				} elsif ($ay[1] eq "JJ") {
				} elsif ($ay[1] eq "NN" && exists $valid{lc($ay[0])}) {
				} else {
					@ay = split(/\//, $ax[$i]);
					$ax[$i] = "$ay[0]/VBP";
					next LOOP;
				}
				$j--;
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
