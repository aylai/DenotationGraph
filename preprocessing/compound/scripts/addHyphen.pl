#!/usr/bin/perl

use strict;
use warnings;

my $file;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

# get maximum number of terms to look for
# and get hyphenated terms
my $max = 0;
my %hyphen = ();
open($file, "$sdir/../data/hyphen.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/-/, $_);
	if ($#ax > $max) {
		$max = $#ax;
	}
	$hyphen{$_} = 1;
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my @az = ();

LOOP:
		for (my $i = 0; $i <= $#ay; $i++) {
			# look for a term that should be hyphenated ($max is the max lookahead)
			for (my $j = 1; $j <= $max && ($i + $j) <= $#ay; $j++) {
				my $x = join("-", @ay[$i .. ($i + $j)]);
				if (exists $hyphen{lc($x)}) {
					push(@az, $x);
					$i = $i + $j;
					next LOOP;
				}
			}
			push(@az, $ay[$i]);
		}
		$ax[1] = join(" ", @az);
	}
	print join("\t", @ax), "\n";
}
close($file);
