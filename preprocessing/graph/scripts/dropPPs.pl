#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin";
use simple;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

# load NPs which are subjects - these are not prepositional objects, and should not be dropped
%subj = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$subj{$ax[1]} = 1;
}
close(file);

# load the head nouns that can be grouped together with a CC
$group = ();
$x = 0;
open(file, "$sdir/../data/ccGroup.txt");
while (<file>) {
	chomp($_);
	if ($_ eq "") {
		$x++;
	} else {
		$group{$_} = $x;
	}
}
close(file);

# do not drop the prepositional object, just drop the preposition, if
# one of the following verbs precedes the preposition ("climb up
# mountain" -> "climb mountain")
%pp3only = ();
$pp3only{"climb"} = 1;
$pp3only{"hold"} = 1;

# drop the prepositional phrase (including object) if one of the
# following verbs precedes the preposition.  For any other verb not on
# either list (%pp3only and %pp3both), the prepositional phrase should
# be ignored (can't tell if it's part of the verb or not)
%pp3both = ();
$pp3both{"bicycle"} = 1;
$pp3both{"bike"} = 1;
$pp3both{"hike"} = 1;
$pp3both{"jump"} = 1;
$pp3both{"race"} = 1;
$pp3both{"ride"} = 1;
$pp3both{"run"} = 1;
$pp3both{"skateboard"} = 1;
$pp3both{"ski"} = 1;
$pp3both{"slide"} = 1;
$pp3both{"walk"} = 1;

# list of prepositions that we can drop
# 1 - normal preposition
# 2 - the PP chunk "on" may come after the prepositional object and should be dropped as well ("with hat on")
# 3 - verb particle (must be preceded by a verb, can be in a PRT chunk)
%drop = ();
$drop{"above"} = 1;
$drop{"across"} = 1;
$drop{"against"} = 1;
$drop{"around"} = 1;
$drop{"at"} = 1;
$drop{"behind"} = 1;
$drop{"beneath"} = 1;
$drop{"beside"} = 1;
$drop{"by"} = 1;
$drop{"down"} = 3;
$drop{"in"} = 1;
$drop{"in front of"} = 1;
$drop{"into"} = 1;
$drop{"for"} = 1;
$drop{"from"} = 1;
$drop{"near"} = 1;
$drop{"next to"} = 1;
$drop{"on"} = 1;
$drop{"on top of"} = 1;
$drop{"over"} = 1;
$drop{"through"} = 1;
$drop{"towards"} = 1;
$drop{"with"} = 2;
$drop{"under"} = 1;
$drop{"underneath"} = 1;
$drop{"up"} = 3;

@dep = ();
@X = ();
@Y = ();
@type = ();
$n = 0;
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	# parse caption, id PP chunks to drop
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);
		$iprev = 0;
