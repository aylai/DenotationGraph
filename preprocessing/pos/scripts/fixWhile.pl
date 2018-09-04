#!/usr/bin/perl

use strict;
use warnings;

# "while" when chunked by itself should be in an SBAR

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);

	for (my $i = 1; $i < $#ax; $i++) {
		if (lc($ax[$i + 0]) eq "while/in" && ($ax[$i - 1] eq "[NP" || $ax[$i - 1] eq "[PP") && $ax[$i + 1] eq "]") {
			$ax[$i - 1] = "[SBAR";
		}
	}

	print join(" ", @ax), "\n";
}
close($file);
