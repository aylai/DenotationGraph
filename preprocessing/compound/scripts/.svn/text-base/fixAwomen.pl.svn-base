#!/usr/bin/perl

use strict;
use warnings;

my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my @az = ();
		for (my $i = 0; $i <= $#ay; $i++) {
			# is this "a women", and not part of "a women 's ..."?
			if (lc($ay[$i]) eq "women" && $i > 0 && $i < $#ay) {
				if (lc($ay[$i - 1]) eq "a" && lc($ay[$i + 1]) ne "'s") {
					push(@az, "woman");
					next;
				}
			# is this "a men", and not part of "a men 's ..."?
			} elsif (lc($ay[$i]) eq "men" && $i > 0 && $i < $#ay) {
				if (lc($ay[$i - 1]) eq "a" && lc($ay[$i + 1]) ne "'s") {
					push(@az, "man");
					next;
				}
			}
			push(@az, $ay[$i]);
		}
		$ax[1] = join(" ", @az);
	}
	print join("\t", @ax), "\n";
}
close($file);
