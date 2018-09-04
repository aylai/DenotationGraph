#!/usr/bin/perl

# tokenize.pl <POS file>

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ai = split(/ /, $ax[1]);
	print "$ax[0]\t";
	$first = 1;
	foreach (@ai) {
		@ax = split(/\//, $_);
		if (not $ax[0] =~ /^[\[\]]/) {
			if ($first == 1) {
				$first = 0;
			} else {
				print " ";
			}
			print "$ax[0]";
		}
	}
	print "\n";
}
close(file);
