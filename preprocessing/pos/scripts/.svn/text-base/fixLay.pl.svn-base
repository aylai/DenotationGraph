#!/usr/bin/perl

use strict;
use warnings;

my $file;

# changes instances of "lay" to "lie" if followed by [PP down ] and then not an NP chunk

my %lay = ();
$lay{"lay"} = "lie";
$lay{"lays"} = "lies";
$lay{"laying"} = "lying";

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();

	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i <= ($#ax - 4)) {
			my @ay = split(/\//, $ax[$i + 0]);
			# is this a version of "lay" that we know how to handle that is followed by a PP or PRT chunk?
			if (exists $lay{lc($ay[0])} && $ax[$i + 1] eq "]" && ($ax[$i + 2] eq "[PP" || $ax[$i + 2] eq "[PRT")) {
				my @az = split(/\//, $ax[$i + 3]);
				# also, is the PP/PRT chunk "down", and not followed by an NP chunk?
				if (lc($az[0]) eq "down" && $ax[$i + 4] eq "]" && ($i > ($#ax - 5) || $ax[$i + 5] ne "[NP")) {
					push(@r, $lay{lc($ay[0])} . "/$ay[1]");
					next;
				}
			}
		}
		push(@r, $ax[$i]);
	}

	print join(" ", @r), "\n";
}
close($file);
