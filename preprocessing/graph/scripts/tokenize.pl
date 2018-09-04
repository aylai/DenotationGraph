#!/usr/bin/perl

use strict;
use warnings;

my $file;

# print only non-chunk boundary tokens.  Strip out any other
# meta-data.
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 2) {
		my @ay = ();
		foreach (split(/ /, $ax[2])) {
			my @az = split(/\//, $_);
			if (not $az[1] =~ /^[\[\]]/) {
				push(@ay, $az[1]);
			}
		}
		$ax[2] = join(" ", @ay);
	}

	print $ax[0], "\t", $ax[2], "\n";
}
close($file);
