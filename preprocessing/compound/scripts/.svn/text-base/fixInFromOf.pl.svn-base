#!/usr/bin/perl

use strict;
use warnings;

my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		for (my $i = 1; $i <= ($#ay - 1); $i++) {
			# look for "in from of", and replace "from" with "front"
			if (lc($ay[$i]) eq "from" && lc($ay[$i - 1]) eq "in" && lc($ay[$i + 1]) eq "of") {
				$ay[$i] = "front";
			}
		}
		$ax[1] = join(" ", @ay);
	}
	print join("\t", @ax), "\n";
}
close($file);
