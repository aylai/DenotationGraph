#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin";
use simple;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

$| = 1;

# load SVO triples - initializes things for calls to getVPs
loadVPs($ARGV[1]);

my @dep = ();
my @X = ();
my @Y = ();
my @type = ();
my $n = 0;
my $file;

# list of light verbs - can not exist by themselves, without a direct object
my %light = ();
open($file, "$sdir/../data/light-verbs.txt");
while (<$file>) {
	chomp($_);
	$light{$_} = 1;
}
close($file);

# in case of an "X to Y" vp chunk, this indicates various Xs with different behaviours:
# 1 - can drop "X to"
# 2 - can drop "to Y"
# 3 - can drop both "X to" and "to Y"
my %splitTo = ();
#$splitTo{"be ready"} = 0;
#$splitTo{"get ready"} = 0;
#$splitTo{"prepare"} = 0;
#$splitTo{"pretend"} = 0;

$splitTo{"appear"} = 1;
$splitTo{"attempt"} = 1;
$splitTo{"be about"} = 1;
$splitTo{"begin"} = 1;
$splitTo{"go"} = 1;
$splitTo{"seem"} = 1;
$splitTo{"start"} = 1;
$splitTo{"struggle"} = 1;
$splitTo{"try"} = 1;

$splitTo{"line up"} = 2;
$splitTo{"pause"} = 2;
$splitTo{"wait"} = 2;

