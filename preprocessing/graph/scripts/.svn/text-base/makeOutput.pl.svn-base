#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin";
use simple;

# the script generates the main denotation graph

# our primary strategy will be this: we will attempt to only generate
# those string produced by multiple captions.

# all rewrite rules are either expanding (+) or extracting (-)

# any time we add a string to the denotation graph, we will run all
# possible extracting rewrite rules on it - extracted strings will be
# smaller/less detailed, and thus we can't tell ahead of time what's
# going to generate it.  We will only run the expanding rules if
# multiple captions generate the string - the children will be more
# specific, and thus any caption that generates a more specific
# version of the string should generate the string itself.  Thus, the
# child strings can have multiple captions producing it iff there are
# multiple caption producing the string.

my $file;

# %index - map of plain text strings to indices
# %np - map of plain text EN chunks to indices (from the NP sub-graph)
# %sent - map of NP indices to captions that contain them
# %links - edges between nodes
# %chunk - map of indices to chunked strings
my $i = 0;
my %index = ();
my %np = ();
my %sent = ();
my %links = ();
my %chunk = ();

# since our strategy requires us to occassionally pause processing of
# a caption, these structures are used to hold the state.  We can only
# have at most one caption paused for a given node, however.  (May be
# advantageous to change that in the future).

# %graphsent - for a given index, which caption ID are we holding here
# %graphstate - for a given index, what's the full string that we are holding here
# %graphvisit - for a given index, which caption + full strings have we visited this node with
#               (we're actually using a token sequence instead of full string, but given the caption
#               that's equivalent to the full string)
my %graphsent = ();
my %graphstate = ();
my %graphvisit = ();

# check if we're extending a previous graph - if so, load the caption maps, and the index file
if ($#ARGV >= 2 && $ARGV[2] ne "") {
	my %hx = ();
	open($file, "$ARGV[2]/node-cap.map");
	while (<$file>) {
		chomp($_);
		my @ax = split(/\t/, $_);
		print "\r$ax[0]";
		if ($#ax > 0) {
			$hx{$ax[0]} = 1;
		}
	}
	close($file);
	print "\n";

	open($file, "$ARGV[2]/node.idx");
	while (<$file>) {
		chomp($_);
		my @ax = split(/\t/, $_);
		print "\r$ax[0]";
		if ($#ax == 0) {
			$ax[1] = "";
		}
		$index{$ax[1]} = $ax[0];
		$sent{$ax[0]} = {};
		if ($i <= $ax[0]) {
			$i = $ax[0] + 1;
		}
		if (exists $hx{$ax[0]}) {
			$graphvisit{$ax[1]} = {};
		}
	}
	close($file);
	print "\n";
}

# load the data structures from the NP and VP sub-graphs
# (VP index has all the strings produced thus far)
open($file, "$ARGV[1]/vp.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 0) {
		$ax[1] = "";
	}
	$index{$ax[1]} = $ax[0];
	$sent{$ax[0]} = {};
	if ($i <= $ax[0]) {
		$i = $ax[0] + 1;
	}
}
close($file);

# VP and NP tree contain different edges
open($file, "$ARGV[1]/vp-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $lid = shift(@ax) . "\t" . shift(@ax) . "\t" . shift(@ax);
	$links{$lid} = {};
	foreach (@ax) {
		$links{$lid}->{$_} = 1;
	}
}
close($file);

open($file, "$ARGV[1]/np-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $lid = shift(@ax) . "\t" . shift(@ax) . "\t" . shift(@ax);
	$links{$lid} = {};
	foreach (@ax) {
		$links{$lid}->{$_} = 1;
	}
}
close($file);

# VP and NP caption maps contain different nodes/caption IDs
open($file, "$ARGV[1]/vp-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	$sent{$id} = {};
	foreach (@ax) {
		$sent{$id}->{$_} = 1;
	}
}
close($file);

open($file, "$ARGV[1]/np-cap.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	$sent{$id} = {};
	foreach (@ax) {
		$sent{$id}->{$_} = 1;
	}
}
close($file);

# we load the NP index so we know what all the NPs are, so we can make SENT edges
open($file, "$ARGV[1]/np.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$np{$ax[1]} = $ax[0];
}
close($file);

my $t0 = time();
$| = 1;
my $c = 0;

