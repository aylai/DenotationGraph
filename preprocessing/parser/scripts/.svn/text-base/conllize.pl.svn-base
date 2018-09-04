#!/usr/bin/perl

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ai = split(/ /, $ax[1]);
	$j = 1;
	for ($i = 0; $i <= $#ai; $i++) {
		@ax = split(/\//, $ai[$i]);
		if (not $ax[0] =~ /^[\[\]]/) {
			print "$j\t$ax[0]\t_\t$ax[1]\t$ax[1]\t_\n";
			$j = $j + 1;
		}
	}
	print "\n";
}
close(file);
