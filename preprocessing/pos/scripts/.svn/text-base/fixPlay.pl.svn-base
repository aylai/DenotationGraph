#!/usr/bin/perl

use strict;
use warnings;

# break up NP chunks with "play" in them

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load color words - can be the head of an NP, but may have been treated as a modifier
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

# the verbs that we are looking for
my %verbs = ();
$verbs{"play"} = "VBP";
$verbs{"playing"} = "VBG";
$verbs{"plays"} = "VBZ";

# verbs + direct object that we are looking for (will conjugate later)
my %dobj = ();
$dobj{"play bagpipes"} = 1;
$dobj{"play banjo"} = 1;
$dobj{"play banjos"} = 1;
$dobj{"play basketball"} = 1;
$dobj{"play baseball"} = 1;
$dobj{"play bass"} = 1;
$dobj{"play checkers"} = 1;
$dobj{"play chess"} = 1;
$dobj{"play corn-hole"} = 1;
$dobj{"play cricket"} = 1;
$dobj{"play dress-up"} = 1;
$dobj{"play drum"} = 1;
$dobj{"play drums"} = 1;
$dobj{"play football"} = 1;
$dobj{"play frisbee"} = 1;
$dobj{"play game"} = 1;
$dobj{"play games"} = 1;
$dobj{"play golf"} = 1;
$dobj{"play guitar"} = 1;
$dobj{"play guitars"} = 1;
$dobj{"play hockey"} = 1;
$dobj{"play horn"} = 1;
$dobj{"play horns"} = 1;
$dobj{"play indoors"} = 1;
$dobj{"play instrument"} = 1;
$dobj{"play instruments"} = 1;
$dobj{"play jenga"} = 1;
$dobj{"play keyboard"} = 1;
$dobj{"play keyboards"} = 1;
$dobj{"play maracas"} = 1;
$dobj{"play music"} = 1;
$dobj{"play nintendo"} = 1;
$dobj{"play outdoors"} = 1;
$dobj{"play outside"} = 1;
$dobj{"play paintball"} = 1;
$dobj{"play piano"} = 1;
$dobj{"play pianos"} = 1;
$dobj{"play ping-pong"} = 1;
$dobj{"play pool"} = 1;
$dobj{"play racquetball"} = 1;
$dobj{"play saxophone"} = 1;
$dobj{"play saxophones"} = 1;
$dobj{"play soccer"} = 1;
$dobj{"play sports"} = 1;
$dobj{"play tennis"} = 1;
$dobj{"play tug-of-war"} = 1;
$dobj{"play ukulele"} = 1;
$dobj{"play ukuleles"} = 1;
$dobj{"play violin"} = 1;
$dobj{"play violins"} = 1;
$dobj{"play volleyball"} = 1;
$dobj{"play xylophone"} = 1;
$dobj{"play xylophones"} = 1;

