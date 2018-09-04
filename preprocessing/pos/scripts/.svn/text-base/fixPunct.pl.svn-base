#!/usr/bin/perl

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	# change multiple spaces to a single space
	$_ =~ s/  */ /g;

    # tokenize commas
	$_ =~ s/([^ 0-9]),/$1 ,/g;
	$_ =~ s/,([^ 0-9])/, $1/g;
	my @ax = ();
	foreach (split(/ /, $_)) {
		# add a space for (); at end of word
		if ($_ =~ /^(.*)([\(\);])$/) {
			if ($1 ne "") {
				push(@ax, $1);
				push(@ax, $2);
				next;
			}
		# add a space for ();# at start of word
		} elsif ($_ =~ /^([\(\);\#])(.*)$/) {
			if ($2 ne "") {
				push(@ax, $1);
				push(@ax, $2);
				next;
			}
		}
		push(@ax, $_);
	}
	print join(" ", @ax), "\n";
}
close($file);
