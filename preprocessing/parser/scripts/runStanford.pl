#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $free = 1;
if ($#ARGV >= 2) {
	$free = $ARGV[2];
} else {
	$ARGV[2] = $free;
}

my $file;

my $max = 1000;
if ($#ARGV >= 3) {
	$max = $ARGV[3];
} else {
	my $n = 0;
	open($file, $ARGV[0]);
	while (<$file>) {
		$n++;
	}
	close($file);
	$max = int($n / $ARGV[2]) + 1;
}

my $out;

my @ay = split(/\//, $ARGV[1]);
pop(@ay);
my $dir = join("/", @ay);

my $n = 0;
my $d = 0;
open($file, $ARGV[0]);
while (<$file>) {
	if ($n == 0) {
		my $x = sprintf("$dir/stanford-%04d.txt", $d);
		open($out, ">$x");
		$d++;
		$n++;
	}

	print $out $_;

	$n++;
	if ($n > $max) {
		close($out);
		$n = 0;
	}
}
close($file);

for (my $i = 0; $i < $d; $i++) {
	if ($free == 0) {
		wait;
		$free++;
	}

	if (fork() == 0) {
		my $iname = sprintf("stanford-%04d.txt", $i);
		my $oname = sprintf("stanford-%04d.out", $i);
		my $lname = sprintf("stanford-%04d.log", $i);
		my $x = "java -cp $sdir/../stanford/stanford-parser.jar -Xmx1024m edu.stanford.nlp.parser.lexparser.LexicalizedParser -sentences newline -outputFormat \"wordsAndTags,typedDependencies\" $sdir/../stanford/englishPCFG.ser.gz $dir/$iname 1> $dir/$oname 2> $dir/$lname";
		print "$x\n";
		system($x);
		exit;
	}
	$free--;
}

while ($free < $ARGV[2]) {
	wait;
	$free++;
}

open($out, ">$ARGV[1]");
for (my $i = 0; $i < $d; $i++) {
	my $x = sprintf("$dir/stanford-%04d.out", $i);
	open($file, $x);
	while (<$file>) {
		print $out $_;
	}
	close($file);
}
close($out);
