#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# read the compound verbs
my @a = ();
my @b = ();
my $k = 0;
open($file, "$sdir/../data/verbs.txt");
while (<$file>) {
	chomp($_);
	my @c = split(/ /, $_);
	$a[$k] = $c[0];
	$b[$k] = $c[1];
	$k++;
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my @az = ();

LOOP:
		for (my $i = 0; $i <= $#ay; $i++) {
			# check if "X Y" is a compound verb
			if ($i < $#ay) {
				for (my $j = 0; $j < $k; $j++) {
					if (lc($ay[$i]) eq $a[$j] && lc($ay[$i + 1]) =~ /^$b[$j]/) {
						push(@az, $ay[$i] . $ay[$i + 1]);
						$i++;
						next LOOP;
					}
				}
			}

			# check if "X-Y" is a compound verb
			my @aq = split(/-/, $ay[$i]);
			if ($#aq == 1) {
				for (my $j = 0; $j < $k; $j++) {
					if (lc($aq[0]) eq $a[$j] && lc($aq[1]) =~ /^$b[$j]/) {
						push(@az, $aq[0] . $aq[1]);
						next LOOP;
					}
				}
			}

			push(@az, $ay[$i]);
		}

		$ax[1] = join(" ", @az);
	}
	print join("\t", @ax), "\n";
}
close($file);
