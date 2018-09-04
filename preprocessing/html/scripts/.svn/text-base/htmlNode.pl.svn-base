#!/usr/bin/perl

# ./makeHTML.pl <graph> <PMI sub-graph> <html dir> <untoken file> <image dir>

use strict;
use warnings;

sub printSubtree($$);
sub printSubtree2($$$);

my $file;

# get the strings
my %index = ();
open($file, "$ARGV[0]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$index{$ax[0]} = $ax[1];
}
close($file);

# get the counts and which captions produced a node
# counts will be displayed, captions will be used
# at the leaves to show which captions produced a node
my %c = ();
my %cap = ();
open($file, "$ARGV[0]/node-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my %hx = ();
	$cap{$id} = {};
	foreach (@ax) {
		$cap{$id}->{$_} = 1;
		my @ay = split(/\#/, $_);
		$hx{$ay[0]} = 1;
	}
	$c{$id} = scalar keys %hx;
}
close($file);

# get the node types
my %type = ();
open($file, "$ARGV[2]/node.type");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$type{$ax[0]} = $ax[1];
}
close($file);

# load the PMI stuff so we can display the mini-tables
my %cc = ();
open($file, "$ARGV[1]/node-image.cnt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if (not exists $cc{$ax[0]}) {
		$cc{$ax[0]} = {};
	}
	$cc{$ax[0]}->{$ax[1]} = $ax[4];

	if (not exists $cc{$ax[1]}) {
		$cc{$ax[1]} = {};
	}
	$cc{$ax[1]}->{$ax[0]} = $ax[4];
}
close($file);

my %pmi = ();
my %cpb = ();
open($file, "$ARGV[1]/node-image.pmi");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if (not exists $pmi{$ax[4]}) {
		$pmi{$ax[4]} = {};
		$cpb{$ax[4]} = {};
	}
	$pmi{$ax[4]}->{$ax[6]} = $ax[0];
	$cpb{$ax[4]}->{$ax[6]} = $ax[1];

	if (not exists $pmi{$ax[6]}) {
		$pmi{$ax[6]} = {};
		$cpb{$ax[6]} = {};
	}
	$pmi{$ax[6]}->{$ax[4]} = $ax[0];
	$cpb{$ax[6]}->{$ax[4]} = $ax[2];
}
close($file);

# get the untokenized captions
my %sent = ();
open($file, $ARGV[3]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$sent{$ax[0]} = $ax[1];
}
close($file);

# get the tree structure
my %link = ();
my %parents = ();
open($file, "$ARGV[0]/node-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if (exists $type{$ax[0]} && exists $type{$ax[2]}) {
		if (not exists $link{$ax[2]}) {
			$link{$ax[2]} = {};
		}
		if (not exists $link{$ax[2]}->{$ax[1]}) {
			$link{$ax[2]}->{$ax[1]} = {};
		}
		$link{$ax[2]}->{$ax[1]}->{$ax[0]} = 1;

		if (not exists $parents{$ax[0]}) {
			$parents{$ax[0]} = {};
		}
		if (not exists $parents{$ax[0]}->{$ax[2]}) {
			$parents{$ax[0]}->{$ax[2]}->{$ax[1]} = 1;
		}
	}
}
close($file);

# for each node, determine which node page it shows up in
# we do this by take each node page, and determining its contents
my %roots = ();
foreach my $x (keys %type) {
	if (($type{$x} & 1) == 0) {
		next;
	}

	my @queue = ();
	my %visit = ();
	push(@queue, $x);
	while ($#queue >= 0) {
		my $q = pop(@queue);
		foreach (keys %{$link{$q}}) {
			foreach (keys %{$link{$q}->{$_}}) {
				if (exists $type{$_} && ($type{$_} & 1) != 0) {
					next;
				}

				if (not exists $visit{$_}) {
					if (not exists $roots{$_}) {
						$roots{$_} = {};
					}
					$roots{$_}->{$x} = 1;

					$visit{$_} = 1;
					push(@queue, $_);
				}
			}
		}
	}
	$roots{$x} = {};
	$roots{$x}->{$x} = 1;
}

my $tIndex = 0;

