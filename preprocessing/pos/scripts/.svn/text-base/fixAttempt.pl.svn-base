#!/usr/bin/perl

use strict;
use warnings;

# retag attempt as a verb.
# this can be tricky because the noun/verb cases that we're retagging are similar:
#  verb: Xs attempt to ...
#  noun: in an attempt to ...
# so we'll look for a succeeding "to", and make sure there are no indications that its actually a noun
my %override = ();
$override{"attempt"} = "VB";

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# check if the token is one that we want to retag, but is not already a verb
		if ((not $ay[1] =~ /^V/) && exists $override{lc($ay[0])}) {
			# look for a succeeding "to"
			if ($i < $#ax) {
				my @az = split(/\//, $ax[$i + 1]);
				if ($az[1] ne "TO") {
					next;
				}
			}

			# make sure it's not preceded by a preposition or determiner - those indicate that this is a noun
			if ($i < $#ax) {
				my @az = split(/\//, $ax[$i - 1]);
				if ($az[1] eq "IN" || $az[1] eq "DT") {
					next;
				}
			}

			$ax[$i] = $ay[0] . "/" . $override{lc($ay[0])};
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
