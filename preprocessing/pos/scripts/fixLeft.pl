#!/usr/bin/perl

use strict;
use warnings;

# retag instances of "left" that have been tagged as a verb
# we're aiming to either retag it as a noun or an adjective

my $file;

# list of auxiliary verbs that can precede left
# when "left" is preceded by a verb, it's an adjective (looks left), unless the verb is one of the auxiliaries ("was left")
my %aux = ();
$aux{"am"} = 1;
$aux{"are"} = 1;
$aux{"been"} = 1;
$aux{"got"} = 1;
$aux{"had"} = 1;
$aux{"has"} = 1;
$aux{"is"} = 1;
$aux{"was"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# check if the token is "left" and tagged as a verb
		if (lc($ay[0]) eq "left" && $ay[1] =~ /^V/) {
			if ($i > 0) {
				my @az = split(/\//, $ax[$i - 1]);
				# if the preceding token indicates the start of an NP ("his left" or "the left")
				if ($az[1] eq "PRP\$" || $az[1] eq "DT") {
					# ...if the next thing is a noun, "left" is a modifier of that noun
					if ($i < $#ax) {
						my @aw = split(/\//, $ax[$i + 1]);
						if ($aw[1] =~ /^N/) {
							$ay[1] = "JJ";
							$ax[$i] = join("/", @ay);
							next;
						}
					}

					# otherwise "left" is a noun
					$ay[1] = "NN";
					$ax[$i] = join("/", @ay);
					next;
				# "in/on left"
				} elsif ((lc($az[0]) eq "in" || lc($az[0]) eq "on") && $az[1] eq "IN") {
					$ay[1] = "JJ";
					$ax[$i] = join("/", @ay);
					next;
				# preceded by a non-auxiliary verb ("look left") -> "left" is an adjective
				} elsif ($az[1] =~ /^V/ && !exists $aux{lc($az[0])}) {
					$ay[1] = "JJ";
					$ax[$i] = join("/", @ay);
					next;
				# "Xes to left" - "to" is really a preposition, and "left" is an adjective
				} elsif ($az[1] eq "TO") {
					$ay[1] = "JJ";
					$ax[$i] = join("/", @ay);
					$az[1] = "IN";
					$ax[$i - 1] = join("/", @az);
					next;
				}
			}

			if ($i < $#ax) {
				my @az = split(/\//, $ax[$i + 1]);
				# "left of" -> left is a preposition
				if ($az[1] eq "IN" && lc($az[0]) =~ /of$/) {
					$ay[1] = "JJ";
					$ax[$i] = join("/", @ay);
					next;
				# "left <body part>" -> left is a preposition
				} elsif (lc($az[0]) eq "hand" || lc($az[0]) eq "arm" || lc($az[0]) eq "leg" || lc($az[0]) eq "foot") {
					$ay[1] = "JJ";
					$ax[$i] = join("/", @ay);
					next;
				}
			}

		}
	}
	print join(" ", @ax), "\n";
}
close($file);
