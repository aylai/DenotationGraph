#!/usr/bin/perl

# ./checkSubj.pl <np> <type>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;

%type = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	$type{$_} = 1;
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	for ($i = 9; $i <= $#ax; $i += 5) {
		nlemmaAdd(tokenize($ax[$i]));
	}
}

%np = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	for ($i = 9; $i <= $#ax; $i += 5) {
		if ($ax[$i - 3] ne "") {
			$x = $ax[$i - 3];
		} else {
			$x = nlemma(tokenize($ax[$i]));
		}
		if (exists $type{$x}) {
			print "$ax[0]\n";
		}
	}
}
close(file);
