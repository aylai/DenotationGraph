#!/usr/bin/perl

use strict;
use warnings;

# if "is/are/has/had <verb>" is turned into two VP chunks, combine them

my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		# check if this is [VP is/are ] [VP ... ], combine the two VP chunks if it is
		if (($i + 4) <= $#ax && 
			$ax[$i] eq "[VP" &&
			($ax[$i + 1] eq "is/VBZ" || $ax[$i + 1] eq "are/VBP") &&
			$ax[$i + 2] eq "]" &&
			$ax[$i + 3] eq "[VP") {
			# check if this is an "is is" or "are are" case (should have been by the compound scripts)
			if ($ax[$i + 1] eq $ax[$i + 4]) {
				push(@ay, "[VP");
			} else {
				push(@ay, "[VP $ax[$i + 1]");
			}
			$i += 3;
		# check if this is [VP has/had ] [VP ... ], drop the has/had VP chunk if it is
		} elsif (($i + 4) <= $#ax && 
				 $ax[$i] eq "[VP" &&
				 ($ax[$i + 1] eq "has/VBZ" || $ax[$i + 1] eq "had/VBN") &&
				 $ax[$i + 2] eq "]" &&
				 $ax[$i + 3] eq "[VP") {
			$i += 2;
		} else {
			push(@ay, "$ax[$i]");
		}
	}
	print join(" ", @ay), "\n";
}
close($file);
