#!/usr/bin/perl

use strict;
use warnings;

# convert [VP to/TO ] to [PP to/IN ]

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i <= ($#ax - 2) && $ax[$i + 0] eq "[VP" && lc($ax[$i + 1]) eq "to/to" && $ax[$i + 2] eq "]" ) {
			my @ay = split(/\//, $ax[$i + 1]);
			$ax[$i + 0] = "[PP";
			$ax[$i + 1] = "ay[0]/IN";
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
