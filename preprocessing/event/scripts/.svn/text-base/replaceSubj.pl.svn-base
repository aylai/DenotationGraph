#!/usr/bin/perl

%replace = ();
for ($i = 1; $i <= $#ARGV; $i++) {
	open(file, $ARGV[$i]);
	while (<file>) {
		chomp($_);
		@ax = split(/\t/, $_);
		if ($#ax == 2) {
			$replace{"$ax[0]\t$ax[1]"} = $ax[2];
		} elsif ($#ax == 1) {
			print "$_\n";
		}
	}
	close(file);
}

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if (exists $replace{$_}) {
		print "$ax[0]\t$replace{$_}\n";
	} else {
		print "$_\n";
	}
}
close(file);
