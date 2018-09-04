#!/usr/bin/perl

use strict;
use warnings;

my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 2) {
	    # EN (NP) and VP chunks are labeled according to the order
	    # they appear in, starting with 0.
	    my @ay = ();
	    my $np = 0;
	    my $vp = 0;
	    foreach (split(/ /, $ax[2])) {
			my @az = split(/\//, $_);

			# store the token ID
			my $id = shift(@az);

			# pull out the token - we'll check if it's an
			# beginning of EN or VP chunk boundary - if it is,
			# we'll put it back, but we want to add an ID first.
			my $token = shift(@az);

			# if it's an EN chunk, add an NP id tag to the second
			# field, and increment the NP id
			if ($token eq "[EN") {
				unshift(@az, "NP$np");
				unshift(@az, $token);
				unshift(@az, $id);
				push(@ay, join("/", @az));
				$np++;

			# if it's a VP chunk, add a VP id tag to the second
			# field, and increment the VP id
			} elsif ($token eq "[VP") {
				unshift(@az, "VP$vp");
				unshift(@az, $token);
				unshift(@az, $id);
				push(@ay, join("/", @az));
				$vp++;
			} else {
				push(@ay, $_);
			}
	    }
	    $ax[2] = join(" ", @ay);
	}
	print join("\t", @ax), "\n";
}
close($file);
