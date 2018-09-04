#!/usr/bin/perl

%word = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	@b = split(/ /, $a[1]);
	for ($i = 0; $i < $#b; $i++) {
		if (lc($b[$i]) eq lc($b[$i + 1])) {
			print "$_\n";
			last;
		}
	}
}
close(file);
