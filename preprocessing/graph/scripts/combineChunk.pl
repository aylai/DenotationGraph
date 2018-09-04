#!/usr/bin/perl

use strict;
use warnings;

# read in the chunk files
my $file;
my %chunk = ();
for (my $i = 0; $i <= $#ARGV; $i++) {
	open($file, $ARGV[$i]);
	while (<$file>) {
		chomp($_);
		my @ax = split(/\t/, $_);
		my $id = shift(@ax);
		my $sc = shift(@ax);
		if (not exists $chunk{$id}) {
			$chunk{$id} = {};
		}
		if (not exists $chunk{$id}->{$sc}) {
			$chunk{$id}->{$sc} = {};
		}
		foreach (@ax) {
			$chunk{$id}->{$sc}->{$_} = 1;
		}
	}
	close($file);
}

# produce a single output, consisting of all of the chunks + captions for each node
foreach my $id (sort { $a <=> $b } keys %chunk) {
	foreach (sort keys %{$chunk{$id}}) {
		print "$id\t$_\t", join("\t", sort keys %{$chunk{$id}->{$_}}), "\n";
	}
}
