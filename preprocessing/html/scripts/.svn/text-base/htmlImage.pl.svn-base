#!/usr/bin/perl

# htmlImage.pl <untoken> <graph> <html dir> <image location>

use strict;
use warnings;

my %img = ();

# get the original captions
my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	my @ax = split(/\t/, $_);
	my @ay = split(/\#/, $ax[0]);
	if (not exists $img{$ay[0]}) {
		$img{$ay[0]} = ();
	}

	push(@{$img{$ay[0]}}, $ax[1]);
}
close($file);

# get the strings
my %index = ();
open($file, "$ARGV[1]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$index{$ax[0]} = $ax[1];
}
close($file);

# get the node types
my %type = ();
open($file, "$ARGV[2]/node.type");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (($ax[1] & 1) != 0) {
		$type{$ax[0]} = $ax[1];
	}
}
close($file);

# for nodes without an HTML page, we want to find an appropriate
# parent node with an HTML page to point to instead.  We've
# prioritized the links, based on how much detail we lose by following
# them.  (For example, SENT links means our parent node will be an NP
# that shows up in the string - not terribly related).
my %edgeType = ();
$edgeType{"ADVP"} = 0;
$edgeType{"DROP"} = 0;
$edgeType{"NPART"} = 0;
$edgeType{"NPMOD"} = 0;
$edgeType{"ORIG"} = 0;
$edgeType{"RB"} = 0;

$edgeType{"NPHEAD"} = 1;
$edgeType{"ofY"} = 1;
$edgeType{"orY"} = 1;
$edgeType{"Xof"} = 1;
$edgeType{"Xor"} = 1;

$edgeType{"DRESS"} = 2;
$edgeType{"PP"} = 2;
$edgeType{"WEAR"} = 2;

$edgeType{"DOBJ"} = 3;
$edgeType{"TVERB"} = 3;
$edgeType{"toY"} = 3;
$edgeType{"Xto"} = 3;

$edgeType{"SUBJ"} = 4;
$edgeType{"VERB"} = 4;
$edgeType{"COMPLEX"} = 4;
$edgeType{"COMPLEX-VERB"} = 4;

$edgeType{"SENT"} = 5;

my $max = 0;
foreach (keys %edgeType) {
	if ($edgeType{$_} >= $max) {
		$max = $edgeType{$_} + 1;
	}
}

# get the parents
my %edge = ();
my %parent = ();
for (my $i = 0; $i <= $max; $i++) {
	open($file, "$ARGV[1]/node-tree.txt");
	while (<$file>) {
		chomp($_);
		my @ax = split(/\t/, $_);
		my @ay = split(/\//, $ax[1]);

		# check the type, make sure we're paying attention to it this round
		if ($i == $max && exists $edgeType{$ay[0]}) {
			next;
		} elsif (!exists $edgeType{$ay[0]} || $edgeType{$ay[0]} != $i) {
			next;
		}

		# if we've got a parent or an HTML page for the node, skip
		if (exists $parent{$ax[0]} || exists $type{$ax[0]}) {
		# if we've got an HTML page for the parent, use it as the parent of the node
		} elsif (exists $type{$ax[2]}) {
			$parent{$ax[0]} = $ax[2];
			delete $edge{$ax[0]};
		# otherwise store the edge
		} else {
			if (!exists $edge{$ax[0]}) {
				$edge{$ax[0]} = {};
			}
			$edge{$ax[0]}->{$ax[2]} = 1;
		}
	}
	close($file);
	
	# get the parents for the rest of the nodes without HTML pages we do
	# this by continually checking each node in %edge (i.e., those without
	# HTML pages or parent nodes) to see if any of their edges link them
	# to a node with a parent.  If so, we copy the parent, and remove the
	# node from %edge.
	my $c;
	do {
		$c = scalar keys %edge;
		
		foreach my $x (keys %edge) {
			foreach (keys %{$edge{$x}}) {
				if (exists $parent{$_}) {
					$parent{$x} = $parent{$_};
					delete $edge{$x};
					last;
				}
			}
		}
	} while ($c != scalar keys %edge);
}

# figure out which nodes were produced by which captions
my %cap = ();
my %c = ();
open($file, "$ARGV[1]/node-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my %hx = ();
	foreach (@ax) {
		my @ay = split(/\#/, $_);
		if (not exists $cap{$ay[0]}) {
			$cap{$ay[0]} = {};
		}
		$cap{$ay[0]}->{$id}++;
		$hx{$ay[0]} = 1;
	}
	$c{$id} = scalar keys %hx;
}
close($file);

mkdir("$ARGV[2]/image");

# make a page for each image
foreach my $x (keys %img) {
	my $f = $x;

	$f =~ s/\.jpg//g;
	$f =~ s/\./_/g;
	open($file, ">$ARGV[2]/image/$f.html");
	print $file "<html>\n";
	print $file "<body>\n";
	print $file "<table>\n";
	print $file "<tr>\n";
	print $file "<td><img src=\"$ARGV[3]/$x\"></td>\n";
	print $file "<td>\n";

	foreach (@{$img{$x}}) {
		print $file "<p>$_</p>\n";
	}

	print $file "</td>\n";
	print $file "</tr>\n";
	print $file "</table>\n";

	print $file "<table>\n";
	foreach (sort { $cap{$x}->{$b} <=> $cap{$x}->{$a} } keys %{$cap{$x}}) {
		if (exists $type{$_}) {
			print $file "<tr><td>$cap{$x}->{$_}</td><td><a href=\"../node/$_.html\">$index{$_}</a></td></tr>\n";
		} elsif (exists $parent{$_}) {
			print $file "<tr><td>$cap{$x}->{$_}</td><td>$index{$_} <a href=\"../node/$parent{$_}.html\">($index{$parent{$_}})</a></td></tr>\n";
		} else {
			print $file "<tr><td>$cap{$x}->{$_}</td><td>$index{$_}</td></tr>\n";
		}
	}
	print $file "</table>\n";

	print $file "</body>\n";
	print $file "</html>\n";
	close($file);
}
