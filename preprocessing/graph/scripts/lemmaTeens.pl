#!/usr/bin/perl

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 2) {
		my @ay = ();
		foreach (split(/ /, $ax[2])) {
			my @az = split(/\//, $_);
			if (lc($az[1]) =~ /^teen/) {
				my $x = substr($az[1], 0, 4);
				if (lc($az[1]) =~ /s$/) {
					$az[1] = $x . "s";
				} else {
					$az[1] = $x;
				}
				push(@ay, join("/", @az));
			} else {
				push(@ay, $_);
			}
		}
		$ax[2] = join(" ", @ay);
	}
	print join("\t", @ax), "\n";
}
close($file);
