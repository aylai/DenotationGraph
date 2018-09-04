#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

my %pos = ();
my %poss = ();
my %prior = ();

#load the overrides
open($file, "$sdir/../data/forcePOS.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	# %pos is indexed by word, and gives the new tag
	$pos{$ax[0]} = $ax[1];
	# %poss is indexed by word, and is the list of possible tags that are acceptable
	$poss{$ax[0]} = {};
	# %prior is indexed by word, and is the list of previous words that mean we should skip retagging
	$prior{$ax[0]} = {};
	foreach (@ax[1 .. $#ax]) {
		if ($_ =~ /^-(.*)/) {
			$prior{$ax[0]}->{$1} = 1;
		} else {
			$poss{$ax[0]}->{$_} = 1;
		}
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my $last = "";
	my $lpos = "";
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# make sure we're not a proper noun (NNP), that we have a possible retagging ($pos), that our current tag isn't acceptable ($poss), and that we're not part of a compound word that shouldn't be retagged ($prior)
		if ($ay[1] ne "NNP" && exists $pos{lc($ay[0])} && !exists $poss{lc($ay[0])}->{$ay[1]} && !exists $prior{lc($ay[0])}->{$last}) {
			# get the new tag - if its acceptable for the word to be a noun, and its preceded by a determiner, its really a noun
			my $npos = $pos{lc($ay[0])};
			if ($lpos eq "DT" && exists $poss{lc($ay[0])}->{"NN"}) {
				$npos = "NN";
			} elsif ($lpos eq "DT" && exists $poss{lc($ay[0])}->{"NNS"}) {
				$npos = "NNS";
			}

			$ay[1] = $npos;
			$ax[$i] = join("/", @ay);
			# if we're retagging this as a verb, and the construction is X and Y - retag Y as well
			# its a bit too complex to do this for nouns, unfortunately
			if ($npos =~ /^V/ && ($i + 2) <= $#ax && lc($ax[$i + 1]) eq "and/cc") {
				@ay = split(/\//, $ax[$i + 2]);
				if ($ay[1] =~ /^N/) {
					$ay[1] = $npos;
					$ax[$i + 2] = join("/", @ay);
				}
			}
		}
		$last = lc($ay[0]);
		$lpos = $ay[1];
	}
	print join(" ", @ax), "\n";
}
close($file);
