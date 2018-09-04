#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin";
use simple;

# this script generates the NP sub-graph, which is a denotation graph with several limitations
# 1) instead of using captions, we will use EN chunks from captions
# 2) we will only use EN chunk related rewrite rules (NPMOD, NPHEAD, NPDET)
# 3) we will only pay attention to strings that already exist in EN chunks in the corpus

# %index maps plain text strings to indices
# %chunk maps plain text strings to chunked strings that can produce the plain text, and caption IDs
# %cap maps indices to caption IDs that produce that index/string
# %orig maps indices to caption IDs and full strings that produce that index/string
my %index = ();
my %chunk = ();
my %cap = ();
my %orig = ();

# this function updates the above structures given the information
# that a full string was produced by a given caption ID.
# arguments: full string, caption ID
sub processNode($$) {
	my $n = $_[0];
	my $id = $_[1];
	my $p = plain($n);
	my $c = chunk($n);

	# we should have pre-seeded %index, which means if we've managed
	# to produce an EN chunk not in %index, we're not interested in it
	if (not exists $index{$p}) {
		return -1;
	}

	# update %orig, %cap, and %chunk
	my $i = $index{$p};
	$orig{$i}->{"$id\t$n"} = 1;
	$cap{$i}->{$id} = 1;
    if (not exists $chunk{$i}->{$c}) {
		$chunk{$i}->{$c} = {};
	}
	$chunk{$i}->{$c}->{$id} = 1;
	return $i;
}

my $file;
my $i = 0;
my %oindex = ();
my %links = ();

my $t0 = time();
$| = 1;
my $c;

# check if we're extending a previous graph
if ($#ARGV >= 2 && $ARGV[2] ne "") {
	$c = 0;

	# grab the index of the previous graph
	open($file, "$ARGV[2]/node.idx");
	while (<$file>) {
		$c++;
		my $dt = time() - $t0;
		print "\r$c $dt";

		chomp($_);
		my @ax = split(/\t/, $_);
		$oindex{$ax[1]} = $ax[0];
		if ($i <= $ax[0]) {
			$i = $ax[0] + 1;
		}
	}
	close($file);
	print "\n";

	# grab the NP specific index of the previous graph
	# we'll consider these to be EN chunks that we've already seen,
	# for the purposes of which strings we can generate
	$c = 0;
	open($file, "$ARGV[2]/np.idx");
	while (<$file>) {
		$c++;
		my $dt = time() - $t0;
		print "\r$c $dt";

		chomp($_);
		my @ax = split(/\t/, $_);
		$index{$ax[1]} = $ax[0];
		$orig{$ax[0]} = {};
		$chunk{$ax[0]} = {};
		$cap{$ax[0]} = {};
	}
	close($file);
	print "\n";
}

# find the EN strings that we'll use - we'll grab the fully reduced
# form from trans06.txt and the originalish form from pre.id
# the intent is to avoid going from [ice] [hockey player] -> [ice] [player]
# after this, we'll run grab generation to determine how all the
# strings are connected
$c = 0;
open($file, "$ARGV[0]/trans.np");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	if ($#ax == 2) {
		$c++;
		my $dt = time() - $t0;
		print "\r$c $dt";

		my @ay = split(/ /, $ax[2]);
		my ($next, $prev) = breakSlash(\@ay, 1);
		for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
			if ($ay[$j]->[1] eq "[EN") {
				my @az = ();
				for (my $k = 0; $k < $next->[$j]; $k++) {
					push(@az, join("/", @{$ay[$j + $k]}));
				}

				my $s = plain(join(" ", @az));
				if (not exists $index{$s}) {
					if (exists $oindex{$s}) {
						my $x = $oindex{$s};
						$index{$s} = $x;
						$orig{$x} = {};
						$chunk{$x} = {};
						$cap{$x} = {};
					} else {
						$index{$s} = $i;
						$orig{$i} = {};
						$chunk{$i} = {};
						$cap{$i} = {};
						$i++;
					}
				}
			}
		}
	}
}
close($file);
print "\n";

$c = 0;
open($file, "$ARGV[0]/pre.final");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	if ($#ax == 2) {
		$c++;
		my $dt = time() - $t0;
		print "\r$c $dt";

		my @ay = split(/ /, $ax[2]);
		my ($next, $prev) = breakSlash(\@ay, 1);
		for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
			if ($ay[$j]->[1] eq "[EN") {
				my @az = ();
				for (my $k = 0; $k < $next->[$j]; $k++) {
					push(@az, join("/", @{$ay[$j + $k]}));
				}

				my $s = plain(join(" ", @az));
				if (not exists $index{$s}) {
					if (exists $oindex{$s}) {
						my $x = $oindex{$s};
						$index{$s} = $x;
						$orig{$x} = {};
						$chunk{$x} = {};
						$cap{$x} = {};
					} else {
						$index{$s} = $i;
						$orig{$i} = {};
						$chunk{$i} = {};
						$cap{$i} = {};
						$i++;
					}
				}
			}
		}
	}
}
close($file);
print "\n";

