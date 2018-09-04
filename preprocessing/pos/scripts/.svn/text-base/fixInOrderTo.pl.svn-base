#!/usr/bin/perl

use strict;
use warnings;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();

LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		push(@r, $ax[$i]);

		my $j;
		my $k;

		# "in" must be at the start of a chunk, followed by "order"
		# as the next token, and then "to".  "to" must be the
		# start of a chunk.  We form an SBAR chunk for "in order to",
		# an if the chunk "to" was in goes on, we start that chunk
		# after "to", instead.
		my @ay = split(/\//, $ax[$i]);
		if (lc($ay[0]) ne "in") {
			next;
		}

		for ($j = $i + 1; $j <= $#ax; $j++) {
			@ay = split(/\//, $ax[$j]);
			if (lc($ay[0]) eq "order") {
				last;
			} elsif ($ay[0] =~ /^[\[\]]/) {
				next;
			}
			next LOOP;
		}
		if ($j > $#ax) {
			next;
		}

		for ($k = $j + 1; $k <= $#ax; $k++) {
			@ay = split(/\//, $ax[$k]);
			if (lc($ay[0]) eq "to") {
				last;
			} elsif ($ay[0] =~ /^[\[\]]/) {
				next;
			}
			next LOOP;
		}
		if ($k > $#ax) {
			next;
		}

		if ($i > 0 && $ax[$i - 1] =~ /^\[/ && $ax[$k - 1] =~ /^\[/) {
			pop(@r);
			pop(@r);
			push(@r, "[SBAR");
			push(@r, $ax[$i]);
			push(@r, $ax[$j]);
			push(@r, $ax[$k]);
			push(@r, "]");
			if (($k + 1) <= $#ax && $ax[$k + 1] eq "]") {
				$i = $k + 1;
			} else {
				push(@r, $ax[$k - 1]);
				$i = $k;
			}
		}
	}
	print join(" ", @r), "\n";
}
close($file);
