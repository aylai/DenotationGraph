#!/usr/bin/perl

use strict;
use warnings;

my $file;
my %map = ();

# read in the map - store it inverted, though
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	foreach (@ax) {
		if (not exists $map{$_}) {
			$map{$_} = {};
		}
		$map{$_}->{$id} = 1;
	}
}
close($file);

# print out inverted map
foreach (sort keys %map) {
	print $_, "\t", join("\t", sort { $a <=> $b } keys %{$map{$_}}), "\n";
}
