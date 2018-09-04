#!/usr/bin/perl

# retag UH (interjections) as NN (this pretty much covers the caption
# starting with "boy" which is mistagged as an interjection case)

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	if ($#ax >= 0) {
		my @ay = split(/\//, $ax[0]);
		if ($ay[1] eq "UH") {
			$ay[1] = "NN";
			$ax[0] = join("/", @ay);
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
