#!/usr/bin/perl

use strict;
use warnings;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# get a list of actors
my %actor = ();
open($file, "$sdir/../data/actor.txt");
while (<$file>) {
	chomp($_);
	$actor{$_} = 1;
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	# @ax - caption
	# @ay - output caption
	# @chunk - tokens in the current chunk
	# $last - current chunk
	# $last2 - previous chunk
	my @ax = split(/ /, $_);
	my @ay = ();
	my @chunk = ();
	my $last = "";
	my $last2 = "";

	for (my $i = 0; $i <= $#ax; $i++) {
		# start of new chunk
		if ($ax[$i] =~ /^\[/) {
			$last = $ax[$i];
		# end of current chunk - process it
		} elsif ($ax[$i] =~ /^\]/) {
			# is this an NP chunk with two or more tokens?
			if ($last eq "[NP" && $#chunk > 0) {
				my @aw = split(/\//, $chunk[$#chunk]);
				# is the last token "someone" or "something"?
				if (lc($aw[0]) eq "something" || lc($aw[0]) eq "someone") {
					# @rev - output to replace the chunk, in reverse order
					# @tchunk - copy of @chunk.  We'll be pulling tokens off of this one.
					# $rchunk - current chunk we're working on in @rev
					my @rev = ();
					my @tchunk = @chunk;
					my $rchunk = "";

					# make "someone/thing" its own NP chunk
					push(@rev, "]");
					push(@rev, pop(@tchunk));
					push(@rev, "[NP");

					@aw = split(/\//, $tchunk[$#tchunk]);
					# is the previous token a plural noun? - this means its usually actually a verb
					if ($aw[1] eq "NNS") {
						push(@rev, "]");
						# if the previous token isn't an actor, turn it into a verb
						# either way, remainder of the NP chunk is its own NP chunk
						if (!exists $actor{lc($aw[0])}) {
							push(@rev, "$aw[0]/VBZ");
							push(@rev, "[VP");
							pop(@tchunk);
							if ($#tchunk >= 0) {
								push(@rev, "]");
								while ($#tchunk >= 0) {
									push(@rev, pop(@tchunk));
								}
								push(@rev, "[NP");
							}
						} else {
							while ($#tchunk >= 0) {
								push(@rev, pop(@tchunk));
							}
							push(@rev, "[NP");
						}
					# or a singular noun or possessive pronoun?
					} elsif ($aw[1] eq "NN" || $aw[1] eq "PRP\$") {
						# turn the remainder of the NP into an NP chunk
						push(@rev, "]");
						while ($#tchunk >= 0) {
							push(@rev, pop(@tchunk));
						}
						push(@rev, "[NP");
					# otherwise...
					} else {
						# process through the remainder of the chunk backwards
						# create chunks based on the tags of the tokens we encounter
						# if two adjacent tokens would belong to the same type of chunk, they belong to the same chunk
						# otherwise just make a new chunk
						while ($#tchunk >= 0) {
							# $w - current token we're working with
							# $t - chunk type
							my $w = pop(@tchunk);
							my $t = "";
							@aw = split(/\//, $w);
							
							# verbs go in VP chunks
							if ($aw[1] =~ /^V/) {
								$t = "[VP";
							# adverbs (or "busy") go in ADVP chunks
							} elsif ($aw[1] eq "RB" || lc($aw[0]) eq "busy") {
								$t = "[ADVP";
							# prepositions go in either SBARs or PPs
							} elsif ($aw[1] eq "IN") {
								if (lc($aw[0]) eq "while" || lc($aw[0]) eq "as") {
									$t = "[SBAR";
								} else {
									$t = "[PP";
								}
							# unexpected tag - place the token back on tchunk, and let something else handle it
							} else {
								push(@tchunk, $w);
								last;
							}

							# we're not in the middle of a chunk - create the ending boundary, and note that we're in the new chunk type
							if ($rchunk eq "") {
								push(@rev, "]");
								push(@rev, $w);
								$rchunk = $t;
							# new chunk type and current chunk type are the same - no need to mess with boundaries
							} elsif ($rchunk eq $t) {
								push(@rev, $w);
							# new chunk type and current chunk type are different - create boundaries
							} else {
								push(@rev, $rchunk);
								push(@rev, "]");
								push(@rev, $w);
								$rchunk = $t;
							}
						}

						# we ran into an unexpected tag - we're going to try another method of creating @rev
						if ($#tchunk != -1) {
							# restart everything
							$rchunk = "";
							@tchunk = @chunk;
							@rev = ();

							# "someone/thing" gets an NP chunk
							push(@rev, "]");
							push(@rev, pop(@tchunk));
							push(@rev, "[NP");

							# if the next thing is a VBG, we'll make a VP chunk out of it, and then an NP chunk out of everything else
							@aw = split(/\//, $tchunk[$#tchunk]);
							if ($aw[1] eq "VBG") {
								push(@rev, "]");
								push(@rev, pop(@tchunk));
								if ($#tchunk >= 1) {
									@aw = split(/\//, $tchunk[$#tchunk - 0]);
									my @av = split(/\//, $tchunk[$#tchunk - 1]);
									if ($aw[1] eq "CC" && $av[1] eq "VBG") {
										push(@rev, pop(@tchunk));
										push(@rev, pop(@tchunk));
									}
								}
								push(@rev, "[VP");

								if ($#tchunk >= 0) {
									@aw = split(/\//, $tchunk[$#tchunk]);
									if (not $aw[1] =~ /^[A-Z]/) {
										push(@rev, pop(@tchunk));
									}
								}

								if ($#tchunk >= 0) {
									push(@rev, "]");
									while ($#tchunk >= 0) {
										push(@rev, pop(@tchunk));
									}
									push(@rev, "[NP");
								}
							}
						}
					}

					# if we managed to use all of the chunk, we've got a valid new chunk (reversed) in @rev
					if ($#tchunk == -1) {
						if ($rchunk ne "") {
							# if the left most chunk in @rev and the chunk prior to the NP chunk we've been operating on are both VP chunks and adjacent to each other
							# we want to combine them
							if ($rchunk eq "[VP" && $last2 eq "[VP" && $ay[$#ay] eq "]") {
								pop(@ay);
							# if the left most chunk in @rev is a VP and the prior chunk is [PP to ], then it's really [VP to ... ]
							} elsif ($rchunk eq "[VP" && $last2 eq "[PP" &&
									 $ay[$#ay - 0] eq "]" && lc($ay[$#ay - 1]) eq "to/to" && $ay[$#ay - 2] eq "[PP") {
								pop(@ay);
								my $x = pop(@ay);
								pop(@ay);
								push(@ay, "[VP");
								push(@ay, $x);
							# otherwise, there's nothing special to do - place the beginning boundary of the first chunk of @rev
							} else {
								push(@rev, $rchunk);
							}
						}

						# dump @rev to the output and update variables
						foreach (reverse(@rev)) {
							push(@ay, $_);
						}
						@chunk = ();
						$last2 = $last;
						$last = "";
						next;
					} else {
#						print join(" ", @chunk), "\n";
					}
				}
			}

			# store the chunk
			push(@ay, $last);
			foreach (@chunk) {
				push(@ay, $_);
			}
			push(@ay, "]");
			@chunk = ();
			$last2 = $last;
			$last = "";
		# if we're inside a chunk keep track of the tokens
		} elsif ($last ne "") {
			push(@chunk, $ax[$i]);
		# otherwise just dump it output
		} else {
			push(@ay, $ax[$i]);
		}
	}

	print join(" ", @ay), "\n";
}
close($file);
