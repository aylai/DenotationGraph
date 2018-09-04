#!/usr/bin/perl

use strict;
use warnings;

my $file;

# store caption IDs + token IDs of empty brackets found
my %found = ();

for (my $j = 0; $j <= $#ARGV; $j++) {
	open($file, $ARGV[$j]);
	while (<$file>) {
		chomp($_);
		my @ax = split(/\t/, $_);

		# only want the captions/strings
		if ($#ax != 2) {
			next;
		}

		# store tokens (@ay) and token IDs (@ids)
		my @ay = split(/ /, $ax[2]);
		my @ids = ();
		for (my $i = 0; $i <= $#ay; $i++) {
			my @az = split(/\//, $ay[$i]);
			$ay[$i] = $az[1];
			$ids[$i] = $az[0];
		}

		# go through each token - look for empty brackets
		for (my $i = 0; $i < $#ay; $i++) {
			if ($ay[$i + 0] =~ /^\[/ && $ay[$i + 1] =~ /^\]/) {
				# note down the caption ID and token ID of the empty bracket
				# make sure we haven't seen it before
				my $id = "$ax[0]#$ids[$i]";
				if (not exists $found{$id}) {
					my $x = $ay[$i];
					$x =~ s/^\[//;
					print "$ARGV[$j]\t$x\t$ax[0]\t", join(" ", @ay), "\n";
					$found{$id} = 1;
				}
			}
		}
	}
	close($file);
}
