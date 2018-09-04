#!/usr/bin/perl

use strict;
use warnings;

my $file;

# look for the verb "wearing" - the next token should be an adjective, not a verb
# e.g., "wearing swimming trunks" - swimming is usually tagged as a verb, but should be treated as an adjective in that case

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		if (lc($ax[$i]) eq "wearing/vbg" && ($i + 2) <= $#ax) {
			my @ay = split(/\//, $ax[$i + 1]);
			if ($ay[1] =~ /^V/) {
				$ay[1] = "JJ";
				$ax[$i + 1] = join("/", @ay);
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
