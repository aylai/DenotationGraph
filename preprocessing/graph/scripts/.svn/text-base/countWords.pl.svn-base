#!/usr/bin/perl

# ./countWords.pl <dir> <word min> <cooccur min>

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

my %stopwords = ();
open($file, "$sdir/../data/stopwords.txt");
while (<$file>) {
	chomp($_);
	$stopwords{$_} = 1;
}
close($file);

my %word = ();
my %c = ();
open($file, "$ARGV[0]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/\#/, $ax[0]);
	foreach (split(/ /, $ax[1])) {
		if (not exists $stopwords{$_}) {
			if (not exists $word{$_}) {
				$word{$_} = {};
			}
			$word{$_}->{$ay[0]} = 1;
		}
	}
}
close($file);

foreach (keys %word) {
	my $n = scalar keys %{$word{$_}};

	if ($n >= $ARGV[1]) {
		$c{$_} = $n;
	} else {
		delete $word{$_};
	}
}

foreach my $x (keys %word) {
	foreach my $y (keys %word) {
		if ($x gt $y) {

			my $n = 0;
			foreach (keys %{$word{$y}}) {
				if (exists $word{$x}->{$_}) {
					$n++;
				}
			}

			if ($n >= $ARGV[2]) {
				print "$x\t$y\t$c{$x}\t$c{$y}\t$n\n";
			}
		}
	}
}
