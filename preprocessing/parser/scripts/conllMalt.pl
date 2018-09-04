#!/usr/bin/perl

# ./conllMalt.pl <POS file> <Malt output>

@sent = ();
$i = 0;
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	$sent[$i] = $a[0];
	$i = $i +  1;
}
close(file);

$i = 0;
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	if ($_ eq "") {
		$i = $i + 1;
		print "\n";
	} else {
		@c = split(/\t/, $_);
		print "$sent[$i]\t$c[0]\t$c[1]\t$c[6]\t$c[7]\n";
	}
}
close(file);
