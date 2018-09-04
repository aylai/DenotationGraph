#!/usr/bin/perl

use strict;
use warnings;

# retag -ing words with VBG

my $file;

# -ing words that could be nouns
my %except = ();
$except{"building"} = 1;
$except{"clothing"} = 1;
$except{"earring"} = 1;
$except{"lettering"} = 1;
$except{"painting"} = 1;
$except{"railing"} = 1;
$except{"roping"} = 1;
$except{"something"} = 1;
$except{"stitching"} = 1;
$except{"swing"} = 1;
$except{"wedding"} = 1;
$except{"writing"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);

		# found an -ing word tagged as a noun that probably isn't a noun
		if ($ay[0] =~ /ing$/ && $ay[1] eq "NN" && !exists $except{lc($ay[0])}) {
			# check prior tags - if we've got a determiner of some sort, then this actually is a noun
			my $j = $i - 1;
			while ($j > 0) {
				my @az = split(/\//, $ax[$j]);
				if ($az[1] eq "DT" || $az[1] eq "PRP\$") {
					next LOOP;
				} elsif ($az[1] eq "JJ") {
				} else {
					last;
				}
				$j--;
			}
			$ay[1] = "VBG";
			$ax[$i] = join("/", @ay);
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
