#!/usr/bin/perl

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		$ax[1] =~ s/^ *//;
		$ax[1] =~ s/ *$//;
		$ax[1] =~ s/  */ /g;
	}
	print join("\t", @ax), "\n";
}
close($file);
