#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load the hyphenated ngrams
my %ngram = ();
open($file, "$sdir/../data/ngram.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$ngram{$ax[0]} = $ax[2];
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		my @az = split(/\//, $ax[$i]);
		# is this a hyphenated n-gram?
		if (exists $ngram{lc($az[0])}) {
			my @as = split(/-/, $az[0]);
			my @at = split(/-/, $ngram{lc($az[0])});
			my $pp = "";

			# is this a preposition, and did we just start a chunk?
			# if so, make sure it's a PP chunk
			if ($az[1] eq "IN" && $i > 0 && $ay[$#ay] =~ /^\[/) {
				$pp = $ay[$#ay];
				$ay[$#ay] = "[PP";
			}

			# unhyphenate and retag tokens
			for (my $j = 0; $j <= $#as; $j++) {
				push(@ay, "$as[$j]/$at[$j]");
			}

			# if we made a PP chunk, then close it, and restart the
			# chunk we were working on, if it goes on.  This handles
			# cases where the preposition gets added to the beginning
			# of, say an NP chunk, or put in an NP chunk by itself.
			# [NP x-y-z/IN ... ] -> [PP x y z ] [NP ... ]
			if ($i < $#ax && $ax[$i + 1] ne "]" && $pp ne "") {
				if (lc($ax[$i + 1]) =~ /^and\//) {
					if (($i + 1) < $#ax) {
						if ($ax[$i + 2] eq "]") {
							push(@ay, "]");
							push(@ay, $ax[$i + 1]);
							$i += 2;
						}
					}
				} else {
					push(@ay, "]");
					push(@ay, $pp);
				}
			}

			next;
		}
		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
