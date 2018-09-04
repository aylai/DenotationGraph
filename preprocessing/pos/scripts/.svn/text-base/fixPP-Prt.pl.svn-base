#!/usr/bin/perl

use strict;
use warnings;

# rechunk cases where the first token in an NP is a preposition, so that it has its own SBAR or PP chunk

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	# @ax - caption
	# @ay - output caption
	# $last - last chunk seen
	# $last2 - second to last chunk seen
	# @words - sequence of words (ignoring chunk boundaries) in the caption
	my @ax = split(/ /, $_);
	my @ay = ();
	my $last = "";
	my $last2 = "";
	my @words = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		my @az = split(/\//, $ax[$i]);

		# if we're dealing with a preposition that is at the start of an NP chunk which is preceded by a PRT chunk
		if ($#ay >= 1 && $ay[$#ay - 0] eq "[NP" && $ay[$#ay - 1] eq "]" && $#az == 1 && $az[1] eq "IN") {
			if ($last2 eq "[PRT" && $i < $#ax && $ax[$i + 1] ne "]") {
				pop(@ay);
				# rechunk as either an SBAR (while/as, but not "dressed up as") or PP (everything else)
				if (lc($az[0]) eq "while") {
					push(@ay, "[SBAR");
				} elsif (lc($az[0]) eq "as" && !($#words >= 1 && $words[$#words - 0] eq "up" && $words[$#words - 1] eq "dressed")) {
					push(@ay, "[SBAR");
				} else {
					push(@ay, "[PP");
				}
				push(@ay, $ax[$i]);
				push(@ay, "]");
				push(@ay, "[NP");
				next;
			}
		}

		if ($ax[$i] =~ /^\[/) {
			$last2 = $last;
			$last = $ax[$i];
		} elsif ($ax[$i] =~ /^\]/) {
		} else {
			push(@words, lc($az[0]));
		}

		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
