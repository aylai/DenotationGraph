#!/usr/bin/perl

use strict;
use warnings;

# joins together compound verbs, so that the chunker doesn't place them in different chnks

my $file;

# list of compound verbs to join together
my %compound = ();
$compound{"ballet-dancing"} = 1;
$compound{"ballroom-dancing"} = 1;
$compound{"brake-dancing"} = 1;
$compound{"break-dancing"} = 1;
$compound{"bungee-climbing"} = 1;
$compound{"bungee-jump"} = 1;
$compound{"bungee-jumping"} = 1;
$compound{"cliff-jumping"} = 1;
$compound{"cross-country-skiing"} = 1;
$compound{"distance-jumping"} = 1;
$compound{"face-paint"} = 1;
$compound{"face-painted"} = 1;
$compound{"face-painting"} = 1;
$compound{"face-paints"} = 1;
$compound{"fire-dancing"} = 1;
$compound{"free-climbing"} = 1;
$compound{"hang-gliding"} = 1;
$compound{"high-jumping"} = 1;
$compound{"high-jumps"} = 1;
$compound{"high-kicking"} = 1;
$compound{"high-kicks"} = 1;
$compound{"hula-hooping"} = 1;
$compound{"ice-skated"} = 1;
$compound{"ice-skating"} = 1;
$compound{"inline-skating"} = 1;
$compound{"karate-kicks"} = 1;
$compound{"knife-fighting"} = 1;
$compound{"line-dancing"} = 1;
$compound{"mma-fighting"} = 1;
$compound{"mountain-biking"} = 1;
$compound{"mountain-climb"} = 1;
$compound{"mountain-climbing"} = 1;
$compound{"pan-frying"} = 1;
$compound{"pole-dancing"} = 1;
$compound{"river-dancing"} = 1;
$compound{"rock-climbing"} = 1;
$compound{"rock-climbs"} = 1;
$compound{"rock-crawling"} = 1;
$compound{"scuba-diving"} = 1;
$compound{"scuba-dives"} = 1;
$compound{"slow-dancing"} = 1;
#$compound{"snow-skiing"} = 1;
$compound{"spray-painting"} = 1;
$compound{"stick-fighting"} = 1;
$compound{"street-dancing"} = 1;
$compound{"sword-fighting"} = 1;
$compound{"synchronized-swimming"} = 1;
$compound{"tap-dance"} = 1;
$compound{"water-rafting"} = 1;
$compound{"window-shop"} = 1;
$compound{"window-shops"} = 1;
$compound{"window-shopping"} = 1;
$compound{"wind-sailing"} = 1;

# find the maximum number of tokens that can be joined together
my $max = 0;
foreach (keys %compound) {
	my @ax = split(/-/, $_);
	if ($max < scalar @ax) {
		$max = scalar @ax;
	}
}

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	my @ax = split(/ /, $_);

	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		$ax[$i] = [ @ay ];
	}

	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i != 0) {
			print " ";
		}

		# find a sequence of tokens that is in %compound
		my $j = 0;
		my @s = ();
		for ($j = 0; ($i + $j) <= $#ax && $j < $max; $j++) {
			push(@s, $ax[$i + $j]->[0]);
		}
		while ($j > 0) {
			if (exists $compound{lc(join("-", @s))}) {
				last;
			}
			$j--;
			pop(@s);
		}

		# make sure it's really a verb
		if ($j > 0 && $ax[$i + $j - 1]->[1] =~ /^V/) {
			print join("-", @s), "/", $ax[$i + $j - 1]->[1];
			$i += $j - 1;
		} else {
			print join("/", @{$ax[$i]});
		}
	}
	print "\n";
}
close($file);