# primary print function - prints out a node
# if it has no leaves, print out the captions that produced it
# if it has leaves, use printSubtree2 to print out the chidlren
sub printSubtree($$) {
	my $s = $_[0];
	my $t = $_[1];

	if (scalar keys %{$link{$s}} == 0) {
		print $file "<li>\n";
		print $file "<table>\n";
		if ($t ne "") {
			print $file "<tr><td>($t)</td><td>$index{$s} ($c{$s})</td></tr>\n";
		} else {
			print $file "<tr><td></td><td>$index{$s} ($c{$s})</td></tr>\n";
		}

		my $last = "";
		my $color = 1;
		foreach (sort keys %{$cap{$s}}) {
			my @ax = split(/\#/, $_);
			if ($ax[0] ne $last) {
				$last = $ax[0];
				$color = 1 - $color;
			}
			if ($color == 0) {
				print $file "<tr><td></td><td>$sent{$_}</td></tr>\n";
			} else {
				print $file "<tr><td></td><td><font color=\"red\">$sent{$_}</font></td></tr>\n";
			}
		}
		print $file "</table>\n";
		print $file "</li>\n";
		return;
	}

	if ($t ne "") {
		print $file "<li><label for=\"$tIndex\">($t) $index{$s} ($c{$s})</label>";
	} else {
		print $file "<li><label for=\"$tIndex\">$index{$s} ($c{$s})</label>";
	}
	print $file "<input type=\"checkbox\" id=\"$tIndex\" />\n";
	$tIndex++;

	printSubtree2($s, "", -1);

	print $file "</li>\n";
}

# print out the edge labels between nodes (and leaf nodes)
# this determines if it is worth grouping edge labels
# Uses slashes to get the type, sub-type, etc. of an edge label
# NPMOD/young = type: NPMOD, sub-type is young, function
# determine if there is an NPMOD node, or just an NPMOD/young node
# $prefix is the portion of the edge label we've used so far
# $nprefix is the number of slashes we've already used
sub printSubtree2($$$) {
	my $s = $_[0];
	my $prefix = $_[1];
	my $nprefix = $_[2];
	my %ntype = ();
	my %itype = ();

	# get all of the edge labels of this node
	foreach (keys %{$link{$s}}) {
		my @ax = split(/\//, $_);
		my $p = "";

		# if we're working on sub-types, grab the type
		if ($nprefix >= 0) {
			$p = join("/", @ax[0 .. $nprefix]);
		}

		# check if this edge is of the right type
		if ($p eq $prefix) {
			# count the number of children this particular sub-type has
			$ntype{$ax[$nprefix + 1]} += scalar keys %{$link{$s}->{$_}};

			# count the number of images that produce
			# the children of this particular sub-type
			if (not exists $itype{$ax[$nprefix + 1]}) {
				$itype{$ax[$nprefix + 1]} = {};
			}
			foreach my $t (keys %{$link{$s}->{$_}}) {
				foreach my $i (keys %{$cap{$t}}) {
					my @ay = split(/\#/, $i);
					$itype{$ax[$nprefix + 1]}->{$ay[0]} = 1;
				}
			}
		}
	}

	# do we actually have anything to print?
	if (scalar keys %ntype > 0) {
		# no prior type, start a new list
		if ($nprefix == -1) {
			print $file "<ol>\n";
		}
		# go through each sub-type
		foreach my $t (sort keys %ntype) {
			# if a sub-type has more than five children, it gets its own sub-tree
			if ($ntype{$t} > 5) {
				my $n = scalar keys %{$itype{$t}};
				print $file "<li><label for=\"$tIndex\">$t ($n)</label><input type=\"checkbox\" id=\"$tIndex\" />\n";
				print $file "<ol>\n";
				$tIndex++;
			}

			# get the children of this type/sub-type
			my %children = ();
			my $slash = -1;
			foreach my $l (keys %{$link{$s}}) {
				my @ax = split(/\//, $l);
				my $p = "";

				if ($nprefix >= 0) {
					$p = join("/", @ax[0 .. $nprefix]);
				}
				
				if ($p eq $prefix && $ax[$nprefix + 1] eq $t) {
					if ($slash == -1 || $slash > $#ax) {
						$slash = $#ax;
					}

					foreach (keys %{$link{$s}->{$l}}) {
						$children{$_} = $l;
					}
				}
			}

			# if there's more than 10 children and another possible sub-type, recurse
			if (scalar keys %children > 10 && $slash > ($nprefix + 2)) {
				if ($prefix eq "") {
					printSubtree2($s, $t, $nprefix + 1);
				} else {
					printSubtree2($s, "$prefix/$t", $nprefix + 1);
				}
			} else {
				# get the captions of the children that have no children themselves
				# we're grouping leaf nodes based on the captions that produced them
				my %caps = ();
				my %rcaps = ();
				foreach (keys %children) {
					if (scalar keys %{$link{$_}} == 0) {
						my $x = join(",", sort keys %{$cap{$_}});
						$caps{$_} = $x;
						if (not exists $rcaps{$x}) {
							$rcaps{$x} = {};
						}
						$rcaps{$x}->{$_} = 1
					}
				}

				my %done = ();
				foreach (sort { $c{$b} <=> $c{$a} } keys %children) {
					# have we already handled this node?
					if (exists $done{$_}) {
						next;
					}
					$done{$_} = 1;

					# leaf nodes - print out the leaf nodes produced by this set
					# of captions, and then the captions themselves
					if (exists $caps{$_}) {
						print $file "<li>\n";
						print $file "<table>\n";
						if ($children{$_} ne "") {
							print $file "<tr><td>($children{$_})</td><td>$index{$_} ($c{$_})</td></tr>\n";
						} else {
							print $file "<tr><td></td><td>$index{$_} ($c{$_})</td></tr>\n";
						}

						foreach my $x (sort { $c{$b} <=> $c{$a} } keys %{$rcaps{$caps{$_}}}) {
							if ($x != $_) {
								if ($children{$x} ne "") {
									print $file "<tr><td>($children{$x})</td><td>$index{$x} ($c{$x})</td></tr>\n";
								} else {
									print $file "<tr><td></td><td>$index{$x} ($c{$x})</td></tr>\n";
								}
							}
							$done{$x} = 1;
						}
						
						my $last = "";
						my $color = 1;
						foreach (sort keys %{$cap{$_}}) {
							my @ax = split(/\#/, $_);
							if ($ax[0] ne $last) {
								$last = $ax[0];
								$color = 1 - $color;
							}
							if ($color == 0) {
								print $file "<tr><td></td><td>$sent{$_}</td></tr>\n";
							} else {
								print $file "<tr><td></td><td><font color=\"red\">$sent{$_}</font></td></tr>\n";
							}
						}
						print $file "</table>\n";
						print $file "</li>\n";
					# there's another node page, print a link
					} elsif (exists $type{$_} && ($type{$_} & 1) != 0) {
						print $file "<li>($children{$_}) <a href=\"$_.html\">$index{$_} ($c{$_})</a></li>\n";
					# print out the subtree
					} else {
						printSubtree($_, $children{$_});
					}
				}
			}
			
			if ($ntype{$t} > 5) {
				print $file "</ol>\n";
				print $file "</li>\n";
			}
		}

		# get our children's captions
		my %ccap = ();
		foreach (keys %{$link{$s}}) {
			foreach my $t (keys %{$link{$s}->{$_}}) {
				foreach my $i (keys %{$cap{$t}}) {
					$ccap{$i} = 1;
				}
			}
		}

		# if we have captions that our children don't, print those out
		if ((scalar keys %ccap) != (scalar keys %{$cap{$s}})) {
			print $file "<li>\n";
			print $file "<table>\n";
			print $file "<tr><td>LEAF</td><td></td></tr>\n";
			
			my $last = "";
			my $color = 1;
			foreach (sort keys %{$cap{$s}}) {
				if (not exists $ccap{$_}) {
					my @ax = split(/\#/, $_);
					if ($ax[0] ne $last) {
						$last = $ax[0];
						$color = 1 - $color;
					}
					if ($color == 0) {
						print $file "<tr><td></td><td>$sent{$_}</td></tr>\n";
					} else {
						print $file "<tr><td></td><td><font color=\"red\">$sent{$_}</font></td></tr>\n";
					}
				}
			}
			print $file "</table>\n";
			print $file "</li>\n";
		}

		if ($nprefix == -1) {
			print $file "</ol>\n";
		}
	}
}

mkdir("$ARGV[2]/node");

# iterate through nodes with pages, and make the pages.
foreach my $x (keys %type) {
	if (($type{$x} & 1) == 0) {
		next;
	}

	open($file, ">$ARGV[2]/node/$x.html");
	print $file "<html>\n";
	print $file "<head>\n";
	print $file "<script src=\"../sorttable.js\"></script>\n";
	print $file "<!--[if gte IE 9 ]><link rel=\"stylesheet\" type=\"text/css\" href=\"../collapsetree.css\" media=\"screen\"><![endif]-->\n";
	print $file "<!--[if !IE]>--><link rel=\"stylesheet\" type=\"text/css\" href=\"../collapsetree.css\" media=\"screen\"><!--<![endif]-->\n";
	print $file "</head>\n";
	print $file "<body>\n";

	print $file "<h2>$index{$x} ($c{$x})</h2>\n";

	# print a max of 100 images
	my $img = 100;
	my %himages = ();
	foreach my $y (keys %{$cap{$x}}) {
		my @ax = split(/\#/, $y);
		$himages{$ax[0]} = 1;
	}
	my @aimages = keys %himages;
	while ($img > 0 && $#aimages >= 0) {
		my $n = int(rand($#aimages + 1));
		my $name = $aimages[$n];
		$name =~ s/\.jpg$//;
		$name =~ s/\./_/;
		print $file "<a href=\"../image/$name.html\"><img src=\"$ARGV[4]/$aimages[$n]\" width=\"100\"></a>\n";
		$aimages[$n] = $aimages[$#aimages];
		pop(@aimages);
		$img--;
	}

	# if it has a PMI page, print a five long PMI table, and link the PMI page
	if (($type{$x} & 2) != 0) {
		my $n = 0;
		my %hx = ();

		foreach (split(/ /, $index{$x})) {
			$hx{$_} = 1;
		}

		print $file "<br>\n";
		print $file "<br>\n";
		print $file "<h3><a href=\"../pmi/$x.html\">PMI</a></h3>\n";

		print $file "<table class=\"sortable\">\n";
		print $file "<tr><th style=\"padding:10px\">x</th><th style=\"padding:10px\">pmi(x, $index{$x})</th><th style=\"padding:10px\">p(x|$index{$x})</th><th style=\"padding:10px\">p($index{$x}|x)</th><th style=\"padding:10px\">c(x)</th><th style=\"padding:10px\">c(x, $index{$x})</th></tr>\n";
LOOPpmi:
		foreach my $y (sort { $pmi{$x}->{$b} <=> $pmi{$x}->{$a} } keys %{$cc{$x}}) {
			foreach (split(/ /, $index{$y})) {
				if (exists $hx{$_}) {
					next LOOPpmi;
				}
			}

			printf $file ("<tr><td style=\"text-align:left\"><a href=\"$y.html\">$index{$y}</a></td><td style=\"text-align:center\">%.3f</td><td style=\"text-align:center\">%.3f</td><td style=\"text-align:center\">%.3f</td><td style=\"text-align:center\">$c{$y}</td><td style=\"text-align:center\">$cc{$x}->{$y}</td></tr>\n", $pmi{$x}->{$y} + 0.0005, $cpb{$y}->{$x} + 0.0005, $cpb{$x}->{$y} + 0.0005);
			$n++;
			if ($n == 5) {
				last;
			}
		}
		print $file "</table>\n";

	}

	print $file "<br>\n";
	print $file "<br>\n";
	print $file "<h3>Parents</h3>\n";
	foreach my $y (keys %{$parents{$x}}) {
		my @ax = keys %{$roots{$y}};
		if (scalar @ax > 1) {
			print $file "$index{$y} (", join(",", keys %{$parents{$x}->{$y}}), ")";
			foreach (@ax) {
				print $file " <a href=\"$_.html\">$index{$_}</a>";
			}
			print $file "<br>\n";
		} else {
			print $file "<a href=\"$ax[0].html\">$index{$y} (", join(",", keys %{$parents{$x}->{$y}}), ")</a><br>\n";
		}
	}


	print $file "<br>\n";
	print $file "<br>\n";
	print $file "<h3>Children</h3>\n";
	print $file "<ol class=\"tree\">\n";
	$tIndex = 0;
	printSubtree($x, "");
	print $file "</ol>\n";

	print $file "</body>\n";
	print $file "</html>\n";
}

