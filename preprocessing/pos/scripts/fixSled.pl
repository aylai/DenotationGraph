#!/usr/bin/perl

use strict;
use warnings;

my $file;

# retags "sled" from a verb to a noun

# preceding tags that indicate that "sled" is a verb
# i.e., previous token is the subject (NNS and PRP), or "to sled"
my %verb = ();
$verb{"NNS"} = 1;
$verb{"PRP"} = 1;
$verb{"TO"} = 1;

# preceding tags that indicate that "sled" is a noun
# determiners (a sled), preposition (in sled), adjectives (red sled), nouns/proper nouns (snow sled), possessive pronouns (her sled)
my %noun = ();
$noun{"DT"} = 1;
$noun{"IN"} = 1;
$noun{"JJ"} = 1;
$noun{"NN"} = 1;
$noun{"NNP"} = 1;
$noun{"PRP\$"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	for (my $i = 0; $i <= $#ax; $i++) {
		my @ay = split(/\//, $ax[$i]);
		# look for instances of "sled" tagged as a verb
		if (lc($ay[0]) eq "sled" && $ay[1] =~ /^V/) {
			if ($i > 0) {
				my @az = split(/\//, $ax[$i - 1]);
				# check if the previous tag indicates that "sled" is a verb
				if (exists $verb{$az[1]}) {
					print STDERR "-$ax[$i - 1] $ax[$i]\n";
				# check if the previous tag indicates that "sled" is a noun
				} elsif (exists $noun{$az[1]}) {
					$ax[$i] = "$ay[0]/NN";
					if ($i < $#ax) {
						my @az = split(/\//, $ax[$i + 1]);
						if ($az[1] eq "NNS") {
							$ax[$i + 1] = "$az[0]/VBZ";
							print STDERR "+$ax[$i - 1] $ax[$i] $ax[$i + 1]\n";
						} elsif (lc($az[1]) eq "laden") {
							$ax[$i + 1] = "$az[0]/VBD";
							print STDERR "+$ax[$i - 1] $ax[$i] $ax[$i + 1]\n";
						}
					} else {
						print STDERR "+$ax[$i - 1] $ax[$i]\n";
					}
				# okay, no idea what sled should be - don't do anything
				} else {
					print STDERR "?$ax[$i - 1] $ax[$i]\n";
				}
			}
		}
	}

	print join(" ", @ax), "\n";
}
close($file);
