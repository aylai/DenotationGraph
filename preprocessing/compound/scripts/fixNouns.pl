#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load compound nouns that are one term
my %dict = ();
open($file, "$sdir/../data/nouns.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);

	$dict{"$ax[0] $ax[1]"} = 1;
	# make plural form, except for "sun set".
	# because "sun sets" is something else entirely
	if ($ax[0] ne "sun" || $ax[1] ne "set") {
		if ($ax[1] =~ /[xs]$/) {
			$dict{"$ax[0] $ax[1]es"} = 1;
		} else {
			$dict{"$ax[0] $ax[1]s"} = 1;
		}
	}
}
close($file);

# join compounds
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my @az = ();

LOOP:
		for (my $i = 0; $i <= $#ay; $i++) {
			# check if "X Y" are a known compound term
			if ($i < $#ay) {
				if (exists $dict{lc($ay[$i]) . " " . lc($ay[$i + 1])}) {
					push(@az, $ay[$i] . $ay[$i + 1]);
					$i++;
					next LOOP;
				}
			}

			# see if we're dealing with a hyphenated version
			my @aq = split(/-/, $ay[$i]);
			if ($#aq == 1) {
				if (exists $dict{lc($aq[0]) . " " . lc($aq[1])}) {
					push(@az, $aq[0] . $aq[1]);
					next LOOP;
				}
			}
			push(@az, $ay[$i]);
		}

		$ax[1] = join(" ", @az);
	}
	print join("\t", @ax), "\n";
}
close($file);
