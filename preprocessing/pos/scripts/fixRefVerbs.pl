#!/usr/bin/perl

use strict;
use warnings;

# rechunk NPs by breaking off a tail verb from it and chunking it as a new VP.
# [NP a girl ] [PP in ] [NP pink climbs ] -> [NP a girl ] [PP in ] [NP pink ] [VP climbs ]

my $file;

# list of token sequences that can form an NP chunk
# e.g., allow [NP another sings ] -> [NP another ] [VP sings ]
my %refs = ();
$refs{"all"} = 1;
$refs{"another"} = 1;
$refs{"both"} = 1;
$refs{"each"} = 1;
$refs{"each other"} = 1;
$refs{"many"} = 1;
$refs{"the other"} = 1;
$refs{"while"} = 1;

# list of tokens that can form the head of an NP chunk
my %ends = ();
$ends{","} = 1;
$ends{"and"} = 1;
$ends{"black"} = 1;
$ends{"blue"} = 1;
$ends{"brown"} = 1;
$ends{"female"} = 1;
$ends{"gray"} = 1;
$ends{"green"} = 1;
$ends{"male"} = 1;
$ends{"orange"} = 1;
$ends{"pink"} = 1;
$ends{"purple"} = 1;
$ends{"red"} = 1;
$ends{"white"} = 1;
$ends{"yellow"} = 1;

# list of tag sequences (space separated) that can form an NP chunk
my %tags = ();
$tags{"CD"} = 1;

# list of complete NP chunks that should remain NP chunks
# (normally we'd want to break "walks" off from the end of an NP, but for "[NP all walks ] [PP of ] [NP life ]" we don't)
my %compounds = ();
$compounds{"all walks"} = 1;
$compounds{"one handed"} = 1;

# nouns which could be mistagged as a verb - assume that they actually are nouns
my %things = ();
$things{"backlighting"} = 1;
$things{"crossing"} = 1;
$things{"eyed"} = 1;
$things{"flooring"} = 1;
$things{"frosting"} = 1;
$things{"icing"} = 1;
$things{"lighting"} = 1;
$things{"netting"} = 1;
$things{"points"} = 1;
$things{"scaffolding"} = 1;
$things{"seating"} = 1;
$things{"stocking"} = 1;
$things{"string"} = 1;
$things{"tubing"} = 1;

my $debug = "";

if ($#ARGV > 0) {
	$debug = $ARGV[1];

	foreach (keys %refs) {
		if ($_ ne $debug) {
			delete $refs{$_};
		}
	}

	foreach (keys %ends) {
		if ($_ ne $debug) {
			delete $ends{$_};
		}
	}

	foreach (keys %tags) {
		if ($_ ne $debug) {
			delete $tags{$_};
		}
	}
}

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	# @ax - input
	# @ay - output
	# $np - 0 not in an NP, 1 in an NP
	# @npw - list of tokens seen so far in the NP
	# @npt - list of tags seen so far in the NP
	my @ax = split(/ /, $_);
	my @ay = ();
	my $np = 0;
	my @npw = ();
	my @npt = ();

LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] eq "]") {
			$np = 0;
		}

		# we're in an NP chunk - see if the last token of the NP chunk is tagged as a verb
		# also, make sure it is not a noun that gets mistagged as a verb (%things)
		# and that the entire NP chunk isn't some compound that shouldn't be broken up (%compounds)
		if ($np == 1) {
			my @az = split(/\//, $ax[$i]);
			if ($az[1] =~ /^V/ && $i < $#ax && $ax[$i + 1] eq "]" &&
				!exists $things{lc($az[0])} && 
				!exists $compounds{lc(join(" ", @npw)) . " " . lc($az[0])}) {

				# if there are other tokens besides the last token in the NP chunk, we may be able to break it up
				if ($#npw >= 0) {
					# we'll be looking at the last token to see if it's in %ends, but it may be the case
					# that it's hyphenated - in which case, matching the last word in the hyphenated token is fine, too.
					my @ah = split(/-/, $npw[$#npw]);

					# if all of the tokens besides the last token is in %refs - it can form its own NP chunk
					# or if all of the tags of the tokens besides the last token is in %tags - it can form its own NP chunk
					# or if the second to last token is in %ends - that's a valid head of an NP chunk that tends not to get used as an NP head
					# ...then we want to break up the NP chunk
					if (exists $refs{join(" ", @npw)} || exists $tags{join(" ", @npt)} ||
						exists $ends{lc($npw[$#npw])} || exists $ends{lc($ah[$#ah])}) {
						# if the second to last token is a separator, we want NP <sep> VP
						if ($npt[$#npt] eq "CC" || $npw[$#npw] eq ",") {
							my $x = pop(@ay);
							if ($#npw == 0) {
								pop(@ay);
							} else {
								push(@ay, "]");
							}
							push(@ay, $x);
						} else {
							push(@ay, "]");
						}
						push(@ay, "[VP");
						push(@ay, join("/", @az));
						push(@ay, "]");
						$i++;
						$np = 0;

						if ($debug ne "") {
							print join(" ", @ay), "\n";
						}
						next;
					}
				}
			} else {
				push(@npw, $az[0]);
				push(@npt, $az[1]);
			}
		}

		if ($ax[$i] eq "[NP") {
			if ($debug ne "") {
				@ay = ();
			}

			$np = 1;
			@npw = ();
			@npt = ();
		}

		push(@ay, $ax[$i]);
	}

	if ($debug eq "") {
		print join(" ", @ay), "\n";
	}
}
close($file);
