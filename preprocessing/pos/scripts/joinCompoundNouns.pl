#!/usr/bin/perl

use strict;
use warnings;

# hyphenate compound nouns, so the chunker doesn't break them up

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load the list of compound nouns
# keep track of the longest sequence of tokens that makes up a compound noun,
# so we know how many tokens we need to keep track of in order to form a possible compound noun
my $max = 0;
my %compound = ();
open($file, "$sdir/../data/compoundNouns.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/-/, $ax[0]);
	$compound{$ax[0]} = $ax[1];
	if (scalar @ay > $max) {
		$max = scalar @ay;
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	my @ax = split(/ /, $_);

	# split tokens into token + tag
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		$ax[$i] = [ @ay ];
	}

	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i != 0) {
			print " ";
		}

		# find the longest string of tokens that is a compound noun
		my $j = 0;
		my @s = ();
		for ($j = 0; ($j + $i) <= $#ax && $j < $max; $j++) {
			push(@s, $ax[$j + $i]->[0]);
		}
		while ($j > 0) {
			if (exists $compound{lc(join("-", @s))}) {
				last;
			}
			$j--;
			pop(@s);
		}

		# make sure we're really dealing with a noun here - last token must be an N*
		# (or an RB if it's "back" - in which case, retag it as an NN)
		if ($j > 0 && ($ax[$i + $j - 1]->[1] =~ /^N/ || lc($ax[$i + $j - 1]->[0]) eq "back")) {
			if ($ax[$i + $j - 1]->[1] eq "RB") {
				$ax[$i + $j - 1]->[1] = "NN";
			}
			print join("-", @s), "/", $ax[$i + $j - 1]->[1];
			$i += $j - 1;
		} else {
			print join("/", @{$ax[$i]});
		}
	}
	print "\n";
}
close($file);