# conjugate the entries in %dobj
my %dobjs = ();
foreach (keys %dobj) {
	my @ax = split(/ /, $_);
	my $x = $ax[0];
	my $y = join(" ", @ax[1 .. $#ax]);

	if (!exists $dobjs{$x}) {
		$dobjs{$x} = {};
	}
	$dobjs{$x}->{$y} = 1;

	$x = $ax[0] . "s";
	if (!exists $dobjs{$x}) {
		$dobjs{$x} = {};
	}
	$dobjs{$x}->{$y} = 1;

	$x = $ax[0] . "ing";
	if (!exists $dobjs{$x}) {
		$dobjs{$x} = {};
	}
	$dobjs{$x}->{$y} = 1;
}

# compound nouns involving the verb "play"
my %compound = ();
$compound{"diving play"} = 1;
$compound{"football play"} = 1;
$compound{"role play"} = 1;
$compound{"school play"} = 1;

# conjugate %compound
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

	$x = $ax[$#ax] . "ing";
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
	foreach (keys %dobjs) {
		if ($_ ne $debug) {
			delete $dobjs{$_};
		}
	}
}

# tag sequences that should not be an NP chunk
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
	# @ay - output caption
	# $np - 0 not in NP chunk, 1 in NP chunk
	# @npw - tokens seen in NP chunk so far
	# @npt - tags seen in NP chunk so far
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

		if ($np == 1) {
			my @az = split(/\//, $ax[$i]);
			# is this the last token of the NP chunk, and is that last token a form of "play"
			if ($i < $#ax && $ax[$i + 1] eq "]") {
				if (exists $verbs{lc($az[0])}) {
					# make sure it's not part of a compound noun
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

					# check if we're dealing with an "is playing" case
					if ($verbs{lc($az[0])} eq "VBG" && lc(join(" ", @npw)) eq "'s") {
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
					# make sure there's something else in the NP chunk
					# check if that something else is something that can't make an NP (%tags)
					# check if that something else is a color word (%color)
					# check if we're dealing with "a/an/the playing"
					} elsif ($#npt >= 0 && (!exists $tags{join(" ", @npt)} || 
											exists $color{lc(join(" ", @npw))} || 
											($verbs{lc($az[0])} eq "VBG" && lc(join(" ", @npw)) ne "a" && lc(join(" ", @npw)) ne "an" && lc(join(" ", @npw)) ne "the"))) {
						# if there's a separator (conjunction or comma) - remove it from the NP chunk
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

						# create a VP chunk containing "play"
						push(@ay, "[VP");
						push(@ay, "$az[0]/" . $verbs{lc($az[0])});
						push(@ay, "]");
						$i++;
						$np = 0;

						if ($debug ne "") {
							print "1 ", join(" ", @ay), "\n";
						}
						next;
					# otherwise, if the only thing in the NP chunk is "playing", turn it into a VP chunk
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
			# otherwise, if this is the second to last token in the NP chunk, see if it's "play" + <direct object>
			} elsif ($i < ($#ax - 1) && $ax[$i + 2] eq "]") {
				my @aw = split(/\//, $ax[$i + 1]);
				if (exists $dobjs{lc($az[0])} && exists $dobjs{lc($az[0])}->{lc($aw[0])}) {
					# also make sure that "play" isn't part of a compound noun
					# (if it is, something really weird is going on, as we have <compound noun w/play> <direct object>)
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

					# if there's nothing else in the NP chunk, turn "play" into a VP chunk and the direct object into a new NP chunk
					if ($#npt == -1) {
						pop(@ay);
						push(@ay, "[VP");
						push(@ay, "$az[0]/" . $verbs{lc($az[0])});
						push(@ay, "]");
						push(@ay, "[NP");
						push(@ay, $ax[$i + 1]);
						push(@ay, "]");
						$i += 2;
						$np = 0;

						if ($debug ne "") {
							print "1 ", join(" ", @ay), "\n";
						}
						next;
					# see if the earlier tags in the NP chunk can form an NP chunk by themselves (%tags)
					# see if the earlier tokens in the NP chunk are a color word (%color)
					# see if the we're looking at "a/an/the playing"
					} elsif (!exists $tags{join(" ", @npt)} ||
							 exists $color{lc(join(" ", @npw))} || 
							 (lc($az[0]) =~ /ing$/ && lc(join(" ", @npw)) ne "a" && lc(join(" ", @npw)) ne "an" && lc(join(" ", @npw)) ne "the")) {
						# if there's a separator (conjunction or comma) - remove it from the NP chunk
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

						# create a VP chunk out of "play", and then an NP chunk out of the direct object
						push(@ay, "[VP");
						push(@ay, "$az[0]/" . $verbs{lc($az[0])});
						push(@ay, "]");
						push(@ay, "[NP");
						push(@ay, $ax[$i + 1]);
						push(@ay, "]");
						$i += 2;
						$np = 0;

						if ($debug ne "") {
							print "1 ", join(" ", @ay), "\n";
						}
						next;
					} else {
						if ($debug ne "") {
							print "0 ", join(" ", @ay), " $ax[$i] $ax[$i + 1] \]\n";
						}
					}
				} elsif (exists $dobjs{lc($az[0])}) {
					if ($debug ne "") {
						print "2 ", join(" ", @ay), " $ax[$i] $ax[$i + 1] \]\n";
					}
				}
				push(@npw, $az[0]);
				push(@npt, $az[1]);
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
