#!/usr/bin/perl

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/ /, $ax[2]);
	@aw = ();
	for ($i = 0; $i <= $#ay; $i++) {
		@az = split(/\//, $ay[$i]);
		if ((not $az[1] =~ /^[\[\]]/) && $az[2] =~ /^[^A-Z]$/) {
			next;
		} else {
			push(@aw, $ay[$i]);
		}
	}
	print "$ax[0]\t$ax[1]\t", join(" ", @aw), "\n";
}
close(file);
