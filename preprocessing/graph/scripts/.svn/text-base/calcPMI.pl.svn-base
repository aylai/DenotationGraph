#!/usr/bin/perl

# ./calcPMI.pl <count file>

use strict;
use warnings;

my $file;

my @adir = split(/\//, $ARGV[0]);
pop(@adir);
my $dir = join("/", @adir);
my $n = 0;

# get the number of images that the denotation graph is over
open($file, "$dir/img.lst");
while (<$file>) {
	$n++;
}
close($file);

# for each pair of nodes...
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	# p(x) = |<visual denotation of x>| / <number of images>
	my $px = $ax[2] / $n;
	my $py = $ax[3] / $n;
	my $pxy = $ax[4] / $n;
	# pmi(x, y) = log (p(x,y) / (p(x) * p(y)))
	my $pmi = log($pxy / ($px * $py));
	# and then normalize the PMI
	my $npmi = $pmi / (-1 * log($pxy));

	# pmi, p(x | y), p(y | x), c(x), x, c(y), y
	print "$npmi\t", $ax[4] / $ax[3], "\t", $ax[4] / $ax[2], "\t$ax[2]\t$ax[0]\t$ax[3]\t$ax[1]\n";
}
close($file);