LOOP:
		# process through the caption, backwards
		# that way if we have chained PPs, we'll essentially treat
		# them as if each PP attaches to the previous prepositional
		# object.
		for ($i = $#ay; $i >= 0; $i = $iprev) {
			$iprev = $i - $prev->[$i];
			$i1 = $i - $prev->[$i] + 1;

			# the main variables to watch are:
			# $prepB + $prepE - indicates the beginning and end of the preposition
			# $objB + $objE - indicates the beginning and end of the prepositional object
			# $verbB + $verbE - indicates the beginning and end of a previous adjacent VP
			# $joinB + $joinE - indicates the beginning and end of everything prior to the above that needs to be dropped
			# (typically things that indicate the PP is part of a
			# relative clause - i.e., "which is in a box" - 
			# need to drop the entire clause)
			# $prepT is the highest preposition type (types stored in
			# %drop) of the prepositions which are going to be dropped

			# encountered a PP chunk - determine if, and what we need to drop
			if ($ay[$i1]->[1] eq "[PP") {
				@as = ();
				$prepT = -1;

				# check the insides of the PP chunk, and find each
				# preposition (due to the way chunking currently
				# works, some groups of prepositions get put into
				# their own chunk (e.g., "up and down", etc.)  make
				# sure each preposition is one we can drop, and get
				# the highest type
				for ($j = $i1 + 1; $j < ($i1 + $next->[$i1] - 1); $j++) {
					if ($ay[$j]->[2] eq "CC") {
						$s = join(" ", @as);
						if (not exists $drop{$s}) {
							next LOOP;
						}
						if ($prepT < $drop{$s}) {
							$prepT = $drop{$s};
						}
						@as = ();
					} else {
						push(@as, lc($ay[$j]->[1]));
					}
				}
				$s = join(" ", @as);
				if (not exists $drop{$s}) {
					next;
				}

				$prepB = $i1;
				$prepE = $i + 1;
				if ($prepT < $drop{$s}) {
					$prepT = $drop{$s};
				}
			# encountered a PRT chunk - may really be part of a PP (or
			# something PPish that we want to drop) - determine if we
			# want to drop it
			} elsif ($ay[$i1]->[1] eq "[PRT") {
				@as = ();
				$prepT = -1;

				# as above - check the insides of the PRT chunk, however
				# the highest type seen must be 3 (i.e., verb particle)
				for ($j = $i1 + 1; $j < ($i1 + $next->[$i1] - 1); $j++) {
					if ($ay[$j]->[2] eq "CC") {
						$s = join(" ", @as);
						if (not exists $drop{$s}) {
							next LOOP;
						}
						if ($prepT < $drop{$s}) {
							$prepT = $drop{$s};
						}
						@as = ();
					} else {
						push(@as, lc($ay[$j]->[1]));
					}
				}
				$s = join(" ", @as);
				if (not exists $drop{$s}) {
					next;
				}

				$prepB = $i1;
				$prepE = $i + 1;
				if ($prepT < $drop{$s}) {
					$prepT = $drop{$s};
				}

				if ($prepT != 3) {
					next;
				}
			} else {
				next;
			}

			# check if we're the right PP of a PP CC PP construction
			$j = $prepB - 1;
			if ($j > 1 && $ay[$j]->[2] eq "CC") {
				$j = $j - 1;
				$j = $j - $prev->[$j] + 1;

				# the left PP is sometimes chunked as an ADVP... which
				# was probably dropped by "dropEventMods.pl" - not
				# sure this does what I want it to do
				# assume that the left PP has no internal CCs
				if ($ay[$j]->[1] eq "[PP" || $ay[$j]->[1] eq "[ADVP") {
					@as = ();
					for ($k = $j + 1; $k < ($j + $next->[$j] - 1); $k++) {
						push(@as, lc($ay[$k]->[1]));
					}
					$s = join(" ", @as);
					if (exists $drop{$s}) {
						if ($prepT < $drop{$s}) {
							$prepT = $drop{$s};
						}
						$prepB = $j;
					}
				# as above, but for PRTs - we want to make sure it's a varb particle
				} elsif ($ay[$j]->[1] eq "[PRT") {
					@as = ();
					for ($k = $j + 1; $k < ($j + $next->[$j] - 1); $k++) {
						push(@as, lc($ay[$k]->[1]));
					}
					$s = join(" ", @as);
					if (exists $drop{$s} && $drop{$s} == 3) {
						$prepT = $drop{$s};
						$prepB = $j;
					}
				}
			}

			# check if we need to drop the object (i.e., is this
			# really a droppable verb particle? - e.g., "climb up
			# stair" -> "climb stair")
			$skipen = 0;
			if ($prepT == 3) {
				$j = $prepB - 1;
				$j = $j - $prev->[$j] + 1;
				if ($ay[$j]->[1] ne "[VP") {
					next;
				}

				@as = ();
				for ($k = $j + 1; $k < ($j + $next->[$j] - 1); $k++) {
					push(@as, lc($ay[$k]->[1]));
				}
				$s = join(" ", @as);

				if (exists $pp3only{$s}) {
					$skipen = 1;
				} elsif (!exists $pp3both{$s}) {
					next;
				}
			}

			# if we're not skipping the object
			$objB = -1;
			$objE = -1;
			$j = $prepE;
			if ($skipen != 1) {
				# make sure we haven't fallen off the end of the
				# caption, that it's an EN chunk, and not a subject
				if ($j > $#ay || $ay[$j]->[1] ne "[EN" || exists $subj{"$ax[0]#$ay[$j]->[2]"}) {
					next;
				}

				# grab trailing "of"s - not always stored within an EN
				# chunk for example, we want to drop "arm of person",
				# but "arm" and "person" are their own EN chunks
				$k = $j + $next->[$j];
				while (($k + 3) <= $#ay && $ay[$k + 0]->[1] eq "[PP" && lc($ay[$k + 1]->[1]) eq "of" && $ay[$k + 2]->[1] eq "]" && $ay[$k + 3]->[1] eq "[EN") {
					$k = $k + 3;
					$k = $k + $next->[$k];
				}

				# check if the object is "X CC Y" - use the first EN chunks to determine that
				if ($k <= $#ay && $ay[$k]->[2] eq "CC") {
					$l = $k + $next->[$k];
					if ($l <= $#ay && $ay[$l]->[1] eq "[EN") {
						$en1 = entityString(\@ay, $next, $j);
						$en2 = entityString(\@ay, $next, $l);
						if (exists $group{$en1} && exists $group{$en2} && $group{$en1} == $group{$en2}) {
							$k = $l + $next->[$l];
							# again, grab the "of"s if needed
							while (($k + 3) <= $#ay && $ay[$k + 0]->[1] eq "[PP" && lc($ay[$k + 1]->[1]) eq "of" && $ay[$k + 2]->[1] eq "]" && $ay[$k + 3]->[1] eq "[EN") {
								$k = $k + 3;
								$k = $k + $next->[$k];
							}
						}
					}
				}

				# if we can have a trailing "on" and we do, add that
				if ($prepT == 2 && ($k + 2) <= $#ay &&
					$ay[$k + 0]->[1] eq "[PP" && $ay[$k + 1]->[1] eq "on" && $ay[$k + 2]->[1] eq "]") {
					$k += 3;
				}

				# if we have a "OBJ that ...", we need to drop the rest of the caption
				if ($k <= $#ay && $ay[$k]->[1] eq "[EN") {
					if (entityString(\@ay, $next, $k) eq "that") {
						$k = $#ay + 1;
					}
				}

				$objB = $j;
				$objE = $k;
			}

			# determine if there is a previous VP chunk
			# if there is, check if it is "be" or "dressed" - if so, we'll drop that as part of the PP
			# i.e., we don't want "is in X" -> "is".
			$verbB = -1;
			$verbE = -1;
			$j = $prepB - 1;
			$j = $j - $prev->[$j] + 1;
			if ($j >= 0 && $ay[$j]->[1] eq "[VP") {
				@as = ();
				for ($k = $j + 1; $k < ($j + $next->[$j] - 1); $k++) {
					push(@as, lc($ay[$k]->[1]));
				}
				$verbT = join(" ", @as);

				if ($verbT eq "be" || $verbT eq "dressed") {
					$verbB = $j;
					$verbE = $prepB;
					
				}
			}

			# look for other terms (which/who/that or CCs) that will need to be dropped if we drop the PP
			# "that is in X" should be dropped entirely, and not just leave "that"
			# note: we are not guaranteed to drop the tokens found, but it'll be that'll be determined next
			$joinB = -1;
			if ($verbB == -1) {
				$joinE = $prepB;
			} else {
				$joinE = $verbB;
			}
			$j = $joinE - 1;
			$j = $j - $prev->[$j] + 1;
			if ($j >= 0) {
				if ($ay[$j]->[1] eq "[EN") {
					$joinT = entityString(\@ay, $next, $j);
					if ($joinT eq "which" || $joinT eq "who" || $joinT eq "that") {
						$joinB = $j;
					}
				} elsif ($ay[$j]->[2] eq "CC") {
					$joinT = lc($ay[$j]->[1]);
					$joinB = $j;
				}
			}


			# $j - beginning of stuff to drop, $k - end of stuff to drop
			# $joinB - $joinE may or may not be dropped
			if ($verbB == -1) {
				$j = $prepB;
			} else {
				$j = $verbB;
			}
			if ($objB == -1) {
				$k = $prepE;
			} else {
				$k = $objE;
			}

			if ($joinB != -1) {
				# if the $join stuff would be the new end of string or end of a clause, drop it
				# (no trailing that/which/who or CCs)
				if ($k > $#ay) {
					$j = $joinB;
				} else {
					@as = ();
					for ($l = $k + 1; $l < ($k + $next->[$k] - 1); $l++) {
						push(@as, lc($ay[$l]->[1]));
					}
					$s = join(" ", @as);
					if ($s eq "as" || $s eq "while") {
						$j = $joinB;
					}
				}

				if ($j != $joinB) {
					# if the next thing would be a VP, and we're dropping a VP, add the $join stuff
					# note: we're pretty much assuming that the VP we're dropping is "be"
					if ($ay[$k]->[1] eq "[VP") {
						if ($verbB != -1) {
							$j = $joinB;
						}
					}
				}

				# finally, if $join is non-null (so some sort of connector precedes the PP),
				# and we're not dropping a VP, check if the next thing is the VP "be",
				# in which case, drop that as well
				if ($j != $joinB) {
					if ($verbB == -1) {
						if ($ay[$k]->[1] eq "[VP") {
							@as = ();
							for ($l = $k + 1; $l < ($k + $next->[$k] - 1); $l++) {
								push(@as, lc($ay[$l]->[1]));
							}
							$s = join(" ", @as);
							if ($s eq "be") {
								$k = $k + $next->[$k];
							}
						}
					}
				}
			}


			# make sure we're not removing the entire string - because if we are, there's no point
			if ($j > 0 || $k <= $#ay) {
				# @aX - left side of rule
				# @aY - right side of rule
				@aX = ();
				@aY = ();

				# check if there's a prior chunk that we can insert ourselves into -
				# the if statement covers the case where there isn't, the else covers the case where there eis
				if (($j - 1) < 0 || (not $ay[$j - 1]->[1] =~ /^\]/)) {
					push(@aX, ($j - 1) >= 0 ? $ay[$j - 1]->[0] : "B");
					push(@aY, ($j - 1) >= 0 ? $ay[$j - 1]->[0] : "B");
					foreach (@ay[$j .. $k - 1]) {
						push(@aY, join("/", @{$_}));
					}
					push(@aX, ($k <= $#ay) ? $ay[$k]->[0] : "E");
					push(@aY, ($k <= $#ay) ? $ay[$k]->[0] : "E");
				} else {
					push(@aX, ($j - 2) >= 0 ? $ay[$j - 2]->[0] : "B");
					push(@aY, ($j - 2) >= 0 ? $ay[$j - 2]->[0] : "B");
					foreach (@ay[$j .. $k - 1]) {
						push(@aY, join("/", @{$_}));
					}
					push(@aX, $ay[$j - 1]->[0]);
					push(@aY, $ay[$j - 1]->[0]);
				}

				# grab the preposition being dropped
				@aprep = ();
				for ($l = $prepB; $l < $prepE; $l++) {
					if (not $ay[$l]->[1] =~ /^[\[\]]/) {
						push(@aprep, $ay[$l]->[1]);
					}
				}

				# if there is an object being dropped, the label will be PP/<prep>/<obj>
				if ($objB != -1) {
					# grab the object being dropped
					@aobj = ();
					for ($l = $objB; $l < $objE; $l++) {
						if (not $ay[$l]->[1] =~ /^[\[\]]/) {
							push(@aobj, $ay[$l]->[1]);
						}
					}

					# check for a preceding CC if we're dropping an object
					# (we've probably already gotten this case earlier, but justin case)
					if ($aX[0] ne "B" && $aX[1] eq "E") {
						for ($l = 0; $l <= $#ay; $l++) {
							if ($ay[$l]->[0] eq $aX[0]) {
								if (exists $ay[$l]->[2] && $ay[$l]->[2] eq "CC") {
									shift(@aX);
									unshift(@aX, ($l > 0) ? $ay[$l - 1]->[0] : "B");

									shift(@aY);
									unshift(@aY, join("/", @{$ay[$l]}));
									unshift(@aY, ($l > 0) ? $ay[$l - 1]->[0] : "B");
									$j--;
								}
								last;
							}
						}
					}

					addTransformation("", join(" ", @aX), join(" ", @aY), "+PP/" . join(" ", @aprep) . "/" . join(" ", @aobj), \@dep, \@X, \@Y, \@type, \$n);
				# otherwise it'll be PP/<prep>
				} else {
					# make sure there isn't a preceding CC
					# (again, probably already handled this case)
					if ($aX[0] ne "B") {
						for ($l = 0; $l <= $#ay; $l++) {
							if ($ay[$l]->[0] eq $aX[0]) {
								if (exists $ay[$l]->[2] && $ay[$l]->[2] eq "CC") {
									shift(@aX);
									unshift(@aX, ($l > 0) ? $ay[$l - 1]->[0] : "B");

									shift(@aY);
									unshift(@aY, join("/", @{$ay[$l]}));
									unshift(@aY, ($l > 0) ? $ay[$l - 1]->[0] : "B");
									$j--;
								}
								last;
							}
						}
					}
					addTransformation("", join(" ", @aX), join(" ", @aY), "+PP/" . join(" ", @aprep), \@dep, \@X, \@Y, \@type, \$n);
				}

				@ay = @ay[0 .. $j - 1, $k .. $#ay];
				($next, $prev) = getNextPrev(\@ay, 1);
				$iprev = $j - 1;
			}

#			print "$ax[0]";
#
#			if ($j > 0) {
#				$l = 0;
#				while ($l < $j) {
#					if (not $ay[$l]->[1] =~ /^[\[\]]/) {
#						print " $ay[$l]->[1]";
#					}
#					$l++;
#				}
#			}
#
#			print " [";
#			while ($j < $k) {
#				if (not $ay[$j]->[1] =~ /^[\[\]]/) {
#					print " $ay[$j]->[1]";
#				}
#				$j++;
#			}
#			print " ]";
#
#			if ($k < $#ay) {
#				$j = $k;
#				$k = $#ay + 1;
#				while ($j < $k) {
#					if (not $ay[$j]->[1] =~ /^[\[\]]/) {
#						print " $ay[$j]->[1]";
#					}
#					$j++;
#				}
#			}
#			print "\n";
		}
		printSentence($ax[0], $ax[1], \@ay, \@dep, \@X, \@Y, \@type, $n);

		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# add rule if the index matches ($n)
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

close(file);
