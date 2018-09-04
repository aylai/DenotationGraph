#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use parse;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;
# compound terms - if the head noun is the index, and the last word of
# the modifier chunk is one of the entries, add it to the head noun.
# i.e., "wooded" (modifier) + "area" (head noun) --> "wooded area" (head noun)
my %compounds = ();
open($file, "$sdir/../data/person.txt");
while (<$file>) {
	chomp($_);
	$compounds{$_} = {};
	$compounds{$_}->{"adult"} = 1;
	$compounds{$_}->{"baby"} = 1;
	$compounds{$_}->{"child"} = 1;
	$compounds{$_}->{"teen"} = 1;
	$compounds{$_}->{"toddler"} = 1;
}
close($file);
if (not exists $compounds{"area"}) {
	$compounds{"area"} = {};
}
$compounds{"area"}->{"wooded"} = 1;
if (not exists $compounds{"park"}) {
	$compounds{"park"} = {};
}
$compounds{"park"}->{"skate"} = 1;
if (not exists $compounds{"player"}) {
	$compounds{"player"} = {};
}
open($file, "$sdir/../data/player.txt");
while (<$file>) {
	chomp($_);
	$compounds{"player"}->{$_} = 1;
}
close($file);

# second argument is the specific entity modifier chunking file.  if
# there isn't one, use the default (third argument).
unless (-e $ARGV[2]) {
	$ARGV[2] = $ARGV[3];
}

