#!/usr/bin/perl

use strict;
use warnings;


my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

# load the ngrams to concatenate - %fix is indexed by the concatenated
# ngram, and stores the tag assigned to the concatenated ngram
# $max is the length of the longest ngram
my $file;
my %fix = ();
my $max = 0;
open($file, "$sdir/../data/ngram.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/-/, $ax[0]);
	$fix{$ax[0]} = $ax[1];
	if (scalar @ay > $max) {
		$max = scalar @ay;
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	my @ax = split(/ /, $_);
	my @az = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		$ax[$i] = [ @ay ];
	}

	# find instances of the ngrams in the caption
	for (my $i = 0; $i <= $#ax; $i++) {
		my $j = 0;
		my @s = ();
		# grab up to $max of the next tokens
		for ($j = 0; ($j + $i) <= $#ax && $j < $max; $j++) {
			push(@s, $ax[$j + $i]->[0]);
		}

		# we'll slowly decrease the number of tokens we've grabbed as we look for ngrams
		# this lets us find the longest ones first
		while ($j > 0) {
			if (exists $fix{lc(join("-", @s))}) {
				last;
			}
			$j--;
			pop(@s);
		}

		# if we've found a token, concatenate, use the new tag, and update the index $i
		if ($j > 0) {
			push(@az, join("-", @s) . "/" . $fix{lc(join("-", @s))});
			$i += $j - 1;
		} else {
			push(@az, join("/", @{$ax[$i]}));
		}
	}
	print join(" ", @az), "\n";
}
close($file);
