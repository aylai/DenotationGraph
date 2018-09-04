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
			my $w = lc($ay[$i]);

			# split up "ice-cream"
			if ($w eq "ice-cream") {
				my @aw = split(/-/, $ay[$i]);
				if ($#aw == 1) {
					push(@az, $aw[0]);
					push(@az, $aw[1]);
					next;
				}
			# and remove hyphens in "wind-sail", "surf-board", etc.
			} elsif ($w =~ /-sail/ || $w =~ /-board/) {
				$ay[$i] =~ s/-//;
			}
			
			push(@az, $ay[$i]);
		}
		$ax[1] = join(" ", @az);
	}
	print join("\t", @ax), "\n";
}
close($file);
