#!/usr/bin/perl

use strict;
use warnings;

my $file;

my %conll = ();

for (my $i = 0; $i <= $#ARGV; $i++) {
	my %first = ();
	open($file, $ARGV[$i]);
	while (<$file>) {
		chomp($_);
		my @ax = split(/\t/, $_);
		if ($#ax > 0) {
			my $id = shift(@ax);
			if (not exists $first{$id}) {
				$conll{$id} = ();
				$first{$id} = 1;
			}
			push(@{$conll{$id}}, join("\t", @ax));
		}
	}
	close($file);
}

foreach my $id (sort keys %conll) {
	foreach (@{$conll{$id}}) {
		print "$id\t$_\n";
	}
	print "\n";
}
