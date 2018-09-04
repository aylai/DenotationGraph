#!/usr/bin/perl

# ./checkSubj.pl <subj file> <np>

%np = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$np{$ax[0]} = 1;
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if (exists $np{$ax[1]}) {
		print "$_\n";
	}
}
close(file);
