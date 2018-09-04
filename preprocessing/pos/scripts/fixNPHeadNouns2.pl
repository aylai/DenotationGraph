#!/usr/bin/perl

# rechunk NPs into NP VPs.  Handles cases where the verb is mistagged.

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load list of candidate head nouns for the new NP
my %actor = ();
open($file, "$sdir/../data/actor.txt");
while (<$file>) {
	chomp($_);
	$actor{$_} = 1;
}
close($file);

# list of verbs which may have been mistagged as a noun
my %mistagV = ();
$mistagV{"bikes"} = 1;
$mistagV{"bite"} = 1;
$mistagV{"bites"} = 1;
$mistagV{"climb"} = 1;
$mistagV{"colors"} = 1;
$mistagV{"crosses"} = 1;
$mistagV{"dances"} = 1;
$mistagV{"drinks"} = 1;
$mistagV{"fight"} = 1;
$mistagV{"gathers"} = 1;
$mistagV{"gestures"} = 1;
$mistagV{"hides"} = 1;
$mistagV{"hold"} = 1;
$mistagV{"hugs"} = 1;
$mistagV{"interact"} = 1;
$mistagV{"jump"} = 1;
$mistagV{"kiss"} = 1;
$mistagV{"kisses"} = 1;
$mistagV{"laugh"} = 1;
$mistagV{"look"} = 1;
$mistagV{"lounge"} = 1;
$mistagV{"paddles"} = 1;
$mistagV{"paints"} = 1;
$mistagV{"passes"} = 1;
$mistagV{"peers"} = 1;
$mistagV{"pets"} = 1;
$mistagV{"play"} = 1;
$mistagV{"pose"} = 1;
$mistagV{"races"} = 1;
$mistagV{"ride"} = 1;
$mistagV{"rides"} = 1;
$mistagV{"rolls"} = 1;
$mistagV{"run"} = 1;
$mistagV{"sews"} = 1;
$mistagV{"sit"} = 1;
$mistagV{"skateboards"} = 1;
$mistagV{"skates"} = 1;
$mistagV{"smile"} = 1;
$mistagV{"smiles"} = 1;
$mistagV{"splashes"} = 1;
$mistagV{"sprints"} = 1;
$mistagV{"squats"} = 1;
$mistagV{"stacks"} = 1;
$mistagV{"steps"} = 1;
$mistagV{"swings"} = 1;
$mistagV{"walk"} = 1;
$mistagV{"waves"} = 1;
$mistagV{"work"} = 1;
$mistagV{"works"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		# find the NPs, $i is the start of NP index, $j is the end of NP index
		if ($ax[$i] eq "[NP") {
			my $j = $i;
			while ($j <= $#ax && $ax[$j] ne "]") {
				$j++;
			}

			# grab the last word of the NP chunk - if it is a mistagged verb,
			# and there is at least one other word in the NP, we may want to rechunk
			my @az = split(/\//, $ax[$j - 1]);
			my $v = $az[0];
			if ($j >= ($i + 3) && $#az == 1 && exists $mistagV{lc($v)}) {
				# grab the second to last word of the NP chunk - store the word in $act
				# this will be the head of the new NP if we rechunk
				my @an = split(/\//, $ax[$j - 2]);
				if ($#an == 1) {
					my $act = lc($an[0]);

					# @az - the result of rechunking the NP as an NP VP
					@az = ();
					for (my $x = $i; $x < ($j - 1); $x++) {
						push(@az, $ax[$x]);
					}
					push(@az, "]");
					push(@az, "[VP");
					if (lc($v) =~ /s$/ || $an[1] ne "NNS") {
						push(@az, "$v/VBZ");
					} else {
						push(@az, "$v/VBP");
					}
					push(@az, "]");

					# see if the head of the new NP is an actor - if so, use the rechunked NP VP
					if (exists $actor{$act}) {
						print STDERR "+", join(" ", @az), "\n";
						push(@ay, join(" ", @az));
						$i = $j;
						next;
					} else {
						print STDERR "-", join(" ", @az), "\n";
					}
				}
			}
		}
		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
