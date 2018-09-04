#!/usr/bin/perl

# ./conllStanford.pl <token file> <stanford output>

@sent = ();
$i = 0;
open(file, $ARGV[0]);
while (<file>) {
	@a = split(/\t/, $_);
	@sent[$i] = $a[0];
	$i = $i + 1;
}
close(file);

$i = 0;
$state = 0;
%head = ();
%type = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	if ($state == 0) {
		$state = $state + 1;
	} elsif ($_ eq "") {
		$state = $state + 1;
	} else {
		@a = split(/[\(\)]|, /, $_);
		@b = split(/-/, $a[2]);
		$z = $b[$#b];
		$z =~ s/\'//;
		$id = "$sent[$i]-$z";
		@b = split(/-/, $a[1]);
		$y = $b[$#b];
		$y =~ s/\'//;
		if ($z != $y) {
			$head{$id} = $y;
			$type{$id} = $a[0];
		}
	}

	if ($state == 3) {
		$i = $i + 1;
		$state = 0;
	}
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	@b = split(/ /, $a[1]);
	for ($i = 0; $i <= $#b; $i++) {
		$j = $i + 1;
		$id = "$a[0]-$j";
		if (exists $head{$id}) {
			print "$a[0]\t$j\t$b[$i]\t$head{$id}\t$type{$id}\n";
		} else {
			print "$a[0]\t$j\t$b[$i]\t0\tnull\n";
		}
	}
	print "\n";
}
close(file);
