#!/usr/bin/perl

use strict;
use warnings;

my $file;

# look for the token "converse" - the only time it shouldn't be a verb is when
# we're talking about converse shoes.

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		if (lc($ay[0]) eq "converse") {
			if ($i < $#ax) {
				my @az = split(/\//, $ax[$i + 1]);
				if (lc($az[0]) eq "shoes" || lc($az[0]) eq "sneakers") {
					next;
				}
			}
			$ay[1] = "VBP";
			$ax[$i] = join("/", @ay);
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
