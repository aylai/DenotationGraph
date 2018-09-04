#!/usr/bin/perl

use strict;
use warnings;

# ensure that words that are composed entirely of letters don't have punctuation tags

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my $s = $_;
	foreach (split(/ /, $_)) {
		my @ax = split(/\//, $_);
		if ($#ax == 1) {
			if ($ax[1] =~ /^[^A-Z]/ && $ax[0] =~ /^[A-Za-z]*$/) {
				print "$_\t$s\n";
			}
		}
	}
}
close($file);