# for graph generation, we'll simply be determining which edges
# connect the strings/nodes we've already found.  For each, caption,
# we'll grab the EN chunks and try applying all of the rules to them
my @id = ();
my @dep = ();
my @X = ();
my @Y = ();
my @type = ();
my $n = 0;
$c = 0;
open($file, "$ARGV[0]/trans.np");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	# if this is a caption
	if ($#ax == 2) {
		$c++;
		my $dt = time() - $t0;
		print "\r$c $dt";

		# find the EN chunks
		my @ay = split(/ /, $ax[2]);
		my ($next, $prev) = breakSlash(\@ay, 1);
		for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
			if ($ay[$j]->[1] eq "[EN") {
				# grab only the EN chunk
				my @az = ();
				for (my $k = 0; $k < $next->[$j]; $k++) {
					push(@az, join("/", @{$ay[$j + $k]}));
				}

				# generate all possible strings using the rewrite rules
				my $x = generateSentences(join(" ", @az), join(" ", @az), \@dep, \@X, \@Y, \@type, $n, "ALL", 0);

				# return is a set of edges - grab the nodes on either end of the edge
				foreach my $s1 (keys %$x) {
                    my $s = processNode($s1, $ax[0]);
					if ($s == -1) {
						next;
					}

					foreach my $l (keys %{$x->{$s1}}) {
genSentLOOP:
						foreach my $t1 (keys %{$x->{$s1}->{$l}}) {
							my $t = processNode($t1, $ax[0]);
							if ($t == -1) {
								next;
							}

							# and grab the edge, if the nodes are not the same
							if ($t != $s) {
								# @al - list of rules used
								# $type - type of the first rule
								# @lid - list of rule IDs
								# @ltype - text string representing the link type
								my @al = split(/,/, $l);
								my $type = (split(/\//, $type[$al[0]]))[0];
								my @lid = ();
								my @ltype = ();
								
								push(@ltype, $type);
								
								# check that we have matching types - if not, abort.
								# we don't handle that atm.
								# also, build the link ID while we're at it
								# (first type, followed by /es)
								foreach (@al) {
									my @ax = split(/\//, $type[$_]);
									if ($type ne shift(@ax)) {
										next genSentLOOP;
									}
									
									push(@lid, $id[$_]);
									push(@ltype, @ax);
								}
								
								$type = join("/", @ltype);
								my $z;
								if ($type =~ /^\+(.*)$/) {
									$z = "$s\t$1\t$t";
								} elsif ($type =~ /^-(.*)$/) {
									$z = "$t\t$1\t$s";
								} else {
									next;
								}
								
								if (not exists $links{$z}) {
									$links{$z} = {};
								}
								
								@lid = sort { $a <=> $b } @lid;
								$links{$z}->{$ax[0] . "#" . join(",", @lid)} = 1;
							}
						}
					}
				}
			}
		}

		@id = ();
		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# if this is a rule, grab it
	} elsif ($#ax == 4) {
		if ($ax[0] == $n) {
			my @ay = split(/\//, $ax[3]);
			$id[$n] = $ax[0];
			$dep[$n] = $ax[1];
			$X[$n] = $ax[2];
			$Y[$n] = $ax[3];
			$type[$n] = $ax[4];
			$n++;
		}
	}
}
close($file);
print "\n";

# output the data structures
print "Sentence Index\n";
open($file, ">$ARGV[1]/np.idx");
foreach (sort { $index{$a} <=> $index{$b} } keys %index) {
	print $file "$index{$_}\t$_\n";
}
close($file);

print "Graph\n";
open($file, ">$ARGV[1]/np-tree.txt");
foreach (keys %links) {
	print $file "$_\t", join("\t", sort keys %{$links{$_}}), "\n";
}
close($file);

print "Caption\n";
open($file, ">$ARGV[1]/np-cap.map");
foreach (keys %cap) {
	if ((scalar keys %{$cap{$_}}) == 0) {
		delete $cap{$_};
	}
}
foreach (sort { $a <=> $b } keys %cap) {
	print $file "$_\t", join("\t", sort keys %{$cap{$_}}), "\n";
}
close($file);

print "Originals\n";
open($file, ">$ARGV[1]/np-orig.txt");
foreach (keys %orig) {
	if ((scalar keys %{$orig{$_}}) == 0) {
		delete $orig{$_};
	}
}
foreach (sort { $a <=> $b } keys %orig) {
	foreach my $id (keys %{$orig{$_}}) {
		print $file "$_\t$id\n";
	}
}
close($file);

print "Chunking\n";
open($file, ">$ARGV[1]/np-chunk.txt");
foreach my $x (sort { $a <=> $b } keys %chunk) {
	foreach (sort keys %{$chunk{$x}}) {
		print $file "$x\t$_\t", join("\t", sort keys %{$chunk{$x}->{$_}}), "\n";
	}
}
close($file);

