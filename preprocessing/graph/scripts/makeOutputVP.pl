#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin";
use simple;

# generate the VP sub-graph.  The process is we will extract VPs from
# the SVO triples (similar to the way splitSubjVerb.pl does it - i.e.,
# the VP/SVO set of functions in simple.pm), and then use the modifier
# dropping rewrite rules and the verb/direct object extraction rewrite
# rules

# %oindex - full index of the previous graph (if one is used)
# %index - our string/node to index map
# %cap - map from index to caption IDs that produce the string/node
# %orig - map from index to caption IDs + full strings that produce the string/node
# %chunk - map from index to chunks that produce the string/node
# %link - edges between nodes (nodes are represented with indices)
my %oindex = ();
my %index = ();
my %cap = ();
my %orig = ();
my %chunk = ();
my $i = 0;
my %links = ();

# given a node and caption ID, update the above data structures.
# also, perform a verb/direct object split if needed
# arguments: node/string, caption ID, direct object EN chunk ID
sub processNode {
	my $n = $_[0];
	my $id = $_[1];
	my $sdobj = $_[2];
	my $p = plain($n);
	my $c = chunk($n);
	my $r = -1;

	# get an index
	if (not exists $index{$p}) {
		if (exists $oindex{$p}) {
			$r = $oindex{$p};
		} else {
			$r = $i;
			$i++;
		}

		$index{$p} = $r;
		$orig{$r} = {};
		$chunk{$r} = {};
		$cap{$r} = {};
	} else {
		$r = $index{$p};
	}
	
	$orig{$r}->{"$id\t$n"} = 1;
	$cap{$r}->{$id} = 1;
	if (not exists $chunk{$r}->{$c}) {
		$chunk{$r}->{$c} = {};
	}
	$chunk{$r}->{$c}->{$id} = 1;

	# if there's a direct object...
	if ($sdobj ne "") {
		my @ay = split(/ /, $n);
		my ($next, $prev) = breakSlash(\@ay, 1);
		# find the right EN chunk, and extract the direct object from it
		# and generate a TVERB link between the VP and the direct object
		for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
			if ($ay[$j]->[1] eq "[EN" && $ay[$j]->[2] eq $sdobj) {
				my @az = ();
				for (my $k = 0; $k < $next->[$j]; $k++) {
					push(@az, join("/", @{$ay[$k + $j]}));
				}
				my $s = processNode(join(" ", @az), $id, "");
				if ($r != $s) {
					my $z = "$r\tTVERB\t$s";
					if (not exists $links{$z}) {
						$links{$z} = {};
					}
					$links{$z}->{$id} = 1;
				}
				last;
			}
		}
	}

	return $r;
}

loadVPs($ARGV[0]);

my $file;

my $t0 = time();
$| = 1;
my $c = 0;

# check if we're extending a previous graph - load its index if we are
if ($#ARGV >= 3 && $ARGV[3] ne "") {
	open($file, "$ARGV[3]/node.idx");
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
}

# load the index of the NP sub-graph
$c = 0;
open($file, "$ARGV[2]/np.idx");
while (<$file>) {
	$c++;
	my $dt = time() - $t0;
	print "\r$c $dt";

	chomp($_);
	my @ax = split(/\t/, $_);
	$index{$ax[1]} = $ax[0];
	if ($i <= $ax[0]) {
		$i = $ax[0] + 1;
	}
}
close($file);
print "\n";

#my %short = ();
#open($file, "$ARGV[1]/trans10.txt");
#while (<$file>) {
#	chomp($_);
#	my @ax = split(/\t/, $_);
#	if ($#ax == 0) {
#		$ax[1] = "";
#	}
#	if ($#ax == 1) {
#		$short{$ax[0]} = $ax[1];
#	}
#}
#close($file);

# list of rules that we will be using
my %rules = ();
$rules{"+ADVP"} = 1;
$rules{"+NPART"} = 1;
$rules{"+NPHEAD"} = 1;
$rules{"+NPMOD"} = 1;
$rules{"+RB"} = 1;

