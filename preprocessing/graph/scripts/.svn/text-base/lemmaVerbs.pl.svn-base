#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use parse;

# auxiliary verbs - drop these
my %auxes = ();
$auxes{"be"} = 1;
$auxes{"have"} = 1;
$auxes{"see"} = 1;
$auxes{"show"} = 1;

# do not use the regular lemmatization for these verbs - doing so will
# conflate them with nouns we don't want to conflate them with
my %overrides = ();
$overrides{"dress"} = "dressed";
$overrides{"park"} = "parking";
$overrides{"stand"} = "standing";

$overrides{"texting"} = "text";
$overrides{"parasailing"} = "parasail";
$overrides{"showcasing"} = "showcase";
$overrides{"wakeboarding"} = "wakeboard";
$overrides{"waterskiing"} = "waterski";
$overrides{"woodworking"} = "woodwork";

# prefixes we want to drop - i.e., "appear to jump", drop "appear to".
# Or we used to want to do that, it'll now be handled in
# splitSubjVerb.pl as a rewrite rule instead.  We still want to drop
# "to", though, as part of the normalization.
my %dropTo = ();
$dropTo{"to"} = 1;
#$dropTo{"appear to"} = 1;
#$dropTo{"attempt to"} = 1;
#$dropTo{"be about to"} = 1;
#$dropTo{"begin to"} = 1;
#$dropTo{"look to"} = 1;
#$dropTo{"seem to"} = 1;
#$dropTo{"start to"} = 1;
#$dropTo{"try to"} = 1;

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/ /, $ax[2]);
	my @az = ();
	breakSlash(\@ay, 1);
	# $vp is the length of the current VP chunk that we're grabbing.
	# -1 if we're not in a VP chunk.  $aux is whether or not we can
	# still see auxiliary verbs
	my $vp = -1;
	my $aux = 0;
	for (my $i = 0; $i <= $#ay; $i++) {
		# enter the VP chunk
		if ($ay[$i]->[1] eq "[VP") {
			$vp = 0;
			$aux = 1;
		# leave the VP chunk if we're in one
		} elsif ($ay[$i]->[1] eq "]") {
			$vp = -1;
		# inside of a VP chunk
		} elsif ($vp >= 0) {
			# if it's a verb, lemmatize it, and possibly junk it if
			# it's an auxiliary verb
			if ($ay[$i]->[2] =~ /^V/) {
				my $w = vlemma($ay[$i]->[1]);
				if (exists $overrides{$w}) {
					$w = $overrides{$w};
				}
				# check if we're an auxiliary verb we are if 1) we
				# still expect auxiliary verbs, 2) we're on the list
				# of auxiliary verbs, and 3) (ignoring adverbs) the
				# next thing in the VP chunk is a verb.
				if ($aux == 1) {
					if (exists $auxes{$w}) {
						for (my $j = $i + 1; $j <= $#ay; $j++) {
							if ($ay[$j]->[1] eq "]") {
								$aux = 0;
								last;
							} elsif ($ay[$j]->[2] =~ /^V/) {
								last;
							} elsif ($ay[$j]->[2] ne "RB") {
								$aux = 0;
								last;
							}
							
						}
					} else {
						$aux = 0;
					}

					if ($aux == 1) {
						next;
					}
				}
				$ay[$i]->[1] = $w;
			# if we encounter a "TO', check if we should drop it, if
			# we do, we can expect auxiliary verbs again.
			} elsif ($ay[$i]->[2] eq "TO") {
				my @at = ();
				unshift(@at, "to");
				for (my $j = 1; $j < $vp; $j++) {
					my @aq = split(/\//, $az[$#az + 1 - $j]);
					unshift(@at, $aq[0]);
				}

				if (exists $dropTo{join(" ", @at)}) {
					while ($vp > 1) {
						pop(@az);
						$vp--;
					}
					$aux = 1;
					next;
				}
			} else {
				$aux = 0;
			}
		# if we're a verb outside of a VP chunk (or an adjective that
		# ends in -ed or -ing), apply verb lemmatization
		} elsif (exists $ay[$i]->[2] && ($ay[$i]->[2] =~ /^V/ ||
										 ($ay[$i]->[2] =~ /^J/ && ($ay[$i]->[1] =~ /ed$/ || $ay[$i]->[1] =~ /ing$/)))) {
			$ay[$i]->[1] = vlemma($ay[$i]->[1]);
		}

		push(@az, join("/", @{$ay[$i]}));
		if ($vp >= 0) {
			$vp++;
		}
	}
	print $ax[0], "\t", $ax[1], "\t", join(" ", @az), "\n";
}
close($file);
