#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load the compound nouns
my %compound = ();
open($file, "$sdir/../data/compoundNouns.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$compound{$ax[0]} = $ax[1];
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = ();
	foreach (split(/ /, $_)) {
		my @ay = split(/\//, $_);

		# see if this is a compound noun we recognize
		if (exists $compound{lc($ay[0])}) {
			# if so, use the tagging information from
			# compoundNouns.txt to retag the other tokens
			my @ab = split(/-/, $compound{lc($ay[0])});
			my @aa = split(/-/, $ay[0]);
			push(@ab, $ay[1]);
			for (my $i = 0; $i <= $#aa; $i++) {
				push(@ax, "$aa[$i]/$ab[$i]");
			}
		} else {
			push(@ax, $_);
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
