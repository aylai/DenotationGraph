#!/usr/bin/perl

use strict;
use warnings;

# convert chunks like [ADVP well ] [VP dressed ] [NP person ] into [NP well dressed person ]

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i != 0) {
			print " ";
		}

		# look for the leading ADVP chunk
		if ($ax[$i] eq "[ADVP") {
			my $j = $i + 1;
			# find the end of the ADVP chunk, and then make sure the next tokens are [VP dressed/VBN ] [ NP
			while ($j <= $#ax && $ax[$j] ne "]") {
				$j++;
			}
			$j++;
			if (($j + 3) <= $#ax && $ax[$j + 0] eq "[VP" && lc($ax[$j + 1]) eq "dressed/vbn" && $ax[$j + 2] eq "]" && $ax[$j + 3] eq "[NP") {
				# convert the three chunks into a single NP chunk
				my @ay = ();
				push(@ay, "[NP");
				for (my $k = $i + 1; $k < ($j - 1); $k++) {
					push(@ay, $ax[$k]);
				}
				push(@ay, $ax[$j + 1]);
				$j += 4;
				while ($j <= $#ax && $ax[$j] ne "]") {
					push(@ay, $ax[$j]);
					$j++;
				}
				push(@ay, "]");
				print join(" ", @ay);
				print STDERR join(" ", @ay), "\n";
				$i = $j;
				next;
			}
		}

		print "$ax[$i]";
	}
	print "\n";
}
close($file);
