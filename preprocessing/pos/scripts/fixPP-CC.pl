#!/usr/bin/perl

use strict;
use warnings;

# [PP CC ] -> CC

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] eq "[PP" && ($i + 2) <= $#ax && $ax[$i + 2] eq "]") {
			my @ay = split(/\//, $ax[$i + 1]);
			if ($ay[1] eq "CC") {
				push(@r, $ax[$i + 1]);
				$i += 2;
				next;
			}
		}
		push(@r, $ax[$i]);
	}

	print join(" ", @r), "\n";
}
close($file);
