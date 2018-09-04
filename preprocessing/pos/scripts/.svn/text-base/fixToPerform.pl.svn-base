#!/usr/bin/perl

use strict;
use warnings;

# convert [PP to/TO ] [ perform ... ] into [VP to/TO perform ...]

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chop($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i <= ($#ax - 4) &&
			$ax[$i + 0] eq "[PP" &&
			$ax[$i + 1] eq "to/TO" &&
			$ax[$i + 2] eq "]" &&
			$ax[$i + 3] =~ /^\[/ &&
			$ax[$i + 4] eq "perform/VBP") {
			push(@ay, "[VP");
			push(@ay, $ax[$i + 1]);
			push(@ay, $ax[$i + 4]);
			$i = $i + 4;
			next;
		}
		push(@ay, $ax[$i]);
	}

	print join(" ", @ay), "\n";
}
close($file);
