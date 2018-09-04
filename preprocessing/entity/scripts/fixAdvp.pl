#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin/../../misc";
use parse;
use util;
use WordNet::QueryData;

use strict;
use warnings;

my $file;

my %advp = ();
$advp{"at least"} = 1;
$advp{"brightly"} = 0;
$advp{"elderly"} = 0;
$advp{"first"} = 0;
$advp{"little"} = 0;
$advp{"long"} = 0;
$advp{"nice"} = 0;
$advp{"slightly"} = 0;
$advp{"still"} = 0;
$advp{"third"} = 0;
$advp{"very"} = 0;

my %nodt = ();
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($ax[7] ne "") {
		$nodt{$ax[0]} = 1;
	}
}
close($file);

open($file, $ARGV[1]);
while (<$file>) {
	chomp($_);
	my @a = split(/\t/, $_);
	my @b = split(/ /, $a[1]);

	my $s = ();
	my $i = 0;
	my $n = 0;
	my $p = 0;
	while ($i <= $#b) {
		($s->[$n], $i, $p) = parse(\@b, $i, $p);
		$n++;
	}

	print "$a[0]\t";
	my $np = 0;
	for (my $i = 0; $i < $n; $i++) {
		if ($i > 0) {
			print " ";
		}
		if ($i < ($n - 1) && $s->[$i + 0]->[0] eq "ADVP" && $s->[$i + 1]->[0] eq "NP") {
			my $x = tokenize($s->[$i]->[1]);
			my $p0 = unparse($s->[$i + 0]);
			my $p1 = unparse($s->[$i + 1]);

			if (exists $advp{$x}) {
				if ($advp{$x} == 0) {
					if (not exists $nodt{"$a[0]#NP$np"}) {
						print "[NP $s->[$i + 0]->[1] $s->[$i + 1]->[1] ]";
						print STDERR "+$p0 $p1\n";
					} else {
						print "$p0 $p1";
						print STDERR "-$p0 $p1\n";
					}
				} else {
					print "[NP $s->[$i + 0]->[1] $s->[$i + 1]->[1] ]";		
					print STDERR "+$p0 $p1\n";
				}
			} else {
				print "[ADVP $s->[$i + 0]->[1] ] [NP $s->[$i + 1]->[1] ]";		
				print STDERR "-$p0 $p1\n";
			}
			
			$np++;
			$i++;
		} elsif ($s->[$i]->[0] eq "NP") {
			print unparse($s->[$i]);
			$np++;
		} else {
			print unparse($s->[$i]);
		}
	}
	print "\n";
}
close($file);
