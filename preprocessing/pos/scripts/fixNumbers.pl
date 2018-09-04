#!/usr/bin/perl

use strict;
use warnings;

# replace numerals with words (0 -> "zero", etc.)

my $file;

# if any of these words appear in the caption, do nothing
my %bad = ();
$bad{"#"} = 1;
$bad{"labeled"} = 1;
$bad{"number"} = 1;
$bad{"numbered"} = 1;


# a numeral before one of these words can be replaced
my %post = ();
$post{"bull"} = 1;
$post{"bulls"} = 1;
$post{"cat"} = 1;
$post{"cats"} = 1;
$post{"dog"} = 1;
$post{"dogs"} = 1;
$post{"girl"} = 1;
$post{"guy"} = 1;
$post{"nerf"} = 1;
$post{"of"} = 1;
$post{"other"} = 1;
$post{"others"} = 1;
$post{"tiers"} = 1;
$post{"wheeler"} = 1;
$post{"year"} = 1;
$post{"years"} = 1;

# a numeral after one of these words can be replaced
my %pre = ();
$pre{"of"} = 1;

# list of number replacements
my @numbers = ( "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
				"eleven", "twelve" );

# also, a numeral before another numeral can be replaced.
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	if ($#ax >= 0 && $ax[0] =~ /^[0-9]*$/ && exists $numbers[$ax[0]]) {
		$post{lc($ax[1])} = 1;
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	if ($#ax >= 0 && $ax[0] =~ /^[0-9]*$/ && exists $numbers[$ax[0]]) {
		$ax[0] = ucfirst($numbers[$ax[0]]);
	}

	my $good = 1;
	for (my $i = 0; $i <= $#ax; $i++) {
		if (exists $bad{lc($ax[$i])}) {
			$good = 0;
			last;
		}
	}

	if ($good == 1) {
		for (my $i = 1; $i < $#ax; $i++) {
			if ($ax[$i] =~ /^[0-9]*$/ && exists $numbers[$ax[$i]] &&
				(exists $pre{lc($ax[$i - 1])} || exists $post{lc($ax[$i + 1])})) {
				$ax[$i] = $numbers[$ax[$i]];
			}
		}
	}

	print join(" ", @ax), "\n";
}
close($file);