$rules{"-Xof"} = 1;
$rules{"-ofY"} = 1;
$rules{"-Xor"} = 1;
$rules{"-orY"} = 1;
$rules{"-Xto"} = 1;
$rules{"-toY"} = 1;

# generate the graph
# basically, we'll do a full generating on all the SVO triples that we
# find for that caption
$c = 0;
my @id = ();
my @dep = ();
my @X = ();
my @Y = ();
my @type = ();
my $n = 0;
my $o = 0;
open($file, "$ARGV[1]/trans.final");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	# if its a caption
	if ($#ax == 2) {
		$c++;
		my $dt = time() - $t0;
		print "\r$c $dt";


		# go through the SVO triples
#		my @ay = split(/ /, $short{$ax[0]});
		my @ay = split(/ /, $ax[2]);
		my ($next, $prev) = breakSlash(\@ay, 1);
		my $c = countVPs($ax[0]);
		for (my $i = 0; $i < $c; $i++) {
			# grab the $ith SVO triple
			my ($subj, $vp, $dobj, $ssubj, $svp, $sdobj) = getVP(\@ay, $next, $prev, $ax[0], $i);

			# missing VP - next
			if ($vp == -2) {
				next;
			}

			# if we didn't find the dobj, have an empty dobj EN identifier
			if (!defined $sdobj || $dobj == -2) {
				$sdobj = "";
			}

			# grab the SVO
			my @az = ();
			my $end = $vp + $next->[$vp];
			if ($dobj >= 0) {
				$end = $dobj + $next->[$dobj];
			}

			for (my $j = $vp; $j < $end; $j++) {
				push(@az, join("/", @{$ay[$j]}));
			}

			# generate strings using the SVO and the selected rules
			my $s = join(" ", @az);
			my $x = generateSentences($s, $s, \@dep, \@X, \@Y, \@type, $n, "ALL", 0);
	
			# take the set of edges - add each node in an edge to the
			# graph, and then add the edge
			foreach my $s1 (keys %$x) {
				my $s = processNode($s1, $ax[0], $sdobj);
				foreach my $l (keys %{$x->{$s1}}) {
genSentLOOP:
					foreach my $t1 (keys %{$x->{$s1}->{$l}}) {
						my $t = processNode($t1, $ax[0], $sdobj);
						
						# grab the edge, if the nodes are not the same
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

		@id = ();
		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
		$o = 0;
	# grab a rule - if its of an appropriate type
	} elsif ($#ax == 4) {
		if ($ax[0] == $n) {
			my @ay = split(/\//, $ax[4]);

			if (exists $rules{$ay[0]}) {
				$id[$o] = $ax[0];
				$dep[$o] = $ax[1];
				$X[$o] = $ax[2];
				$Y[$o] = $ax[3];
				$type[$o] = $ax[4];
				$o++;
			}
			$n++;
		}
	}
}
close($file);
print "\n";

# output data structures
print "Sentence Index\n";
open($file, ">$ARGV[2]/vp.idx");
foreach (sort { $index{$a} <=> $index{$b} } keys %index) {
	print $file "$index{$_}\t$_\n";
}
close($file);

print "Graph\n";
open($file, ">$ARGV[2]/vp-tree.txt");
foreach (keys %links) {
	print $file "$_\t", join("\t", sort keys %{$links{$_}}), "\n";
}
close($file);

print "Caption\n";
open($file, ">$ARGV[2]/vp-cap.map");
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
open($file, ">$ARGV[2]/vp-orig.txt");
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
open($file, ">$ARGV[2]/vp-chunk.txt");
foreach my $x (sort { $a <=> $b } keys %chunk) {
	foreach (sort keys %{$chunk{$x}}) {
		print $file "$x\t$_\t", join("\t", sort keys %{$chunk{$x}->{$_}}), "\n";
	}
}
close($file);
