#!/usr/bin/perl

use strict;
use warnings;

# break up NPs of the form "[NP X and Y ]" into "[NP X ] and [NP Y ]"

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# list of people terms - we only want to break up cases where it's "<nopt person> and <person>"
# so this is cases like "[NP man ] [VP wearing ] [NP sweater and woman ]" should really be "[NP man ] [VP wearing ] [NP sweater ] and [NP woman ]"
# we leave things like "man and woman" as a single NP chunk.  (i.e., does it make sense to leave the two entities grouped?)
my %person = ();
open($file, "$sdir/../data/person.txt");
while (<$file>) {
	chomp($_);
	$person{$_} = 1;
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();
	my $first = 1;

LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		# never break up the first NP chunk
		if ($first == 0) {
			# do we have an NP chunk?
			if ($ax[$i] eq "[NP") {
				# $j - index of the end of the NP chunk
				# $k - index of the "and" in the NP chunk
				my $j;
				my $k;

				for ($j = $i; $j <= $#ax; $j++) {
					if ($ax[$j] eq "]") {
						last;
					}

					# if there are commas, we're dealing with a list, and we have no idea how to handle that
					if ($ax[$j] =~ /^,/) {
						push(@r, $ax[$i]);
						next LOOP;
					}
				}

				for ($k = $i; $k <= $j; $k++) {
					my @ay = split(/\//, $ax[$k]);
					if (lc($ay[0]) eq "and") {
						last;
					}
				}

				# if there is an "and" in the NP chunk, and the right side of the and is at most two tokens...
				if ($k <= $j && ($k + 4) > $j) {
					# @ay - head of the right side of the and
					# @az - head of the left side of the and
					# @aw - the first token of the right side of the and (possibly the head, possibly a modifier)
					# l1 - length of the left side of the and
					# l2 - length of the right side of the and
					my @ay = split(/\//, $ax[$j - 1]);
					my @az = split(/\//, $ax[$k - 1]);
					my @aw = split(/\//, $ax[$k + 1]);
					my $l1 = ($k - $i) - 1;
					my $l2 = ($j - $k) - 1;

					# if the right side is a person term and the left side isn't, and the right side isn't rider (don't break up "horse and rider")
					# and there's stuff on both the left and right sides of the and, and the right side isn't too longer (more than two tokens - we've technically checked that twice now)
					# and either the right side is one token, or there's a VP for the right side to be the subject of...
					# and either the right side is one token, or neither token is a modifier..
					if (exists $person{lc($ay[0])} && !exists $person{lc($az[0])} &&
						lc($ay[0]) ne "rider" &&
						$l1 > 0 && $l2 > 0 &&
						$l2 < 3 &&
						($l2 == 1 || ($j < $#ax && $ax[$j + 1] eq "[VP")) &&
						($l2 == 1 || !($aw[1] =~ /^[JV]/) || !($az[1] =~ /^[JV]/))) {
						# then we break up the NP chunk
						for (my $l = $i; $l <= $j; $l++) {
							if ($l == $k) {
								push(@r, "]");
							}
							push(@r, $ax[$l]);
							if ($l == $k) {
								push(@r, "[NP");
							}
						}
						
						$i = $j;
						next;
					}
				}
			}
		}

		$first = 0;

		if ($ax[$i] eq "[SBAR") {
			$first = 1;

			while ($i <= $#ax && $ax[$i] ne "]") {
				push(@r, $ax[$i]);
				$i++;
			}
		}

		push(@r, $ax[$i]);
	}

	print join(" ", @r), "\n";
}
close($file);
