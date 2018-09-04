#!/usr/bin/perl

use strict;
use warnings;

# list of words that are always VBPs unless preceded by a determiner
my %override = ();
$override{"struggle"} = "VBP";
$override{"try"} = "VBP";

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# non-verb word that we'd like to turn into a verb
		if ((not $ay[1] =~ /^V/) && exists $override{lc($ay[0])}) {
			# check if the preceding token was a determiner
			if ($i > 0) {
				my @az = split(/\//, $ax[$i - 1]);
				if ($az[1] eq "DT") {
					next;
				}
			}

			$ax[$i] = $ay[0] . "/" . $override{lc($ay[0])};
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
