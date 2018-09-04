#!/usr/bin/perl

use strict;
use warnings;

# rechunks cases where cooks has been mistagged as a verb, e.g.
# [NP person cooks ] -> [NP person ] [VP cooks/VBZ ]
# (should look into fixing this in the tagging stage.

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();
	my $chunk = "";

	for (my $i = 0; $i <= $#ax; $i++) {
		# store the type of chunk we're in (need to know if we're in the middle of an NP chunk)
		if ($ax[$i] =~ /^\[/) {
			$chunk = $ax[$i];
		} elsif ($ax[$i] =~ /^\]/) {
			$chunk = "";
		} else {
			# look for "cooks" in an NP chunk
			my @ay = split(/\//, $ax[$i]);
			if (lc($ay[0]) eq "cooks" && $ay[1] =~ /^N/ && $chunk eq "[NP") {
				if ($i > 0) {
					# check if the previous token was a singular noun
					# other tags (such as adjectives) mean that "cooks" can still be the head of the NP
					# but a noun probably means that "cooks" is a verb
					my @az = split(/\//, $ax[$i - 1]);
					if ($#az == 1 && $az[1] eq "NN") {
						push(@r, "]");
						push(@r, "[VP");
						push(@r, $ay[0] . "/VBZ");
						if (not $ax[$i + 1] =~ /^\]/) {
							push(@r, "]");
							push(@r, "[NP");
						}
						next;
					}
				}
			}
		}
		push(@r, $ax[$i]);
	}

	print join(" ", @r), "\n";
}
close($file);
