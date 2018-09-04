#!/usr/bin/perl

use strict;
use warnings;

my $file;

# retag the following tokens as VBs if they are preceded by a "to"
# note: will always retag these tokens as verbs, even if they were previously tagged as something else
my %verb = ();
$verb{"herd"} = "VB";

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i < $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# check if we're seeing a "to"
		if ($ay[1] eq "TO") {
			# see if the next token is something we want to retag as a verb
			my @az = split(/\//, $ax[$i + 1]);
			if (exists $verb{lc($az[0])}) {
				$az[1] = $verb{lc($az[0])};
				$ax[$i + 1] = join("/", @az);
			}
		}
	}

	print join(" ", @ax), "\n";
}
close($file);
