#!/usr/bin/perl

use strict;
use warnings;

my $file;

my $i = 0;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$ax[4] = $i;
	print join("\t", @ax), "\n";
	$i++;
}
close($file);
