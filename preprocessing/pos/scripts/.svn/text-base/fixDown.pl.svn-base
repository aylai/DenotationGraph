#!/usr/bin/perl

use strict;
use warnings;

# retag down as either an adjective (if it was a list item marker), or a preposition (if it was a verb)

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ay = ();
	foreach (split(/ /, $_)) {
		my @ax = split(/\//, $_);

		if (lc($ax[0]) eq "down") {
			if ($ax[1] eq "LS") {
				$ax[1] = "JJ";
			} elsif ($ax[1] =~ /^V/) {
				$ax[1] = "IN";
			}
		}

		push(@ay, join("/", @ax));
	}
	print join(" ", @ay), "\n";
}
close($file);
