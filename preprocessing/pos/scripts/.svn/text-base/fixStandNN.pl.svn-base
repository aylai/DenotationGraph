#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

# list of "stand"s that are nouns
my %valid = ();
$valid{"advertising"} = 1;
$valid{"balloon"} = 1;
$valid{"banana"} = 1;
$valid{"beverage"} = 1;
$valid{"book"} = 1;
$valid{"bread"} = 1;
$valid{"bubble"} = 1;
$valid{"cigarette"} = 1;
$valid{"chocolate"} = 1;
$valid{"clothing"} = 1;
$valid{"cream"} = 1;
$valid{"coffee"} = 1;
$valid{"concession"} = 1;
$valid{"cone"} = 1;
$valid{"convenience"} = 1;
$valid{"curbside"} = 1;
$valid{"doll"} = 1;
$valid{"donut"} = 1;
$valid{"farm"} = 1;
$valid{"flower"} = 1;
$valid{"food"} = 1;
$valid{"fruit"} = 1;
$valid{"fruits"} = 1;
$valid{"gift"} = 1;
$valid{"grip"} = 1;
$valid{"grocery"} = 1;
$valid{"guitar"} = 1;
$valid{"hamburger"} = 1;
$valid{"hat"} = 1;
$valid{"hotdog"} = 1;
$valid{"ice"} = 1;
$valid{"information"} = 1;
$valid{"item"} = 1;
$valid{"lemonade"} = 1;
$valid{"lottery"} = 1;
$valid{"magazine"} = 1;
$valid{"market"} = 1;
$valid{"microphone"} = 1;
$valid{"music"} = 1;
$valid{"newspaper"} = 1;
$valid{"night"} = 1;
$valid{"outdoors"} = 1;
$valid{"night"} = 1;
$valid{"pizza"} = 1;
$valid{"produce"} = 1;
$valid{"programmes"} = 1;
$valid{"refreshment"} = 1;
$valid{"restaurant"} = 1;
$valid{"roadside"} = 1;
$valid{"sales"} = 1;
$valid{"seafood"} = 1;
$valid{"shoeshine"} = 1;
$valid{"smoothies"} = 1;
$valid{"snack"} = 1;
$valid{"snow"} = 1;
$valid{"street"} = 1;
$valid{"store"} = 1;
$valid{"table"} = 1;
$valid{"tea"} = 1;
$valid{"telephone"} = 1;
$valid{"ticket"} = 1;
$valid{"tourist"} = 1;
$valid{"tuba"} = 1;
$valid{"vegetable"} = 1;
$valid{"vendor"} = 1;

my $file;

# load the list of actors - if we see one of these before "stand", we're probably dealing with a verb
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
LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		# see if we're dealing with a noun "stand" token
		if (lc($ax[$i]) eq "stand/nn") {
			my $j = $i - 1;

			# in order for us to accept stand as a noun, we have to find the NP it belongs to
			# we'll do this by searching backwards for the start of the NP (either a singular determiner or a directional preposition)
			# in the mean time, the tokens in the NP must be ones that we expect (see %valid), and if
			# we encounter tokens that indicate that "stand" is a verb, we'll retag it as a verb
			while ($j >= 0) {
				my @ay = split(/\//, $ax[$j]);
				# start of an NP - singular noun determiners - stand is a noun
				if ($ay[1] eq "DT" || $ay[1] eq "PRP\$" || $ay[1] eq "POS" || ($ay[1] eq "CD" && lc($ay[0]) eq "one")) {
					last;
				# start of an NP - directional preposition - stand is a noun
				} elsif ($ay[1] eq "IN" && (lc($ay[0]) eq "in-front-of" || lc($ay[0]) eq "by" || lc($ay[0]) eq "at" || lc($ay[0]) eq "beside")) {
					last;
				# person term - this is probably an actor and stand is verb
				} elsif (exists $person{lc($ay[0])}) {
					# retag the person term as a noun (tagging stand as a noun may have screwed up this tagging)
					if (not $ay[1] =~ /^N/) {
						$ay[1] = "NN";
						$ax[$j] = join("/", @ay);
					}

					my @az = ();
					while ($j <= $i) {
						push(@az, $ax[$j]);
						$j++;
					}
					print STDERR join(" ", @az), "\n";

					@ay = split(/\//, $ax[$i]);
					$ax[$i] = "$ay[0]/VBP";
					next LOOP;
				# tokens that could be part of an NP - keep looking for the start of the NP
				} elsif ($ay[1] eq "JJ" || $ay[1] eq "CC" || $ay[1] eq "NNP" || $ay[1] eq "RB") {
					# except "top" could be the piece of clothing, in which case retag "stand" as a verb
					if (lc($ay[0]) eq "top") {
						my @az = ();
						while ($j <= $i) {
							push(@az, $ax[$j]);
							$j++;
						}
						print STDERR join(" ", @az), "\n";

						@ay = split(/\//, $ax[$i]);
						$ax[$i] = "$ay[0]/VBP";
						next LOOP;
					}
				# more tokens that could be part of an NP
				} elsif ($ay[1] =~ /^NN/ && exists $valid{lc($ay[0])}) {
				} elsif ($ay[1] eq "VBG" && (lc($ay[0]) eq "selling" || lc($ay[0]) eq "vending")) {
				} elsif ($ay[1] eq "VBN" && (lc($ay[0]) eq "lit" || lc($ay[0]) eq "stuffed")) {
				# non-NP token - stand is a verb
				} else {
					my @az = ();
					while ($j <= $i) {
						push(@az, $ax[$j]);
						$j++;
					}
					print STDERR join(" ", @az), "\n";

					@ay = split(/\//, $ax[$i]);
					$ax[$i] = "$ay[0]/VBP";
					next LOOP;
				}
				$j--;
			}
		}
	}
	print join(" ", @ax), "\n";
}
close($file);
