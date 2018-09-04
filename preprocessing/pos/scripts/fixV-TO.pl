#!/usr/bin/perl

use strict;
use warnings;

# we want to retag these tokens as verbs, if they're followed by a "to"
my %override = ();
$override{"diving"} = "VBG";
$override{"turns"} = "VBZ";

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# if one of the tokens we want to retag as a verb isn't already tagged as a verb
		if ((not $ay[1] =~ /^V/) && exists $override{lc($ay[0])}) {
			# check if the next token is "to"
			if ($i < $#ax) {
				my @az = split(/\//, $ax[$i + 1]);
				if ($az[1] ne "TO") {
					next;
				}
			}

			$ax[$i] = $ay[0] . "/" . $override{lc($ay[0])};
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
