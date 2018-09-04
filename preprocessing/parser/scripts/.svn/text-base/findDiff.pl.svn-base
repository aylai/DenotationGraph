#!/usr/bin/perl

use strict;
use warnings;

my $file;

my @id = ();
my @sent = ();

open($file, "../$ARGV[0].coref");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	push(@id, $ax[0]);
	push(@sent, $ax[1]);
}
close($file);

my $i = 0;
my $j = 0;
my @w = ();
my @p = ();
foreach (split(/ /, $sent[$i])) {
	my @ay = split(/\//, $_);
	if (not $ay[0] =~ /^[\[\]]/) {
		push(@w, $ay[0]);
		push(@p, $ay[1]);
	}
}

open($file, "$ARGV[0]/conll.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if ($j >= 0) {
		if (!exists $w[$j] || $w[$j] ne $ax[1] || $p[$j] ne $ax[3]) {
			$j = -1;
		} else {
			$j++;
		}
	}

	if ($#ax == -1) {
		$i++;
		$j = 0;
		@w = ();
		@p = ();
		if (exists $sent[$i]) {
			foreach (split(/ /, $sent[$i])) {
				my @ay = split(/\//, $_);
				if (not $ay[0] =~ /^[\[\]]/) {
					push(@w, $ay[0]);
					push(@p, $ay[1]);
				}
			}
		}
	}
}
close($file);
