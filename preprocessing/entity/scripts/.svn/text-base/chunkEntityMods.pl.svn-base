#!/usr/bin/perl

use strict;
use warnings;

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;

my $file;
my %mods = ();

my %bigrams = ();
$bigrams{"rock climbing"} = 1;
$bigrams{"native american"} = 1;
$bigrams{"african american"} = 1;

my %unigrams = ();
$unigrams{"haired"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	for (my $i = 9; $i <= $#ax; $i += 5) {
		nlemmaAdd(tokenize($ax[$i]));
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	for (my $i = 9; $i <= $#ax; $i += 5) {
		my $h = nlemma(tokenize($ax[$i]));
		if (not exists $mods{$h}) {
			$mods{$h} = {};
		}
		my $m = tokenize($ax[$i - 1]);
		if ($m ne "") {
			$mods{$h}->{$m} = 1;
		}
	}
}
close($file);

my %output = ();
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	for (my $i = 9; $i <= $#ax; $i += 5) {
		my $h = nlemma(tokenize($ax[$i]));
		my @ay = split(/ /, tokenize($ax[$i - 1]));

		if ($h eq "face") {
			next;
		}
		if ($#ay <= 0) {
			next;
		}
		if ($ay[$#ay] eq "stuffed" || $ay[$#ay] eq "looking") {
			next;
		}

		for (my $j = 0; $j < $#ay; $j++) {
			my $x = join(" ", @ay[0 .. $j]);
			my $y = join(" ", @ay[($j + 1) .. $#ay]);

			my $w = $ay[$j] . " " . $ay[$j + 1];
			if (exists $bigrams{$w}) {
				next;
			}

			if (exists $unigrams{$ay[$j + 1]}) {
				next;
			}

			if ($ay[$j] eq "very" || $ay[$j] eq "bright" || $ay[$j] eq "dark" || $ay[$j] eq "light") {
				next;
			}

			if (exists $mods{$h}->{$x} && exists $mods{$h}->{$y}) {
				if (not exists $output{$h}) {
					$output{$h} = {};
				}
				$output{$h}->{"$x\t$y"}++;
			}
		}
	}
}
close($file);

foreach my $h (sort keys %output) {
	foreach (sort keys %{$output{$h}}) {
		print "$h\t$_\n";
	}
}
