#!/usr/bin/perl

# ./pickNodes.pl <graph> <PMI sub-graph> <max size> <min size> [restricted word list]

use strict;
use warnings;

my $file;

# restricted words - skip any node that contains one
my %restrict = ();
if ($#ARGV >= 4) {
	open($file, $ARGV[4]);
	while (<$file>) {
		chomp($_);
		$restrict{$_} = 1;
	}
	close($file);
}

# track which nodes will have which pages
# 1 means has a node page
# 2 means has a PMI page
my %type = ();
open($file, "$ARGV[0]/node.idx");
LOOPidx:
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	foreach (@ax) {
		if (exists $restrict{$_}) {
			next LOOPidx;
		}
	}
	$type{$ax[0]} = 0;
}
close($file);

open($file, "$ARGV[1]/node-image.cnt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (exists $type{$ax[0]}) {
		$type{$ax[0]} |= 3;
	}
	if (exists $type{$ax[1]}) {
		$type{$ax[1]} |= 3;
	}
}
close($file);

# read the tree, find roots
my %link = ();
my %notroot = ();
my %parents = ();
open ($file, "$ARGV[0]/node-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if (!exists $type{$ax[2]} || !exists $type{$ax[0]}) {
		next;
	}

	if (not exists $link{$ax[2]}) {
		$link{$ax[2]} = {};
	}
	$link{$ax[2]}->{$ax[0]} = 1;

	$notroot{$ax[0]} = 1;

	if (not exists $parents{$ax[0]}) {
		$parents{$ax[0]} = {};
	}
	$parents{$ax[0]}->{$ax[2]} = 1;
}
close($file);

# roots get to be node pages
foreach (keys %type) {
	if (not exists $notroot{$_}) {
		$type{$_} |= 1;
	}
}

# get image counts, and remove node with an image count of one, 
# and whose parents all have an image count of one.
# Then, remove edges involving removed nodes
my %c = ();
open($file, "$ARGV[0]/node-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my %hx = ();
	foreach (@ax) {
		my @ay = split(/\#/, $_);
		$hx{$ay[0]} = 1;
	}
	$c{$id} = scalar keys %hx;
}
close($file);

LOOPrem:
foreach my $x (keys %c) {
	if ($c{$x} == 1 && $type{$x} == 0) {
		foreach (keys %{$parents{$x}}) {
			if ($c{$_} != 1) {
				next LOOPrem;
			}
		}

		delete $type{$x};
		delete $link{$x};
	}
}

foreach my $x (keys %link) {
	foreach (keys %{$link{$x}}) {
		if (not exists $type{$_}) {
			delete $link{$x}->{$_};
		}
	}

	if (scalar keys %{$link{$x}} == 0) {
		delete $link{$x};
	}
}

# initialize size, and children/parent counts
my %size = ();
my %cchild = ();
my %cparent = ();
foreach (keys %type) {
	$cchild{$_} = {};
	$cparent{$_} = {};
}

foreach my $x (keys %type) {
	if (exists $link{$x}) {
		$size{$x} = (scalar keys %{$link{$x}}) + 1;
	} else {
		$size{$x} = 1;
	}

	$cchild{$x} = {};
	foreach (keys %{$link{$x}}) {
		$cchild{$x}->{$_} = 1;
		$cparent{$_}->{$x} = 1;
	}
}

# anything that's not already a root is potentially a root
my %potential = ();
my $n = 0;
foreach (keys %type) {
	if (($type{$_} & 1) == 0) {
		$potential{$_} = 1;
	} else {
		$n++;
	}
}

# loop through the potentials, starting from the smallest
# see if hooking them up to their parents forms a small enough page,
# or if a potential is too small to exist on its own
do {
	my %changed = ();
	my $smallest = -1;
LOOPjoin:
	foreach my $x (sort { $size{$a} <=> $size{$b} } keys %potential) {
		# if we've changed the size of this, skip
		if (exists $changed{$x}) {
			next;
		}

		# if we're dealing with nodes larger than something we've changed
		# re-sort the array
		if ($smallest != -1 && $size{$x} > $smallest) {
			last;
		}

		delete $potential{$x};

		if ($size{$x} >= $ARGV[3]) {
			foreach (keys %{$cparent{$x}}) {
				# check if any of the resulting pages formed by
				# not making this node into its own page is too large
				if (($size{$_} + ($cparent{$x}->{$_} * ($size{$x} - 1))) > $ARGV[2]) {
					$type{$x} |= 1;
					$n++;
					next LOOPjoin;
				}
			}
		}

		# update parent/child counts and sizes
		foreach my $y (keys %{$cparent{$x}}) {
			$size{$y} += $cparent{$x}->{$y} * ($size{$x} - 1);
			$changed{$y} = 1;
			if ($smallest == -1 || $size{$y} < $smallest) {
				$smallest = $size{$y};
			}

			foreach (keys %{$cchild{$x}}) {
				$cchild{$y}->{$_} += $cchild{$x}->{$_} * $cchild{$y}->{$x};
				$cparent{$_}->{$y} = $cchild{$y}->{$_};
				delete $cparent{$_}->{$x};
			}
			delete $cchild{$y}->{$x};
		}
	}
} while (scalar keys %potential != 0);

foreach (sort { $a <=> $b } keys %type) {
	print "$_\t$type{$_}\n";
}
