#!/usr/bin/perl

use strict;
use warnings;

# rechunk shop/shops as their own VP chunk, if preceded by an appropriate type of noun (singular vs. plural for shops vs shop).
# may want to look into handling this as a retagging instead of a rechunking

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();
	my $np = 0;
	my $chunk = "";
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] eq "[NP") {
			$np++;
		}

		# save the current chunk we're in (need to know if we're in an NP)
		if ($ax[$i] =~ /^\[/) {
			$chunk = $ax[$i];
		} elsif ($ax[$i] =~ /^\]/) {
			$chunk = "";
		# if we're in an NP chunk, and it is the first NP chunk, check if we want to do our rechunking
		} elsif ($i > 0 && $i < $#ax && $ax[$i + 1] eq "]" && $chunk eq "[NP" && $np == 1) {
			my @ay = split(/\//, $ax[$i]);
			my @az = split(/\//, $ax[$i - 1]);
			# NN shops -> [NP .. NN ] [VP shops/VBZ ]
			if ($#az == 1 && $az[1] eq "NN" && lc($ay[0]) eq "shops") {
				push(@r, "]");
				push(@r, "[VP");
				push(@r, $ay[0] . "/VBZ");
				next;
			# NNS shop -> [NP .. NNS ] [VP shop/VBP ]
			} elsif ($#az == 1 && $az[1] eq "NNS" && lc($ay[0]) eq "shop") {
				push(@r, "]");
				push(@r, "[VP");
				push(@r, $ay[0] . "/VBP");
				next;
			}
		}

		push(@r, $ax[$i]);
	}

	print join(" ", @r), "\n";
}
close($file);
