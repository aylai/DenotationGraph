#!/usr/bin/perl

use strict;
use warnings;

my $file;
my $n = 0;
open($file, $ARGV[0]);
while (<$file>) {
	my @ax = split(/\t/, $_);
	if ($#ax >= 1 && $ax[1] =~ /\#/) {
		foreach (split(/ /, $ax[1])) {
			if ($_ ne "#" && $_ =~ /.\#/) {
				print STDERR "$ax[0]\n";
				$n++;
				last;
			}
		}
	}
}
close($file);

if ($n > 0) {
	print STDERR "Warning! $n captions have untokenized #s.\n";
}
