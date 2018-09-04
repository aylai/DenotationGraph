#!/usr/bin/perl

# replace "though" with "through", unless preceded by "even", "as" or ",".

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	print "$ax[0]\t";
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		for (my $i = 0; $i <= $#ay; $i++) {
			if ($i == 0) {
				print "$ay[$i]";
			} else {
				if (lc($ay[$i]) eq "though") {
					my $y = lc($ay[$i - 1]);
					if ($y eq "even" || $y eq "as" || $y eq ",") {
						print " $ay[$i]";
					} else {
						print " through";
					}
				} else {
					print " $ay[$i]";
				}
			}
		}
	}
	print "\n";
}
close($file);
