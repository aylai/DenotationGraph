#!/usr/bin/perl

use strict;
use warnings;

my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my %hx = ();
	foreach (@ax) {
		my @ay = split(/\#/, $_);
		$hx{$ay[0]} = 1;
	}
	print $id, "\t", scalar keys %hx, "\n";
}
close($file);
