#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my @ax = split(/\//, $ARGV[0]);
pop(@ax);
my $dir = join("/", @ax);

my $file;

open($file, $ARGV[0]) or exit;
while (<$file>) {
	chomp($_);
	# get hte sub-graph image file, and pass it to subgraph.pl
	my @ay = split(/\t/, $_);
	print "$ay[0]\n";
	mkdir("$dir/graph/$ay[0]");
	system("$sdir/subgraph.pl $dir/graph $dir/$ay[1] $dir/graph/$ay[0]");
}
close($file);
