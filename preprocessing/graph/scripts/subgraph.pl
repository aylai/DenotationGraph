#!/usr/bin/perl

# ./subgraph.pl <src> <img> <dest>

use strict;
use warnings;

my $file;
my $out;

my %img = ();

# read the set of images in the denotation graph
open($file, "$ARGV[0]/img.lst");
while (<$file>) {
	chomp($_);
	$img{$_} = 0;
}
close($file);

# grab the images in this subgraph that are also in the denotation graph
open($file, $ARGV[1]);
while (<$file>) {
	chomp($_);
	if (exists $img{$_}) {
		$img{$_} = 1;
	}
}
close($file);

# remove the other images from the hash
foreach (keys %img) {
	if ($img{$_} == 0) {
		delete $img{$_};
	}
}

# make the subgraph directory
mkdir($ARGV[2]);

# create the new token file
open($file, "$ARGV[0]/token.txt");
open($out, ">$ARGV[2]/token.txt");
while (<$file>) {
	my @ax = split(/\t/, $_);
	my @ay = split(/\#/, $ax[0]);
	if (exists $img{$ay[0]}) {
		print $out $_;
	}
}
close($out);
close($file);

# create the image list
open($out, ">$ARGV[2]/img.lst");
foreach (sort keys %img) {
	print $out "$_\n";
}
close($out);

# caption to node map is easy - only use captions of images we're keeping
# generate the list of nodes to keep during this phase
my %node = ();
open($file, "$ARGV[0]/cap-node.map");
open($out, ">$ARGV[2]/cap-node.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/\#/, shift(@ax));
	if (exists $img{$ay[0]}) {
		print $out "$_\n";
		foreach (@ax) {
			$node{$_} = 1;
		}
	}
}
close($out);
close($file);

# node to caption map is harder - make sure it's a node we're keeping
# and then remove any captions for images that we aren't keeping
open($file, "$ARGV[0]/node-cap.map");
open($out, ">$ARGV[2]/node-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	if (exists $node{$id}) {
		print $out "$id";
		foreach (@ax) {
			my @ay = split(/\#/, $_);
			if (exists $img{$ay[0]}) {
				print $out "\t$_";
			}
		}
		print $out "\n";
	}
}
close($out);
close($file);

# node index - only keep the node indices of the nodes that we're keeping
open($file, "$ARGV[0]/node.idx");
open($out, ">$ARGV[2]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (exists $node{shift(@ax)}) {
		print $out "$_\n";
	}
}
close($out);
close($file);

# type-chunk.txt is basically the node-caption map, with an additional field
# for the chunk information - handle it the same way as the node-caption map
open($file, "$ARGV[0]/type-chunk.txt");
open($out, ">$ARGV[2]/type-chunk.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	if (exists $node{$id}) {
		print $out "$id\t", shift(@ax), "\t", shift(@ax);
		foreach (@ax) {
			my @ay = split(/\#/, $_);
			if (exists $img{$ay[0]}) {
				print $out "\t$_";
			}
		}
		print $out "\n";
	}
}
close($out);
close($file);

# edge file - keep edges that link nodes that we're keeping, and only save
# the captions of those edges if they're for an image we're keeping.
open($file, "$ARGV[0]/node-tree.txt");
open($out, ">$ARGV[2]/node-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $x = shift(@ax);
	my $y = shift(@ax);
	my $z = shift(@ax);
	if (exists $node{$x} && exists $node{$z}) {
		print $out "$x\t$y\t$z";
		foreach (@ax) {
			my @ay = split(/\#/, $_);
			if (exists $img{$ay[0]}) {
				print $out "\t$_";
			}
		}
		print $out "\n";
	}
}
close($out);
close($file);


