#!/usr/bin/perl

use strict;
use warnings;

# retag sign as a noun if it's (" <text> " sign)

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my $z = 0;
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);

		if ($z == 1 && lc($ay[0]) eq "sign") {
			$ax[$i] = "$ay[0]/NN";
		}

		if ($ay[0] eq "\"") {
			$z = 1;
		} else {
			$z = 0;
		}
	}

	print join(" ", @ax), "\n";
}
close($file);
