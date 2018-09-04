#!/usr/bin/perl

use strict;
use warnings;

# break off a tail verb from an NP chunk into its own VP chunk

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load color words - these can be the head of an NP (referring to clothing)
# but will typically be treated as a modifier
my %color = ();
open($file, "$sdir/../data/color.txt");
while (<$file>) {
	chomp($_);
	$color{$_} = 1;
}
close($file);

my $debug = "";

if ($#ARGV > 0) {
	$debug = $ARGV[1];
}

# list of tokens that are verbs and should be retagged as a verb
my %verbs = ();
$verbs{"cheer"} = "VBP";
$verbs{"cheers"} = "VBZ";
$verbs{"cheering"} = "VBG";
$verbs{"dance"} = "VBP";
$verbs{"dances"} = "VBZ";
$verbs{"dancing"} = "VBG";
$verbs{"fight"} = "VBP";
$verbs{"fights"} = "VBZ";
$verbs{"fighting"} = "VBG";
$verbs{"gestures"} = "VBZ";
$verbs{"gesturing"} = "VBG";
$verbs{"grin"} = "VBP";
$verbs{"grins"} = "VBZ";
$verbs{"grinning"} = "VBG";
#$verbs{"kick"} = "VBP";
$verbs{"kicks"} = "VBZ";
$verbs{"kicking"} = "VBG";
$verbs{"laugh"} = "VBP";
$verbs{"laughs"} = "VBZ";
$verbs{"laughing"} = "VBG";
$verbs{"lean"} = "VBP";
$verbs{"leans"} = "VBZ";
$verbs{"leaning"} = "VBG";
$verbs{"pet"} = "VBP";
$verbs{"pets"} = "VBZ";
$verbs{"petting"} = "VBG";
$verbs{"pose"} = "VBP";
$verbs{"poses"} = "VBZ";
$verbs{"posing"} = "VBG";
$verbs{"rest"} = "VBP";
$verbs{"rests"} = "VBZ";
$verbs{"resting"} = "VBG";
$verbs{"run"} = "VBP";
$verbs{"runs"} = "VBZ";
$verbs{"running"} = "VBG";
$verbs{"sleep"} = "VBP";
$verbs{"sleeps"} = "VBZ";
$verbs{"sleeping"} = "VBG";
#$verbs{"slide"} = "VBP";
$verbs{"slides"} = "VBZ";
$verbs{"sliding"} = "VBG";
$verbs{"smile"} = "VBP";
$verbs{"smiles"} = "VBZ";
$verbs{"smiling"} = "VBG";
$verbs{"speak"} = "VBP";
$verbs{"speaks"} = "VBZ";
$verbs{"speaking"} = "VBG";
$verbs{"swim"} = "VBP";
$verbs{"swims"} = "VBZ";
$verbs{"swimming"} = "VBG";
$verbs{"talk"} = "VBP";
$verbs{"talks"} = "VBZ";
$verbs{"talking"} = "VBG";
$verbs{"walk"} = "VBP";
$verbs{"walks"} = "VBZ";
$verbs{"walking"} = "VBG";
$verbs{"yell"} = "VBP";
$verbs{"yells"} = "VBZ";
$verbs{"yelling"} = "VBG";

# list of compound nouns that end with a verb
# the verb should not be broken up into their own VP
my %compound = ();
$compound{"ballet dance"} = 1;
$compound{"ballroom dance"} = 1;
$compound{"belly dance"} = 1;
$compound{"breakdancing dance"} = 1;
$compound{"dragon dance"} = 1;
$compound{"ethnic dance"} = 1;
$compound{"fan dance"} = 1;
$compound{"festival dance"} = 1;
$compound{"fire dance"} = 1;
$compound{"flatulence dance"} = 1;
$compound{"floor dance"} = 1;
$compound{"hula dance"} = 1;
$compound{"lap dance"} = 1;
$compound{"line dance"} = 1;
$compound{"parade or dance"} = 1;
$compound{"performance or dance"} = 1;
$compound{"pole dance"} = 1;
$compound{"ritual dance"} = 1;
$compound{"river dance"} = 1;
$compound{"slow dance"} = 1;
$compound{"song and dance"} = 1;
$compound{"song or dance"} = 1;
$compound{"street dance"} = 1;
$compound{"traditional dance"} = 1;
$compound{"unique dance"} = 1;

