#!/usr/bin/perl

use strict;
use warnings;

# fix cases where there's a VP chunk consisting of only a PRP

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	my $last = "";
	for (my $i = 0; $i <= $#ax; $i++) {
		# see if we're at a [VP PRP ] - we're retagging it as an NP chunk
		if ($i <= ($#ax - 2) && $i > 0 && $ax[$i + 0] eq "[VP" && $ax[$i + 1] =~ /\/PRP$/ && $ax[$i + 2] eq "]" && $ax[$i - 1] eq "]") {
			# if the last chunk we saw was an NP chunk, we're going to take the last token in it and turn it into its own VP chunk
			if ($last eq "[NP") {
				pop(@ay);
				my @az = split(/\//, pop(@ay));
				if ($az[1] eq "NNS" || $az[0] =~ /[Ss]$/) {
					$az[1] = "VBZ";
				} else {
					$az[1] = "VBP";
				}

				if ($ay[$#ay] =~ /^\[/) {
					pop(@ay);
				} else {
					push(@ay, "]");
				}
				push(@ay, "[VP");
				push(@ay, join("/", @az));
				push(@ay, "]");
			}
			push(@ay, "[NP");
			next;
		}

		if ($ax[$i] =~ /^\[/) {
			$last = $ax[$i];
		}
		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
