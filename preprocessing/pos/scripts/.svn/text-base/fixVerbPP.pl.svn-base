#!/usr/bin/perl

use strict;
use warnings;

my $file;

# tokens to retag as verbs
# will be retagged in the succeeding word indicates that it should be a verb
# (is an RB, TO, CC, or RP - except for "of".  "stands of X", etc. are nouns)
my %verbs = ();
$verbs{"looks"} = 1;
$verbs{"poses"} = 1;
$verbs{"stands"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i < $#ax) {
			my @ay = split(/\//, $ax[$i + 0]);
			my @az = split(/\//, $ax[$i + 1]);

			# check if the succeeding word is one that indicates retagging is appropriate
			if (lc($az[0]) ne "of" && ($az[1] eq "IN" || $az[1] eq "RB" || $az[1] eq "TO" || $az[1] eq "CC" || $az[1] eq "RP")) {
				if ($i > 0) {
					my @aw = split(/\//, $ax[$i - 1]);
					if ($aw[1] eq "DT") {
						next;
					}
				}

				# check if we want to retag the word
				if (exists $verbs{lc($ay[0])}) {
					$ay[1] = "VBZ";
					$ax[$i + 0] = join("/", @ay);
				}
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
