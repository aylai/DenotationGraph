#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	for ($i = 9; $i <= $#ax; $i += 5) {
		nlemmaAdd(tokenize($ax[$i]));
	}
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);

	print "$ax[0]\t$ax[4]";
	for ($i = 9; $i <= $#ax; $i += 5) {
		print "\t", nlemma(tokenize($ax[$i]));
	}
	print "\n";
}
close(file);
