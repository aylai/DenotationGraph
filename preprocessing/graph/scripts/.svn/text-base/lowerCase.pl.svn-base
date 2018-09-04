#!/usr/bin/perl

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	print "$ax[0]\t$ax[1]\t";
	@az = ();
	foreach (split(/ /, $ax[2])) {
		@ay = split(/\//, $_);
		if (not $ay[1] =~ /^[\[\]]/) {
			$ay[1] = lc($ay[1]);
		}
		push(@az, join("/", @ay));
	}
	print join(" ", @az);
	print "\n";
}
close(file);
