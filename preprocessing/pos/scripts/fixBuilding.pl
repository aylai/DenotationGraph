#!/usr/bin/perl

use strict;
use warnings;

my $file;

# if building is preceded by one of these prepositions leave it as a verb
my %prep = ();
$prep{"after"} = 1;
$prep{"at"} = 1;
$prep{"for"} = 1;
$prep{"on"} = 1;
$prep{"while"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 1; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# non-noun "building" - turn this into a noun if preceded by a determiner or an unrecognized preposition
		if (lc($ay[0]) eq "building" && $ay[1] =~ /^[^N]/) {
			my @az = split(/\//, $ax[$i - 1]);
			if ($az[1] eq "DT" || ($az[1] eq "IN" && !exists $prep{lc($az[0])})) {
				$ax[$i] = "$ay[0]/NN";
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