$splitTo{"bend down"} = 3;
$splitTo{"bend over"} = 3;
$splitTo{"crouch"} = 3;
$splitTo{"dive"} = 3;
$splitTo{"gather"} = 3;
$splitTo{"kneel"} = 3;
$splitTo{"kneel down"} = 3;
$splitTo{"lean in"} = 3;
$splitTo{"lean over"} = 3;
$splitTo{"leap"} = 3;
$splitTo{"jump"} = 3;
$splitTo{"jump up"} = 3;
$splitTo{"reach"} = 1;
$splitTo{"reach out"} = 3;
$splitTo{"reach up"} = 3;
$splitTo{"run"} = 3;
$splitTo{"sit"} = 3;
$splitTo{"sit down"} = 3;
$splitTo{"stop"} = 3;
$splitTo{"walk"} = 3;
$splitTo{"wind up"} = 3;
$splitTo{"work"} = 3;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	# reading a caption
	if ($#ax == 2) {
		my @ay = split(/ /, $ax[2]);
		my ($next, $prev) = breakSlash(\@ay, 1);

		# get the SVOs that are supposed to be in this caption
		my $c = countVPs($ax[0]);
		for (my $i = 0; $i < $c; $i++) {
			# get the $ith SVO
			my ($subj, $vp, $dobj, $ssubj, $svp, $sdobj) = getVP(\@ay, $next, $prev, $ax[0], $i);

			# check for missing SVO (-2 is missing, -1 is there isn't one)
			if ($vp == -2) {
				print STDERR "$ax[0]\tmissing VP\t$svp\n";
				next;
			}
			if ($subj == -2) {
				print STDERR "$ax[0]\tmissing SUBJ\t$svp\t$ssubj\n";
				next;
			}
			if ($dobj == -2) {
				print STDERR "$ax[0]\tmissing DOBJ\t$svp\t$sdobj\n";
			}

			# $vpE is the end of the VP (including direct object if one exists)
			my $vpE = $vp;
			if ($dobj >= 0) {
				$vpE = $dobj;
			}

			# generate the rule to extract the SVO from the complex sentence
			# @aX - left hand side of rule
			# @aY - right hand side of rule
			# $drop - are we actually going to drop anything?
			# $split - is there a subj and a VP?
			my $drop = 0;
			my $split = 0;
			my @aX = ();
			my @aY = ();
			push(@aX, "B");
			push(@aY, "B");
			for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
				if ($j == $subj) {
					push(@aX, $ay[$j]->[0]);
					push(@aY, $ay[$j]->[0]);
					$split |= 1;
				} elsif ($j >= $vp && $j <= $vpE) {
					push(@aX, $ay[$j]->[0]);
					push(@aY, $ay[$j]->[0]);
					$split |= 2;
				} else {
					push(@aX, $ay[$j]->[0]);
					$drop = 1;
				}
			}
			push(@aX, "E");
			push(@aY, "E");

			# if there are actual things that the rule would drop, generate it
			if ($drop == 1) {
				# differentiate between extracting an SVO and extracting a VP
				if ($split == 2) {
					addTransformation("", join(" ", @aX), join(" ", @aY), "-COMPLEX-VERB", \@dep, \@X, \@Y, \@type, \$n);
				} else {
					addTransformation("", join(" ", @aX), join(" ", @aY), "-COMPLEX", \@dep, \@X, \@Y, \@type, \$n);
				}
			}

			# if there's a subject and a VP, generate rules to split them
			if ($split == 3) {
				# generate the grab the subject rule
				# @aX - left side of rule
				# @aY - right side of rule
				@aX = ();
				@aY = ();
				push(@aX, "B");
				push(@aY, "B");
				for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
					if ($j == $subj) {
						push(@aX, $ay[$j]->[0]);
						push(@aY, $ay[$j]->[0]);
					} elsif ($j >= $vp && $j <= $vpE) {
						push(@aX, $ay[$j]->[0]);
					}
				}
				push(@aX, "E");
				push(@aY, "E");
				addTransformation("", join(" ", @aX), join(" ", @aY), "-VERB", \@dep, \@X, \@Y, \@type, \$n);

				# generate the grab the VP rule
				# @aX - left side of rule
				# @aY - right side of rule
				@aX = ();
				@aY = ();
				push(@aX, "B");
				push(@aY, "B");
				for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
					if ($j == $subj) {
						push(@aX, $ay[$j]->[0]);
					} elsif ($j >= $vp && $j <= $vpE) {
						push(@aX, $ay[$j]->[0]);
						push(@aY, $ay[$j]->[0]);
					}
				}
				push(@aX, "E");
				push(@aY, "E");
				addTransformation("", join(" ", @aX), join(" ", @aY), "-SUBJ", \@dep, \@X, \@Y, \@type, \$n);
			}

			# if there is a VP and the VP includes a direct object,
			# generate rules to split them
			if (($split & 2) != 0 && $dobj >= 0) {
				# generate the grab the direct object from the VP rule
				# @aX - left side of rule
				# @aY - right side of rule
				@aX = ();
				@aY = ();
				push(@aX, "B");
				push(@aY, "B");
				for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
					if ($j >= $vp && $j <= $vpE) {
						if ($j == $dobj) {
							my $k = $j;
							push(@aX, $ay[$k]->[0]);
							push(@aY, $ay[$k]->[0]);
							for ($k = $j + 1; $next->[$k] != 0; $k += $next->[$k]) {
								push(@aX, $ay[$k]->[0]);
								push(@aY, $ay[$k]->[0]);
							}
							push(@aX, $ay[$k]->[0]);
							push(@aY, $ay[$k]->[0]);
						} else {
							push(@aX, $ay[$j]->[0]);
						}
					}
				}
				push(@aX, "E");
				push(@aY, "E");
				addTransformation("", join(" ", @aX), join(" ", @aY), "-TVERB", \@dep, \@X, \@Y, \@type, \$n);
				
				# generate the grab the verb (including particle) from the VP chunk rule
				# @aX - left side of rule
				# @aY - right side of rule
				my @t = ();
				@aX = ();
				@aY = ();
				push(@aX, "B");
				push(@aY, "B");
				for (my $j = 0; $j <= $#ay; $j += $next->[$j]) {
					if ($j >= $vp && $j <= $dobj) {
						if ($j < $dobj) {
							push(@aX, $ay[$j]->[0]);
							push(@aY, $ay[$j]->[0]);
							for (my $k = 0; $k < $next->[$j]; $k++) {
								push(@t, join("/", @{$ay[$j + $k]}));
							}
						} else {
							push(@aX, $ay[$j]->[0]);
						}
					}
				}
				push(@aX, "E");
				push(@aY, "E");
				
				# check if the verb is a light verb - if it is, do not
				# generate the rule for example: "doing a jump" is not
				# "doing" (okay, technically it is, but "doing" is so
				# generic as to be unusable)
				if (not exists $light{plain(join(" ", @t))}) {
					addTransformation("", join(" ", @aX), join(" ", @aY), "-DOBJ", \@dep, \@X, \@Y, \@type, \$n);
				}
			}

			# if there's a VP, check if contains a TO, and needs to be split up
			if (($split & 2) != 0) {
				# @az = tokens seen in the VP so far (left of the TO)
				my @az = ();
				my $st = $vp;
				# look for TOs in the VP chunk
				for (my $j = $vp; $j < $vp + $next->[$vp]; $j++) {
					if (not $ay[$j]->[1] =~ /^[\[\]]/) {
						if ($ay[$j]->[2] eq "TO") {
							my $to = join(" ", @az);

							# see if the left side of the TO indicates that it should be split
							if (exists $splitTo{$to}) {
								if (($splitTo{$to} & 1) != 0) {
									# generate the drop the "X to" rule
									# @aX - left side of rule
									# @aY - right side of rule
									@aX = ();
									@aY = ();
									push(@aX, $ay[$vp]->[0]);
									push(@aY, $ay[$vp]->[0]);
									for (my $k = $st + 1; $k <= $j; $k++) {
										push(@aX, $ay[$k]->[0]);
									}
									for (my $k = $j + 1; $k < $vp + $next->[$vp]; $k++) {
										push(@aX, $ay[$k]->[0]);
										push(@aY, $ay[$k]->[0]);
									}
									addTransformation("", join(" ", @aX), join(" ", @aY), "-Xto", \@dep, \@X, \@Y, \@type, \$n);
								}
								
								if (($splitTo{$to} & 2) != 0) {
									# generate the drop the "to Y" rule
									# @aX - left side of rule
									# @aY - right side of rule
									@aX = ();
									@aY = ();
									push(@aX, $ay[$vp]->[0]);
									push(@aY, $ay[$vp]->[0]);
									for (my $k = $st + 1; $k < $j; $k++) {
										push(@aX, $ay[$k]->[0]);
										push(@aY, $ay[$k]->[0]);
									}
									for (my $k = $j; $k < $vp + $next->[$vp] - 1; $k++) {
										push(@aX, $ay[$k]->[0]);
									}
									push(@aX, $ay[$vp + $next->[$vp] - 1]->[0]);
									push(@aY, $ay[$vp + $next->[$vp] - 1]->[0]);

									# if there's a direct object, we need to drop the direct object
									# since "X to Y DOBJ" -> "X", not "X DOBJ"
									# (e.g., "jump to catch Frisbee" -> "jump" not "jump Frisbee")
									# also, we need subject/verb split rules that don't involve a direct object
									# since, it can disappear from the VP when we drop the "to Y"
									# and "SUBJ X to Y" -> "SUBJ X" -> "Y" / "SUBJ"
									# (e.g., "man run to tag" -> "man run" -> "run" / "man")
									if ($dobj >= 0) {
										# if there is a subject, generate rules for the new split
										if ($split == 3) {
											# generate the "SUBJ Y" -> "SUBJ" rule
											# @bX - left side of rule
											# @bY - right side of rule
											my @bX = ();
											my @bY = ();
											push(@bX, "B");
											push(@bY, "B");
											for (my $k = 0; $k <= $#ay; $k += $next->[$k]) {
												if ($k == $subj) {
													push(@bX, $ay[$k]->[0]);
													push(@bY, $ay[$k]->[0]);
												} elsif ($k == $vp) {
													foreach (@aY) {
														push(@bX, $_);
													}
												}
											}
											push(@bX, "E");
											push(@bY, "E");
											addTransformation("", join(" ", @bX), join(" ", @bY), "-VERB", \@dep, \@X, \@Y, \@type, \$n);
											
											# generate the "SUBJ Y" -> "Y" rule
											# @bX - left side of rule
											# @bY - right side of rule
											@bX = ();
											@bY = ();
											push(@bX, "B");
											push(@bY, "B");
											for (my $k = 0; $k <= $#ay; $k += $next->[$k]) {
												if ($k == $subj) {
													push(@bX, $ay[$k]->[0]);
												} elsif ($k == $vp) {
													foreach (@aY) {
														push(@bX, $_);
														push(@bY, $_);
													}
												}
											}
											push(@bX, "E");
											push(@bY, "E");
											addTransformation("", join(" ", @bX), join(" ", @bY), "-SUBJ", \@dep, \@X, \@Y, \@type, \$n);
										}

										# add the direct object (technically this is wrong -
										# may need to go further, doesn't seem to cause problems though).
										push(@aX, $ay[$vp + $next->[$vp]]->[0]);
										push(@aX, "E");
										push(@aY, "E");
									}
									addTransformation("", join(" ", @aX), join(" ", @aY), "-toY", \@dep, \@X, \@Y, \@type, \$n);
								}
							} else {
								# TO is not recognized - not necessarily a problem, but may want to see if we want to handle it
								print STDERR "$ax[0]\tunrecognized TO\t$to\n";
							}

							$st = $j;
						}
						push(@az, $ay[$j]->[1]);
					}
				}
			}
		}
		printSentence($ax[0], $ax[1], \@ay, \@dep, \@X, \@Y, \@type, $n);

		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# read in rule if the index is correct ($n)
	} elsif ($#ax == 4) {
		if ($ax[0] == $n) {
			$dep[$ax[0]] = $ax[1];
			$X[$ax[0]] = $ax[2];
			$Y[$ax[0]] = $ax[3];
			$type[$ax[0]] = $ax[4];
			$n++;
		}
	}
}
close($file);