$compound{"balloon fight"} = 1;
$compound{"bull fight"} = 1;
$compound{"gang fight"} = 1;
$compound{"gun fight"} = 1;
$compound{"hockey fight"} = 1;
$compound{"karate fight"} = 1;
$compound{"knife fight"} = 1;
$compound{"martial arts fight"} = 1;
$compound{"mma fight"} = 1;
$compound{"paintball fight"} = 1;
$compound{"pillow fight"} = 1;
$compound{"play fight"} = 1;
$compound{"playful fight"} = 1;
$compound{"pro fight"} = 1;
$compound{"snow fight"} = 1;
$compound{"snowball fight"} = 1;
$compound{"stick fight"} = 1;
$compound{"sword fight"} = 1;
$compound{"water fight"} = 1;

$compound{"hand gestures"} = 1;

$compound{"high kick"} = 1;
$compound{"karate kick"} = 1;
$compound{"synchronized kick"} = 1;

$compound{"' pose"} = 1;
$compound{"action pose"} = 1;
$compound{"ballet pose"} = 1;
$compound{"dance pose"} = 1;
$compound{"dancing pose"} = 1;
$compound{"exercise pose"} = 1;
$compound{"fighting pose"} = 1;
$compound{"intertwining pose"} = 1;
$compound{"karate pose"} = 1;
$compound{"kissing pose"} = 1;
$compound{"lunge pose"} = 1;
$compound{"pirouette pose"} = 1;
$compound{"playful pose"} = 1;
$compound{"prayer pose"} = 1;
$compound{"symmetrical pose"} = 1;
$compound{"weird pose"} = 1;
$compound{"wrestling pose"} = 1;
$compound{"yoga pose"} = 1;

$compound{"arm rest"} = 1;

$compound{"home run"} = 1;
$compound{"marathon run"} = 1;
$compound{"mile run"} = 1;
$compound{"motocross run"} = 1;
$compound{"relay run"} = 1;
$compound{"ski run"} = 1;

$compound{"synchronized swim"} = 1;

$compound{"pep talk"} = 1;

$compound{"advocacy walk"} = 1;
$compound{"aids walk"} = 1;
$compound{"beach walk"} = 1;
$compound{"benefit walk"} = 1;
$compound{"cobblestone walk"} = 1;
$compound{"evening walk"} = 1;
$compound{"hollywood walk"} = 1;
$compound{"model walk"} = 1;
$compound{"nature walk"} = 1;
$compound{"night walk"} = 1;
$compound{"organized walk"} = 1;
$compound{"river walk"} = 1;
$compound{"rock walk"} = 1;
$compound{"space walk"} = 1;
$compound{"winter walk"} = 1;

