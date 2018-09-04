#!/usr/bin/perl

# fix a number of cases where an NP VP or NP VP NP has been chunked as an NP

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load the list of nouns that are actors - i.e., if they're followed by a verb, it's probably a NP VP case
my %actor = ();
open($file, "$sdir/../data/actor.txt");
while (<$file>) {
	chomp($_);
	$actor{$_} = 1;
}
close($file);

# list of head nouns that can be preceded by an -ing verb (i.e., "sporting arena")
my %ingObj = ();
$ingObj{"area"} = 1;
$ingObj{"arena"} = 1;
$ingObj{"board"} = 1;
$ingObj{"booth"} = 1;
$ingObj{"clothing"} = 1;
$ingObj{"competition"} = 1;
$ingObj{"costume"} = 1;
$ingObj{"device"} = 1;
$ingObj{"equipment"} = 1;
$ingObj{"event"} = 1;
$ingObj{"field"} = 1;
$ingObj{"gear"} = 1;
$ingObj{"machine"} = 1;
$ingObj{"outfit"} = 1;
$ingObj{"park"} = 1;
$ingObj{"room"} = 1;
$ingObj{"uniform"} = 1;
$ingObj{"wall"} = 1;

# list of -ing verbs that can also be nouns
my %intVBG = ();
$intVBG{"backing"} = 1;
$intVBG{"boating"} = 1;
$intVBG{"building"} = 1;
$intVBG{"carving"} = 1;
$intVBG{"casting"} = 1;
$intVBG{"climbing"} = 1;
$intVBG{"covering"} = 1;
$intVBG{"crossing"} = 1;
$intVBG{"cutting"} = 1;
$intVBG{"dancing"} = 1;
$intVBG{"drawing"} = 1;
$intVBG{"driving"} = 1;
$intVBG{"drying"} = 1;
$intVBG{"fighting"} = 1;
$intVBG{"interesting"} = 1;
$intVBG{"netting"} = 1;
$intVBG{"racing"} = 1;
$intVBG{"rafting"} = 1;
$intVBG{"riding"} = 1;
$intVBG{"railing"} = 1;
$intVBG{"roping"} = 1;
$intVBG{"sculpting"} = 1;
$intVBG{"seeing"} = 1;
$intVBG{"setting"} = 1;
$intVBG{"signing"} = 1;
$intVBG{"skating"} = 1;
$intVBG{"skiing"} = 1;
$intVBG{"skydiving"} = 1;
$intVBG{"swing"} = 1;
$intVBG{"taking"} = 1;
$intVBG{"training"} = 1;
$intVBG{"viewing"} = 1;
$intVBG{"wrestling"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	# go through the caption, looking for NPs.
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($ax[$i] eq "[NP") {
			# get the counds $i is the start of NP index, $j is the end of NP index
			my $j = $i;
			while ($j <= $#ax && $ax[$j] ne "]") {
				$j++;
			}

			# find the last non-noun in the NP - store index in $k
			my $k = $j;
			while ($k > $i) {
				$k--;
				my @az = split(/\//, $ax[$k]);
				if ($#az == 1 && not $az[1] =~ /^NN/) {
					last;
				}
			}

			# found a non-noun in the NP - need to see if breaking up the NP is appropriate
			if (($i + 1) < $k) {
				# @az is the last non-noun in the NP
				# @aw is the prior token to it (potentially the new head noun)
				my @az = split(/\//, $ax[$k - 0]);
				my @aw = split(/\//, $ax[$k - 1]);
				# if we're looking at NN* V* (where V* is not VBN or VBD), this may be a case where we want to break up the NP
				# (i.e., if it's something like "dog/NN runs/VBZ").  We skip VBN and VBD because the chunker usually gets them right.
				if ($aw[1] =~ /^NN/ && $az[1] =~ /^V/ && $az[1] ne "VBN" && $az[1] ne "VBD") {
					# build the rechunked version of the NP in @az
					my @az = ();
					for (my $x = $i; $x < $k; $x++) {
						push(@az, $ax[$x]);
					}
					push(@az, "]");
					push(@az, "[VP");
					push(@az, $ax[$k]);
					push(@az, "]");
					if (($k + 1) < $j) {
						push(@az, "[NP");
						for (my $x = $k + 1; $x < $j; $x++) {
							push(@az, $ax[$x]);
						}
						push(@az, "]");
					}

					# $good - determines whether we use the rechunked version or stay with the original NP
					my $good = 0;

					# if the head noun of the new first NP is going to be a recognized actor
					# (typically a person, animal, or piece of clothing - e.g.,
					# [NP man] [PP in] [NP hat shooting gun] -> [NP man] [PP in] [NP hat] [VP shooting] [NP gun]
					# then we want to rechunk
					if (exists $actor{lc($aw[0])}) {
						$good = 1;
					# if there's no direct object in the rechunking
					} elsif (($k + 1) == $j) {
						# check what the verb of the new VP is going to be
						my @aw = split(/\//, $ax[$k]);

						# if it's a VBZ, we probably want to rechunk, modulo some cases where its actually a mis-tagged noun
						if ($aw[1] eq "VBZ") {
							if (lc($aw[0]) ne "sailboats" && lc($aw[0]) ne "covers" && lc($aw[0]) ne "hoodie" && lc($aw[0]) ne "gear" && lc($aw[0]) ne "track" && lc($aw[0]) ne "do") {
								$good = 1;
							}
						# if it's a VBG, check if it's a VBG which could also be a noun
						} elsif ($aw[1] eq "VBG") {
							if (not exists $intVBG{lc($aw[0])}) {
								$good = 1;
							}
						}
					# otherwise we have a direct object - check if its the sort of noun that could have a VBG modifier
					} else {
						my @aw = split(/\//, $ax[$j - 1]);
						if (not exists $ingObj{lc($aw[0])}) {
							$good = 1;
						}
					}

					# use the rechunking
					if ($good == 1) {
						print STDERR "+", join(" ", @az), "\n";
						push(@ay, join(" ", @az));
						$i = $j;
					# use the original NP
					} else {
						print STDERR "-", join(" ", @az), "\n";
						push(@ay, $ax[$i]);
					}
					next;
				}
			}
		}
		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
