#!/usr/bin/perl

use strict;
use warnings;

my $file;
my $out;
open($file, "$ARGV[0]/node-cap.map");
open($out, ">$ARGV[0]/node-img.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my %hx = ();
	my $id = shift(@ax);
	# strip the caption number from the image file name
	foreach (@ax) {
		my @ay = split(/\#/, $_);
		$hx{$ay[0]} = 1;
	}
	print $out "$id\t", join("\t", sort keys %hx), "\n";
}
close($out);
close($file);
