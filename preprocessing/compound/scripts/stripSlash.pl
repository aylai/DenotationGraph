#!/usr/bin/perl

use strict;
use warnings;

my $file;
my $n = 0;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		if ($ax[1] =~ /\//) {
			$n++;
			$ax[1] =~ s/\///g;
		}
	}
	print join("\t", @ax), "\n";
}
close($file);

if ($n > 0) {
	print STDERR "Warning! $n captions have had '/'s removed.\n";
}