# conjugate the compound nouns - -s and -ing endings
my %compounds = ();
foreach (keys %compound) {
	my @ax = split(/ /, $_);
	my $x = $ax[$#ax];
	my $y = join(" ", @ax[0 .. $#ax - 1]);

	if (not exists $compounds{$x}) {
		$compounds{$x} = {};
	}
	$compounds{$x}->{$y} = 1;

	$x = $ax[$#ax] . "s";
	if (not exists $compounds{$x}) {
		$compounds{$x} = {};
	}
	$compounds{$x}->{$y} = 1;

	if ($ax[$#ax] =~ /e$/) {
		$x = $ax[$#ax];
		$x =~ s/e$/ing/;
	} else {
		$x = $ax[$#ax] . "ing";
	}
	if (not exists $compounds{$x}) {
		$compounds{$x} = {};
	}
	$compounds{$x}->{$y} = 1;

	$x = $ax[$#ax];
	$x =~ s/(.)$/$1$1ing/;
	if (not exists $compounds{$x}) {
		$compounds{$x} = {};
	}
	$compounds{$x}->{$y} = 1;
}

if ($debug ne "") {
	foreach (keys %verbs) {
		if ($_ ne $debug) {
			delete $verbs{$_};
		}
	}
}

# list of tag sequences that should not be used to form an NP chunk
my %tags = ();
$tags{"DT"} = 1;
$tags{"DT JJ"} = 1;
$tags{"DT JJR"} = 1;
$tags{"DT JJS"} = 1;
$tags{"DT VBG"} = 1;
$tags{"DT VBN"} = 1;
$tags{"DT RB JJ"} = 1;
$tags{"DT RB JJR"} = 1;
$tags{"DT RB JJS"} = 1;
$tags{"DT RB VBG"} = 1;
$tags{"DT RB VBN"} = 1;
$tags{"JJ"} = 1;
$tags{"JJR"} = 1;
$tags{"JJS"} = 1;
$tags{"RB JJ"} = 1;
$tags{"RB JJR"} = 1;
$tags{"RB JJS"} = 1;
$tags{"POS"} = 1;
$tags{"PRP\$"} = 1;
$tags{"PRP\$ JJ"} = 1;
$tags{"PRP\$ JJR"} = 1;
$tags{"PRP\$ JJS"} = 1;
$tags{"PRP\$ VBG"} = 1;
$tags{"PRP\$ VBN"} = 1;
$tags{"PRP\$ RB JJ"} = 1;
$tags{"PRP\$ RB JJR"} = 1;
$tags{"PRP\$ RB JJS"} = 1;
$tags{"PRP\$ RB VBG"} = 1;
$tags{"PRP\$ RB VBN"} = 1;
$tags{"RB"} = 1;
$tags{"RB VBG"} = 1;
$tags{"RB VBN"} = 1;
$tags{"VBG"} = 1;
$tags{"VBN"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	# @ax - caption
	# @ay - new caption
	# $np - 0 not in NP chunk, 1 in NP chunk
	# @npw - tokens seen so far in NP chunk
	# @npt - tags seen so far in NP chunk
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

		# are we in an NP chunk?
		if ($np == 1) {
			my @az = split(/\//, $ax[$i]);
			# if so, check if we're at the last token
			if ($i < $#ax && $ax[$i + 1] eq "]") {
				# if this could be a verb....
				if (exists $verbs{lc($az[0])}) {
					# make sure the last token is not part of a compound noun that we want to keep
					if ($#npt >= 0 && exists $compounds{lc($az[0])}) {
						for (my $j = $#npw; $j >= 0; $j--) {
							if (exists $compounds{lc($az[0])}->{lc(join(" ", @npw[$j .. $#npw]))}) {
								if ($debug ne "") {
									print "0 ", join(" ", @ay), " $ax[$i] \]\n";
								}
								push(@ay, $ax[$i]);
								next LOOP;
							}
						}
					}

					# check for an "is Xing" case
					if ($verbs{lc($az[0])} eq "VBG" && lc(join(" ", @npw)) eq "'s") {
						# just turn the NP chunk into a VP chunk.  Also, retag the 's
						pop(@ay);
						pop(@ay);
						push(@ay, "[VP");
						push(@ay, join(" ", @npw) . "/VBZ");
						push(@ay, "$az[0]/" . $verbs{lc($az[0])});
						push(@ay, "]");
						$i++;
						$np = 0;

						if ($debug ne "") {
							print "1 ", join(" ", @ay), "\n";
						}
						next;
					# make sure the NP chunk is longer than one token
					# make sure the sequence of tags isn't something that can't be used to form an NP chunk (%tags)
					# or, if the sequence of tokens is a color word, we can just make an NP chunk out of that (%color)
					# or, if the verb is an -ing verb, check if we're looking "a/an/the Xing" (should be handled by %tags, as well)
					} elsif ($#npt >= 0 && (!exists $tags{join(" ", @npt)} ||
											exists $color{lc(join(" ", @npw))} ||
											($verbs{lc($az[0])} eq "VBG" && lc(join(" ", @npw)) ne "a" && lc(join(" ", @npw)) ne "an" && lc(join(" ", @npw)) ne "the"))) {
						# if there's a separator (conjunction or comma), move that outside of the chunks
						# either way, end the current NP chunk
						if ($npt[$#npt] eq "CC" || $npt[$#npt] eq ",") {
							my $and = pop(@ay);
							if ($#npt == 0) {
								pop(@ay);
							} else {
								push(@ay, "]");
							}
							push(@ay, $and);
						} else {
							push(@ay, "]");
						}

						# make a new VP chunk
						push(@ay, "[VP");
						push(@ay, "$az[0]/" . $verbs{lc($az[0])});
						push(@ay, "]");
						$i++;
						$np = 0;

						if ($debug ne "") {
							print "1 ", join(" ", @ay), "\n";
						}
						next;
					# if there's nothing but the verb, and it's an -ing verb, turn the NP chunk into a VP chunk
					} elsif ($#npt == -1 && $verbs{lc($az[0])} eq "VBG") {
						pop(@ay);
						push(@ay, "[VP");
						push(@ay, "$az[0]/" . $verbs{lc($az[0])});
						push(@ay, "]");
						$i++;
						$np = 0;

						if ($debug ne "") {
							print "1 ", join(" ", @ay), "\n";
						}
						next;
					} else {
						if ($debug ne "") {
							print "0 ", join(" ", @ay), " $ax[$i] \]\n";
						}
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
