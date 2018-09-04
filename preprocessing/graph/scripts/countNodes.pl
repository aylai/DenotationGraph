#!/usr/bin/perl

# ./countNodes.pl <dir> <caption min> <cooccur min>

use strict;
use warnings;

# %imageNodes - for each image, the set of nodes it produces
# %c - image count for each node
my %imageNodes = ();
my %c = ();
open(my $file, "$ARGV[0]/node-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my %hx = ();

	foreach (@ax) {
		my @ay = split(/\#/, $_);
		$hx{$ay[0]} = 1;
	}
	@ax = keys %hx;
	my $n = scalar @ax;

	# ensure that the size of its visual denotation is above the threshold
	# otherwise, ignore the node
	if ($n >= $ARGV[1]) {
		$c{$id} = $n;
		foreach (@ax) {
			if (not exists $imageNodes{$_}) {
				$imageNodes{$_} = {};
			}
			$imageNodes{$_}->{$id} = 1;
		}
	}
}
close($file);

# generate co-occurrence counts
my %cooccur = ();
foreach (keys %imageNodes) {
	my @ax = sort { $b <=> $a } keys %{$imageNodes{$_}};
	for (my $i = 0; $i <= $#ax; $i++) {
		my $inode = $ax[$i];
		if (not exists $cooccur{$inode}) {
			$cooccur{$inode} = {};
		}

		for (my $j = $i + 1; $j <= $#ax; $j++) {
			my $jnode = $ax[$j];
			$cooccur{$inode}->{$jnode}++;
		}
	}
}

# print out counts
foreach my $x (keys %cooccur) {
	foreach my $y (keys %{$cooccur{$x}}) {
		# make sure the co-occurrence count is above the threshold
		if ($cooccur{$x}->{$y} >= $ARGV[2]) {
			print $x, "\t", $y, "\t", $c{$x}, "\t", $c{$y}, "\t", $cooccur{$x}->{$y}, "\n";
		}
	}
}
