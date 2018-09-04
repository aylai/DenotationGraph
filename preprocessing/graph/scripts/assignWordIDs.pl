#!/usr/bin/perl

# Add a token ID to each token.  Uses the first field (with '/'
# separators).
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/ /, $ax[1]);
	print $ax[0], "\t", $#ay + 1, "\t";
	for ($i = 0; $i <= $#ay; $i++) {
		if ($i > 0) {
			print " ";
		}
		print "$i/$ay[$i]";
	}
	print "\n";
}
close(file);
