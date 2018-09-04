#!/usr/bin/perl

use strict;
use warnings;

my $file;

open ($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);

		# look for verbs not tagged as VBG, check if the previous token was tagged TO (so we're in a "to <verb>" case)
		# and then retag the verb as VB
		if ($ay[1] =~ /^V/ && $ay[1] ne "VBG" && $i > 0) {
			my @az = split(/\//, $ax[$i - 1]);
			if ($az[1] eq "TO") {
				$ax[$i] = "$ay[0]/VB";
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
