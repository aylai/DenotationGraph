#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use WordNet::QueryData;

my $file;

my %synset = ();
my %word = ();

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if (not exists $synset{$ax[1]}) {
		$synset{$ax[1]} = $ax[0];
	}
	$word{$ax[0]} = $ax[1];
}
close($file);

my %parents = ();

foreach my $s (keys %synset) {
	my @queue = getHypes($s);
	$parents{$s} = {};
	while ($#queue >= 0) {
		my $q = shift(@queue);
		if (exists $synset{$q}) {
			$parents{$s}->{$q} = 1;
		} else {
			@queue = @queue, getHypes($q);
		}
	}
}

foreach my $w (sort keys %word) {
	my $s = $word{$w};

	if ($w ne $synset{$s}) {
		print "$w\t$synset{$s}\n";
	} else {
		foreach (keys %{$parents{$s}}) {
			print "$w\t$synset{$_}\n";
		}
	}
}
