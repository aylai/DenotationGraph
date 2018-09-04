#!/usr/bin/perl

use strict;
use warnings;

# move a TO at the end of a VP chunk outside of the VP
# "[VP walk to ]" -> "[VP walk ] [PP to ]"
# (maybe that should be a PRT chunk.  Hard to tell

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();

	# $vp - -1 outside of a vp, otherwise the number of tokens in the VP seen so far
	my $vp = -1;


	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] eq "[VP") {
			$vp = 0;
		} elsif ($ax[$i] eq "]") {
			$vp = -1;
		# if we're seeing a TO in a VP and the next thing is the end of the VP chunk
		} elsif ($ax[$i] =~ /\/TO$/ && $vp >= 0) {
			if ($i < $#ax && $ax[$i + 1] eq "]") {
				# if we're the only thing in the VP chunk, change to a PP chunk
				if ($vp == 1) {
					pop(@ay);
					push(@ay, "[PP");
				# otherwise, close the VP chunk and place the TO in a new PP chunk
				} else {
					push(@ay, "]");
					push(@ay, "[PP");
				}
			}
		}

		push(@ay, $ax[$i]);
		if ($vp >= 0) {
			$vp++;
		}
	}

	print join(" ", @ay), "\n";
}
close($file);