my %chunk = ();
open($file, $ARGV[2]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$chunk{join(" ", @ax)} = join("\t", @ax[1 .. $#ax]);
}
close($file);

my %mod = ();
my %start = ();
my %end = ();

# get the possible noun lemmatizations (head nouns of all NP chunks)
open($file, $ARGV[1]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	for (my $i = 0; ($i * 5 + 7) <= $#ax; $i++) {
		my $w = tokenize($ax[$i * 5 + 9]);
		nlemmaAdd($w);
	}
}
close($file);

# get the rewrite rules for determiners
my %count = ();
open($file, "$sdir/../data/count.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	$count{$ax[0]} = $ax[1];
}
close($file);

# for each EN chunk, we're going to grab the determiner/modifier/head splits.
my %npd = ();
my %npm = ();
my %nph = ();
my %np = ();
open($file, $ARGV[1]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	for (my $i = 0; ($i * 5 + 7) <= $#ax; $i++) {
		my $id = $ax[0] . "#" . $i;

		my @ay = split(/ /, $ax[$i * 5 + 7]);
		my @np = @ay;
		$npd{$id} = scalar @ay;
		@ay = split(/ /, $ax[$i * 5 + 8]);
		@np = (@np, @ay);
		$npm{$id} = scalar @ay;
		@ay = split(/ /, $ax[$i * 5 + 9]);
		@np = (@np, @ay);
		$nph{$id} = scalar @ay;
		$np{$id} = join(" ", @np);
	}
}
close($file);

# produce the noun lemmatized captions.
# when you encounter an EN chunk, confirm the determiner-modifier-head split
# grab the token IDs, and then lemmatize the EN chunk
# when you encounter a noun tagged word outside an EN chunk, lemmatize it
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/ /, $ax[2]);
	my @az = ();
	breakSlash(\@ay, 1);
	for (my $i = 0; $i <= $#ay; $i++) {
		if ($ay[$i]->[1] eq "[EN") {
			my $enid = $ax[0] . "#" . $ay[$i]->[2];
			my $n = 0;
			my $j = 0;

			# go through the EN chunk, looking for NP chunks
			while ($i <= $#ay) {
				push(@az, join("/", @{$ay[$i]}));
				if ($ay[$i]->[1] =~ /^\[/) {
					$j++;

					# found an NP chunk at the right depth
					if ($j == 2 && $ay[$i]->[1] eq "[NP") {
						my $npid = $enid . "#" . $n;

						# see if we know about this NP chunk
						if (exists $np{$npid}) {
							# go through the NP chunk, and make sure it is what we expect
							# collect the determiner, modifier, and head parts while we're at it
							my @np = split(/ /, $np{$npid});
							my @npd = ();
							my @npm = ();
							my @nph = ();
							my $k;
							my $l = $j + 1;
							for ($k = 0; ($i + 1 + $k) <= $#ay; $k++) {
								my @az = @{$ay[$i + 1 + $k]};
								my $z = shift(@az);
								my $token = join("/", @az);
								unshift(@az, $z);
								if ($token =~ /^\[/) {
									$l++;
								} elsif ($token =~ /^\]/) {
									$l--;
								}

								if ($l == $j && $token =~ /^\]/ && $k == (scalar @np)) {
									last;
								} elsif ($k > $#np || $token ne $np[$k]) {
									$k = -1;
									last;
								}

								if ($k < $npd{$npid}) {
									push(@npd, \@az);
								} elsif ($k < ($npd{$npid} + $npm{$npid})) {
									push(@npm, \@az);
								} else {
									push(@nph, \@az);
								}
							}

							if ($k == -1 || ($i + $k) > $#ay) {
								print STDERR $npid, ": mismatch\n";
							} else {
								# NP chunk matches what we expect - break into NPD, NPM (NPMC), and NPH chunks, and lemmatize

								# if the head noun is a comma (this will happen) grab the last
								# word of the modifier chunk or the determiner chunk.  If this
								# is not possible, retag it as a noun.  (Yes, this is to avoid
								# a problem later on, no this is the best solution at this
								# point.  Really needs to be fixed during tagging/chunking.)
								if ($#nph == 0 && $nph[0]->[1] eq "," && $nph[0]->[2] eq ",") {
									if ($#npm >= 0) {
										$nph[0] = pop(@npm);
									} elsif ($#npd >= 0) {
										$nph[0] = pop(@npd);
									} else {
										$nph[0]->[2] = "NN";
									}
								}

								# process the determiner - strip "at least"
								if ($#npd >= 2 && lc($npd[0]->[1]) eq "at" && lc($npd[1]->[1]) eq "least") {
									shift(@npd);
									shift(@npd);
								}

								# check if the determiner is a count
								my @dt = ();
								foreach (@npd) {
									push(@dt, lc($_->[1]));
								}
								my $dt = join(" ", @dt);
								if (exists $count{$dt}) {
									# if the count is "one", we'll just drop the determiner
									# too much junk being generated for the graph
									if ($count{$dt} ne "one") {
										# replace the last token in the determiner with the count
										while ($#npd > 0) {
											shift(@npd);
										}
										$npd[0]->[1] = $count{$dt};
										$npd[0]->[2] = "DT";
									} else {
										@npd = ();
									}
								# save the determiner if it's part of "each other"
								} elsif ($dt eq "each" && $#nph == 0 && lc($nph[0]->[1]) eq "other") {
								} else {
									@npd = ();
								}

								# check the modifier and the head
								# see if we're dealing with a compound (head needs to steal a token from the modifier)
								# see if the modifier can be further chunked
								my @npmc = ();
								if ($#npm >= 0 && $#nph >= 0) {
									# tokenize and lemmatize the head
									my @ah = ();
									foreach (@nph) {
										push(@ah, lc($_->[1]));
									}
									my $h = nlemma(join(" ", @ah));

									# tokenize the modifier
									my @am = ();
									foreach (@npm) {
										push(@am, lc($_->[1]));
									}

									# check if this is a compound noun (and we should be stealing tokens from the modifier)
									if (exists $compounds{$h} && exists $compounds{$h}->{$am[$#am]}) {
										unshift(@nph, pop(@npm));
										unshift(@ah, pop(@am));
										nlemmaAdd(join(" ", @ah));

										# remove any punctuation marks from the modifier
										# BUGBUG: is this the right thing to do?
										for (my $x = $#npm; $x >= 0; $x--) {
											if ($npm[$x]->[2] =~ /^[,\.\'\"\`]/) {
												splice(@npm, $x, 1);
											}
										}
									} else {
										my $hm = $h . " " . join(" ", @am);
										# if it's a known entity modifier chunking, store the
										# lengths of each chunk in @npmc
										if (exists $chunk{$hm}) {
											foreach (split(/\t/, $chunk{$hm})) {
												push(@npmc, scalar split(/ /, $_));
											}
										# remove any punctuation marks from the modifier
										# BUGBUG: is this the right thing to do?
										} else {
											for (my $x = $#npm; $x >= 0; $x--) {
												if ($npm[$x]->[2] =~ /^[,\.\'\"\`]/) {
													splice(@npm, $x, 1);
												}
											}
										}
									}
								}

								# lemmatize the head noun
								my @ah = ();
								foreach (@nph) {
									push(@ah, lc($_->[1]));
								}
								my $h = nlemma(join(" ", @ah));
								# if the lemmatized version is different than the head noun
								# assume the lemmatized version has the same number of tokens
								# and do a one-for-one replacement
								if ($h ne join(" ", @ah)) {
									my @ah = split(/ /, $h);
									for (my $x = 0; $x <= $#nph; $x++) {
										$nph[$x]->[1] = $ah[$x];
									}
								}

								# now rebuild the NP chunk with additional chunk boundaries
								if ($#npd >= 0) {
									push(@az, "$ax[1]/[NPD");
									$ax[1]++;

									foreach (@npd) {
										push(@az, join("/", @{$_}));
									}

									push(@az, "$ax[1]/]");
									$ax[1]++;
								}

								# build the NPM/NPMC chunks (using @npmc if it exists)
								if ($#npm >= 0) {
									push(@az, "$ax[1]/[NPM");
									$ax[1]++;

									if ($#npmc == -1) {
										foreach (@npm) {
											push(@az, join("/", @{$_}));
										}
									} else {
										push(@az, "$ax[1]/[NPMC");
										$ax[1]++;

										foreach (@npm) {
											if ($npmc[0] == 0) {
												push(@az, "$ax[1]/]");
												$ax[1]++;
												push(@az, "$ax[1]/[NPMC");
												$ax[1]++;
												shift(@npmc);
											}
											push(@az, join("/", @{$_}));
											$npmc[0]--;
										}

										push(@az, "$ax[1]/]");
										$ax[1]++;
									}

									push(@az, "$ax[1]/]");
									$ax[1]++;
								}

								if ($#nph >= 0) {
									push(@az, "$ax[1]/[NPH");
									$ax[1]++;

									foreach (@nph) {
										push(@az, join("/", @{$_}));
									}

									push(@az, "$ax[1]/]");
									$ax[1]++;
								}

								$i = $i + $k;
							}
						} else {
							print STDERR $npid, ": missing\n";
						}

						$n++;
					}
				} elsif ($ay[$i]->[1] =~ /^\]/) {
					$j--;

					if ($j == 0) {
						last;
					}
				}
				$i++;
			}
		} else {
			if ($#{$ay[$i]} == 2 && $ay[$i]->[2] =~ /^N/) {
				$ay[$i]->[1] = nlemma($ay[$i]->[1]);
			}
			push(@az, join("/", @{$ay[$i]}));
		}
	}
	print $ax[0], "\t", $ax[1], "\t", join(" ", @az), "\n";
}

close($file);
