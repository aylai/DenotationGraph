#!/usr/bin/perl

# ./makeIndex.pl <graph> <html dir>

use strict;
use warnings;

my $file;

# get the strings of each node
my %index = ();
open($file, "$ARGV[0]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$index{$ax[0]} = $ax[1];
}
close($file);

# get the image counts of each node
my %c = ();
open($file, "$ARGV[0]/node-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my %hx = ();
	foreach (@ax) {
		my @ay = split(/\#/, $_);
		$hx{$ay[0]} = 1;
	}
	$c{$id} = scalar keys %hx;
}
close($file);

# load the type of each node and generate the pages
# 1 means it has a node page
# 2 means it has a PMI page
my %type = ();
open($file, "$ARGV[1]/node.type");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($ax[1] != 0) {
		$type{$ax[0]} = $ax[1];
	}
}
close($file);

open($file, ">$ARGV[1]/index.html");
print $file "<html>\n";
print $file "<body>\n";
print $file "<table>\n";
foreach (sort { $c{$b} <=> $c{$a} } keys %type) {
	print $file "<tr>";
	if (($type{$_} & 1) == 0) {
		print $file "<td></td>";
	} else {
		print $file "<td><a href=\"node/$_.html\">node</a></td>";
	}
	if (($type{$_} & 2) == 0) {
		print $file "<td></td>";
	} else {
		print $file "<td><a href=\"pmi/$_.html\">pmi</a></td>";
	}
	print $file "<td>$c{$_}</td><td>$index{$_}</td></tr>\n";
}
print $file "</table>\n";
print $file "<body>\n";
print $file "</html>\n";
close($file);