# we divide the rules up into the positive and negative groups,
# depending on the sign in their label
my @pid = ();
my @pdep = ();
my @pX = ();
my @pY = ();
my @ptype = ();
my $pn = 0;

my @nid = ();
my @ndep = ();
my @nX = ();
my @nY = ();
my @ntype = ();
my $nn = 0;

# we're going to store all of the rules for each caption - any caption
# can get paused, and we'll need their rules when we resume expanding them
my %gnid = ();
my %gndep = ();
my %gnX = ();
my %gnY = ();
my %gntype = ();
my %gnn = ();

my %gpid = ();
my %gpdep = ();
my %gpX = ();
my %gpY = ();
my %gptype = ();
my %gpn = ();

my %gorig = ();

# %nps - list of EN chunks in this node/string
# %child - stores the tree structure
my %nps = ();
my %child = ();

# %done - mark down which caption + full strings have already been expanded
my %done = ();

# generate the graph
open($file, "$ARGV[1]/initial.rewrite");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	# we have a caption
	if ($#ax == 2) {
		$c++;
#		if (($c % 1000) == 0) {
			my $dt = time() - $t0;
			print "\r$c $dt";
#		}

		# store the rules for this caption - both negative and positive
		$gorig{$ax[0]} = $ax[2];
		$done{$ax[0]} = {};

		$gnn{$ax[0]} = $nn;
		$gnid{$ax[0]} = ();
		$gndep{$ax[0]} = ();
		$gnX{$ax[0]} = ();
		$gnY{$ax[0]} = ();
		$gntype{$ax[0]} = ();
		for (my $j = 0; $j < $nn; $j++) {
			$gnid{$ax[0]}->[$j] = $nid[$j];
			$gndep{$ax[0]}->[$j] = $ndep[$j];
			$gnX{$ax[0]}->[$j] = $nX[$j];
			$gnY{$ax[0]}->[$j] = $nY[$j];
			$gntype{$ax[0]}->[$j] = $ntype[$j];
		}

		$gpn{$ax[0]} = $pn;
		$gpid{$ax[0]} = ();
		$gpdep{$ax[0]} = ();
		$gpX{$ax[0]} = ();
		$gpY{$ax[0]} = ();
		$gptype{$ax[0]} = ();
		for (my $j = 0; $j < $pn; $j++) {
			$gpid{$ax[0]}->[$j] = $pid[$j];
			$gpdep{$ax[0]}->[$j] = $pdep[$j];
			$gpX{$ax[0]}->[$j] = $pX[$j];
			$gpY{$ax[0]}->[$j] = $pY[$j];
			$gptype{$ax[0]}->[$j] = $ptype[$j];
		}

		# we use a work queue - basically, any calls to
		# generateSentences will get queued, we'll go through the
		# queue one by one, and then determine if the results merit
		# further calls to generateSentences

		# there are two types of calls we put on the queue
		# negative rewrite rules use a full expansion - the @n... group, and a 0 limit.
		# positive rewrite rules use a one step-expansion - the @p... group, and a -1 limit

		# a queue item consists of string, rewrite rules (IDs, dependency, left side, right side, labels, count), expansion limits, and caption ID
		my @queue = ();
		push(@queue, [ $ax[2], \@nid, \@ndep, \@nX, \@nY, \@ntype, $nn, 0, $ax[0] ]);
#		push(@queue, [ $ax[2], \@pid, \@pdep, \@pX, \@pY, \@ptype, $pn, 0, $ax[0] ]);

		while ($#queue >= 0) {
			my $q = shift(@queue);
			my $id = $q->[8];
			my $ids = ids($q->[0]) . " " . $q->[7];

			# check if we've already done this expansion - use $ids (token sequence) to uniquely identify what full string we're dealing with
			# used to use the plain text version - some captions have the same plain text multiple times
			if (exists $done{$id}->{$ids}) {
				next;
			} else {
				$done{$id}->{$ids} = 1;
			}

			# generate the sentence
			my $x = generateSentences($q->[0], $gorig{$id}, $q->[2], $q->[3], $q->[4], $q->[5], $q->[6], "ALL", $q->[7]);

			# process all the source nodes in the returned edges
			foreach my $k (keys %{$x}) {
				my $s = plain($k);
				my $sc = chunk($k);
				my $sids = $id . "," . ids($k);

				# create data structures for this nide
				if (not exists $index{$s}) {
					$index{$s} = $i;
					$sent{$i} = {};
					$nps{$i} = {};
					$child{$i} = {};
					$chunk{$i} = {};
					$i++;
				}

				# see if we've ever generated this string before
				if (not exists $graphvisit{$s}) {
					$graphvisit{$s} = {};

					# if not, note down that this node and full string have reached the node
					# next time someone else reaches the node, we'll need to expand it
					$graphsent{$s} = $id;
					$graphstate{$s} = $k;

					# if this was a positive rewrite rule expansion, run a negative rewrite rule expansion as well
					if ($q->[7] == -1) {
						push(@queue, [ $k, $gnid{$id}, $gndep{$id}, $gnX{$id}, $gnY{$id}, $gntype{$id}, $gnn{$id}, 0, $id ]);
					}
				# otherwise, check if we've visited generated this node before with this particular caption and full string
				} elsif (not exists $graphvisit{$s}->{$sids}) {
					# we'll need to do a positive rewrite rule expansion, since there's some other way to reach here
					# and the visual denotation of this string could be larger than one
					push(@queue, [ $k, $gpid{$id}, $gpdep{$id}, $gpX{$id}, $gpY{$id}, $gptype{$id}, $gpn{$id}, -1, $id ]);
					# additionally, if we got here via positive rewrite rule expansion, we need to do a negative rewrite rule expansion
					if ($q->[7] == -1) {
						push(@queue, [ $k, $gnid{$id}, $gndep{$id}, $gnX{$id}, $gnY{$id}, $gntype{$id}, $gnn{$id}, 0, $id ]);
					}
					# finally, check if we have a stored caption/full string that also needs to be expanded
					if (exists $graphsent{$s}) {
						my $idStore = $graphsent{$s};
						push(@queue, [ $graphstate{$s}, $gpid{$idStore}, $gpdep{$idStore}, $gpX{$idStore}, $gpY{$idStore}, $gptype{$idStore}, $gpn{$idStore}, -1, $idStore ]);
						delete $graphsent{$s};
						delete $graphstate{$s};
					}
				}
				# note down that we've visited this node
				$graphvisit{$s}->{$sids} = 1;

				# note down that the caption can produce this string
				$s = $index{$s};
				$sent{$s}->{$id} = 1;

				# also, store that a particular chunking produces this string
				if (not exists $chunk{$s}->{$sc}) {
					$chunk{$s}->{$sc} = {};
				}
				$chunk{$s}->{$sc}->{$id} = 1;

				# look for EN chunks - if they're in the NP sub-graph, we may want to add SENT links from the NP sub-graph to this node
				# (in order to avoid SENT link spam, we'll only link to the oldest nodes that contain the appropriate EN chunk)
				my @ay = split(/ /, $k);
				my ($next, $prev) = breakSlash(\@ay, 1);
				for (my $j = 0; $j <= $#ay; $j++) {
					if ($ay[$j]->[1] eq "[EN") {
						my @az = ();
						for (my $k = 0; $k < $next->[$j]; $k++) {
							push(@az, join("/", @{$ay[$j + $k]}));
						}

						my $e = plain(join(" ", @az));
						if (exists $np{$e}) {
							$e = $np{$e};
							if ($e != $s) {
								$nps{$s}->{$e} = 1;
								$sent{$e}->{$id} = 1;
							}
						}
					}
				}

				# process the edges and destination nodes of the source node
				foreach my $l (keys %{$x->{$k}}) {
					foreach (keys %{$x->{$k}->{$l}}) {
						my $t = plain($_);
						my $tc = chunk($_);
						my $tids = $id . "," . ids($_);

						# same as before - create an entry for the node if one doesn't exist
						if (not exists $index{$t}) {
							$index{$t} = $i;
							$sent{$i} = {};
							$nps{$i} = {};
							$child{$i} = {};
							$chunk{$i} = {};
							$i++;
						}

						# if this is an entirely new node, pause, since the visual denotation is 1, so far
						if (not exists $graphvisit{$t}) {
							$graphvisit{$t} = {};
							
							$graphsent{$t} = $id;
							$graphstate{$t} = $_;

							# run the negative rewrite rule expansion, if this was a positive rewrite rule expansion
							# (if this was a negative rewrite rule expansion, we've already used all the negative rewrite rules)
							if ($q->[7] == -1) {
								push(@queue, [ $_, $gnid{$id}, $gndep{$id}, $gnX{$id}, $gnY{$id}, $gntype{$id}, $gnn{$id}, 0, $id ]);
							}
						# if this is an old node, but we haven't visited it with this caption/full string
						} elsif (not exists $graphvisit{$t}->{$tids}) {
							# we're a string generated by applying a rewrite rule, and we've encounted an occupied node
							# so we need to perform a positive rewrite rule expansion
							push(@queue, [ $_, $gpid{$id}, $gpdep{$id}, $gpX{$id}, $gpY{$id}, $gptype{$id}, $gpn{$id}, -1, $id ]);

							# furthermore if we were produced by a positive rewrite rule expansion,
							# we need to perform a negative rewrite rule expansion
							# (if we were a negative rewrite rule expansion, we've already used all the negative rewrite rules)
							if ($q->[7] == -1) {
								push(@queue, [ $_, $gnid{$id}, $gndep{$id}, $gnX{$id}, $gnY{$id}, $gntype{$id}, $gnn{$id}, 0, $id ]);
							}

							# finally, if there was a "paused" caption at this node, continue its processing
							if (exists $graphsent{$t}) {
								my $idStore = $graphsent{$t};
								push(@queue, [ $graphstate{$t}, $gpid{$idStore}, $gpdep{$idStore}, $gpX{$idStore}, $gpY{$idStore}, $gptype{$idStore}, $gpn{$idStore}, -1, $idStore ]);
								delete $graphsent{$t};
								delete $graphstate{$t};
							}
						}
						# note down that we've visited this node
						$graphvisit{$t}->{$tids} = 1;

						# store caption production information
						$t = $index{$t};
						$sent{$t}->{$id} = 1;

						# store chunking information
						if (not exists $chunk{$t}->{$tc}) {
							$chunk{$t}->{$tc} = {};
						}
						$chunk{$t}->{$tc}->{$id} = 1;

						# find the EN chunks - note them for possible SENT links into the NP sub-graph
						my @ay = split(/ /, $_);
						my ($next, $prev) = breakSlash(\@ay, 1);
						for (my $j = 0; $j <= $#ay; $j++) {
							if ($ay[$j]->[1] eq "[EN") {
								my @az = ();
								for (my $k = 0; $k < $next->[$j]; $k++) {
									push(@az, join("/", @{$ay[$j + $k]}));
								}
								
								my $e = plain(join(" ", @az));
								if (exists $np{$e}) {
									$e = $np{$e};
									if ($e != $t) {
										$nps{$t}->{$e} = 1;
										$sent{$e}->{$id} = 1;
									}
								}
							}
						}

						# store the edge
						if ($t != $s) {
							# @al - list of rules used
							# $type - type of the first rule
							# @lid - list of rule IDs
							# @ltype - text string representing the link type
							my @al = split(/,/, $l);
							my $type = (split(/\//, $q->[5]->[$al[0]]))[0];
							my @lid = ();
							my @ltype = ();
							
							push(@ltype, $type);
							
							# check that we have matching types - if not, abort.
							# we don't handle that atm.
							# also, build the link ID while we're at it
							# (first type, followed by /es)
							foreach (@al) {
								my @ax = split(/\//, $q->[5]->[$_]);
								if ($type ne shift(@ax)) {
									next genSentLOOP;
								}
								
								push(@lid, $q->[1]->[$_]);
								push(@ltype, @ax);
							}
							
							$type = join("/", @ltype);
							my $z;
							if ($type =~ /^\+(.*)$/) {
								$z = "$s\t$1\t$t";
								$child{$t}->{$s} = 1;
							} elsif ($type =~ /^-(.*)$/) {
								$z = "$t\t$1\t$s";
								$child{$s}->{$t} = 1;
							} else {
								next;
							}
							
							if (not exists $links{$z}) {
								$links{$z} = {};
							}

							@lid = sort { $a <=> $b } @lid;
							$links{$z}->{$id . "#" . join(",", @lid)} = 1;
						}
					}
				}
			}
		}

		@pid = ();
		@pdep = ();
		@pX = ();
		@pY = ();
		@ptype = ();
		$pn = 0;

		@nid = ();
		@ndep = ();
		@nX = ();
		@nY = ();
		@ntype = ();
		$nn = 0;
	# we've got a rewrite rule - determine if it's positive or negative, and store it in the correct group
	} elsif ($#ax == 4) {
		if ($ax[4] =~ /^\+/) { # && $ax[2] =~ /^B.*E$/) {
			$pid[$pn] = $ax[0];
			$pdep[$pn] = $ax[1];
			$pX[$pn] = $ax[2];
			$pY[$pn] = $ax[3];
			$ptype[$pn] = $ax[4];
			$pn++;
		} elsif ($ax[4] =~ /^\-/) {
			$nid[$nn] = $ax[0];
			$ndep[$nn] = $ax[1];
			$nX[$nn] = $ax[2];
			$nY[$nn] = $ax[3];
			$ntype[$nn] = $ax[4];
			$nn++;
		}
	}
}
close($file);
print "\n";

# at this point, for each node in the NP sub-graph, we have a list of
# nodes (%nps) that contain them as EN chunks.  We're going to find
# the oldest of those nodes and add SENT links (this avoids spamming
# the graph with SENT links, while still having useful SENT links).
print "Adding entity links\n";

# %entities is the set of entities that a node or any of its ancestor has a SENT link to
# %state is the number of unvisited parents that a node has
my %entities = ();
my %state = ();
foreach (keys %index) {
	$state{$index{$_}} = 0;
	$entities{$index{$_}} = {};
}

# count the parents
foreach my $i (keys %child) {
	foreach (keys %{$child{$i}}) {
		$state{$_}++;
	}
}

# iterate through the states with 0 unvisited parents
# figure out what new SENT links need to be added
# update the list of entities the node or its ancestors has a SENT link to
# pass the list down to its children, and subtract 1 from their number of unvisited parents
my $changed;
while (scalar keys %state > 0) {
	$changed = 0;

	foreach my $i (keys %state) {
		# if all my parents have been visited
		if ($state{$i} == 0) {
			# remove me from consideration, note that something's changed
			delete $state{$i};
			$changed++;

			# go through the list of my entity mentions
			foreach my $j (keys %{$nps{$i}}) {
				# if none of my ancestors has a SENT link to the entity mention
				if (not exists $entities{$i}->{$j}) {
					# add the link
					my $lid = "$i\tSENT\t$j";
					if (not exists $links{$lid}) {
						$links{$lid} = {};
					}
					foreach (keys %{$sent{$i}}) {
						$links{$lid}->{$_} = 1;
					}
				}
			}

			# go through my children
			foreach my $c (keys %{$child{$i}}) {
				# indicate that one of their parents (me) has been visited
				$state{$c}--;
				# also, add my entity mentions and my ancestors entity mentions to their list of entity mentions
				foreach (keys %{$nps{$i}}) {
					$entities{$c}->{$_} = 1;
				}
				foreach (keys %{$entities{$i}}) {
					$entities{$c}->{$_} = 1;
				}
			}
		}
	}

	# if we have nodes with parents, and nothing's changed - we have a loop and need to abort
	if ($changed == 0) {
		print "Graph has a loop\n";
		last;
	}
}

# output data structures
print "Node Index\n";
open($file, ">$ARGV[0]/expand.idx");
foreach (sort { $index{$a} <=> $index{$b} } keys %index) {
	print $file "$index{$_}\t$_\n";
}
close($file);

print "Node-Caption Map\n";
open($file, ">$ARGV[0]/expand-cap.map");
foreach (sort { $a <=> $b } keys %sent) {
	print $file "$_\t", join("\t", sort keys %{$sent{$_}}), "\n";
}
close($file);

print "Graph\n";
open($file, ">$ARGV[0]/expand-tree.txt");
foreach (keys %links) {
	print $file "$_\t", join("\t", sort keys %{$links{$_}}), "\n";
}
close($file);

print "Chunking\n";
open($file, ">$ARGV[0]/expand-chunk.txt");
foreach my $x (sort { $a <=> $b } keys %chunk) {
	foreach (sort keys %{$chunk{$x}}) {
		print $file "$x\t$_\t", join("\t", sort keys %{$chunk{$x}->{$_}}), "\n";
	}
}
close($file);
