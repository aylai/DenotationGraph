#!/usr/bin/perl

use strict;
use warnings;

my $file;

my %nodes = ();
open($file, $ARGV[1]);
while (<$file>) {
	chomp($_);
	$nodes{$_} = 1;
}
close($file);

open($file, "$ARGV[0]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (exists $nodes{$ax[0]}) {
		$nodes{$ax[0]} = $ax[1];
	}
}
close($file);

open($file, "$ARGV[0]/node-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (exists $nodes{$ax[0]} && exists $nodes{$ax[2]}) {
		print "$nodes{$ax[0]}\t$ax[1]\t$nodes{$ax[2]}\n";
	}
}
close($file);
