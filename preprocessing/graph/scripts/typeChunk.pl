#!/usr/bin/perl

use strict;
use warnings;

my $file;

# VP node/chunking - starts with a VP chunk
# EN node/chunking - consists of only a single EN chunk
# SN node/chunking - everything else
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	my $type = "SN";

	if ($ax[1] =~ /^\[VP/) {
		$type = "VP";
	} elsif ($ax[1] =~ /^\[EN/) {
		my @ay = split(/ /, $ax[1]);
		my $depth = 0;
		my $i;
		for ($i = 0; $i <= $#ay; $i++) {
			if ($ay[$i] =~ /^\[/) {
				$depth++;
			} elsif ($ay[$i] =~ /^\]/) {
				$depth--;
				if ($depth == 0) {
					$depth = -1;
					last;
				}
			}
		}

		if ($depth == -1 && $i >= $#ay) {
			$type = "EN";
		}
	}

	print "$ax[0]\t$type\t$ax[1]\t", join("\t", @ax[2 .. $#ax]), "\n";
}
close($file);
