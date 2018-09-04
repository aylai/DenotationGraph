#!/usr/bin/perl

# normalize hyphens - change all instance of multiple hyphens to
# singles ("--" -> "-")

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = ();
	if ($#ax >= 1) {
		foreach (split(/ /, $ax[1])) {
			if ($_ =~ /^(.*)---*(.*)$/) {
				push(@ay, "$1-$2");
			} else {
				push(@ay, $_);
			}
		}
	}
	print "$ax[0]\t", join(" ", @ay), "\n";
}
close($file);
