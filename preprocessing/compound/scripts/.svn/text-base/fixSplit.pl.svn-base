#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load words to split up
my %split = ();
open($file, "$sdir/../data/split.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my $x = $ax[0] . $ax[1];
	$split{$x} = length($ax[0]);
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my @az = ();
		for (my $i = 0; $i <= $#ay; $i++) {
			# if it's a word we want to split, split it at the space
			if (exists $split{lc($ay[$i])}) {
				my $x = $split{lc($ay[$i])};
				push(@az, substr($ay[$i], 0, $x));
				push(@az, substr($ay[$i], -(length($ay[$i]) - $x)));
			} else {
				push(@az, $ay[$i]);
			}
		}
	}
	print join("\t", @ax), "\n";
}
close($file);
