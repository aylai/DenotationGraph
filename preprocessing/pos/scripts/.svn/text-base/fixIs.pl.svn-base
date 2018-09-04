#!/usr/bin/perl

# retag "is/VBZ" as "his/PRP$"

use strict;
use warnings;

my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# look for "is/VBZ"
		if ($ay[0] eq "is" && $ay[1] ne "VBZ") {
			if ($i < $#ax) {
				my @az = split(/\//, $ax[$i + 1]);
				# "is <noun>" doesn't make any sense, is probably "his/PRP$"
				if ($az[1] =~ /^NN/) {
					# check for a compound noun, where the first word is tagged as a noun - only looks for "<noun> climbing"
					if (($i + 1) < $#ax) {
						my @az = split(/\//, $ax[$i + 2]);
						if ($az[1] eq "VBG" && $az[0] eq "climbing") {
							$ax[$i] = "is/VBZ";
							print STDERR "is/VBZ $ax[$i + 1] $ax[$i + 2]\n";
							next;
						}
					}
					$ax[$i] = "his/PRP\$";
					print STDERR "his/PRP\$ $ax[$i + 1]\n";
				} else {
					$ax[$i] = "is/VBZ";
					print STDERR "is/VBZ $ax[$i + 1]\n";
				}
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
