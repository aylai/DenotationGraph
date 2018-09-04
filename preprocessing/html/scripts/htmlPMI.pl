#!/usr/bin/perl

# ./htmlPMI.pl <sub-graph dir> <HTML dir>

use strict;
use warnings;

my $file;

my %pmi = ();
my %cpb = ();
my %c = ();
my %cc = ();
my %index = ();

# read the co-occurrence counts, PMI, and conditional probabilities
open($file, "$ARGV[0]/node-image.cnt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	$c{$ax[0]} = $ax[2];
	$c{$ax[1]} = $ax[3];

	if (not exists $cc{$ax[0]}) {
		$cc{$ax[0]} = {};
	}
	$cc{$ax[0]}->{$ax[1]} = $ax[4];

	if (not exists $cc{$ax[1]}) {
		$cc{$ax[1]} = {};
	}
	$cc{$ax[1]}->{$ax[0]} = $ax[4];
}
close($file);

open($file, "$ARGV[0]/node-image.pmi");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if (not exists $pmi{$ax[4]}) {
		$pmi{$ax[4]} = {};
		$cpb{$ax[4]} = {};
	}
	$pmi{$ax[4]}->{$ax[6]} = $ax[0];
	$cpb{$ax[4]}->{$ax[6]} = $ax[1];

	if (not exists $pmi{$ax[6]}) {
		$pmi{$ax[6]} = {};
		$cpb{$ax[6]} = {};
	}
	$pmi{$ax[6]}->{$ax[4]} = $ax[0];
	$cpb{$ax[6]}->{$ax[4]} = $ax[2];
}
close($file);

# get the string of each node
open($file, "$ARGV[0]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (exists $cc{$ax[0]}) {
		$index{$ax[0]} = $ax[1];
	}
}
close($file);


mkdir("$ARGV[1]/pmi");

# print out a PMI table for each node.  Sort by PMIs
# use sorttable.js to make the tables sortable.
foreach my $n (keys %c) {
	open($file, ">$ARGV[1]/pmi/$n.html");
	print $file "<html>\n";
	print $file "<script src=\"../sorttable.js\"></script>\n";
	print $file "<style>\n";
	print $file "td { text-align:center; }\n";
	print $file "th { padding:10px; }\n";
	print $file "</style>\n";
	print $file "<body>\n";

	print $file "<h3><a href=\"../node/$n.html\">$index{$n}</a> ($c{$n})</h3><br>\n";

	print $file "<table class=\"sortable\">\n";
	print $file "<tr><th>x</th><th>pmi(x, $index{$n})</th><th>p(x|$index{$n})</th><th>p($index{$n}|x)</th><th>c(x)</th><th>c(x, $index{$n})</th></tr>\n";
	foreach (sort { $pmi{$n}->{$b} <=> $pmi{$n}->{$a} } keys %{$cc{$n}}) {
		printf $file ("<tr><td style=\"text-align:left\"><a href=\"$_.html\">$index{$_}</a></td><td>%.3f</td><td>%.3f</td><td>%.3f</td><td>$c{$_}</td><td>$cc{$n}->{$_}</td></tr>\n", $pmi{$n}->{$_} + 0.0005, $cpb{$_}->{$n} + 0.0005, $cpb{$n}->{$_} + 0.0005);
	}
	print $file "</table>\n";

	print $file "</body>\n";
	print $file "</html>\n";
}
