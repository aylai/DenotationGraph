#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin";
use simple;

# get list of modifiers which only apply to the original head noun
%entmod = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	$entmod{$_} = 1;
}
close(file);

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
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);
		for ($i = 0; $i <= $#ay; $i += $next->[$i]) {
			if ($ay[$i]->[1] eq "[EN") {
				# if the head noun is "hair", do not remove modifiers
				# (probably a color word - hair by itself isn't
				# usually worth noting)
				$en = entityString(\@ay, $next, $i);
				if ($en eq "hair") {
					next;
				}

				# process each NP chunk, looking for its NPM chunk.
				for ($j = $i + 1; $next->[$j] != 0; $j += $next->[$j]) {
					if ($ay[$j]->[1] eq "[NP") {
						$k = $j + 1;
						while ($ay[$k]->[1] ne "[NPM" && $next->[$k] != 0) {
							$k += $next->[$k];
						}

						if ($ay[$k]->[1] eq "[NPM") {
							# if there are NPMC chunks in the NPM
							# chunk, they'll start immediately
							# process them one by one.
							if ($ay[$k + 1]->[1] eq "[NPMC") {
								# @ao is the IDs of the tokens in the
								# NPH chunk.  We'll use these if we
								# need to ensure that the head noun is
								# the original one.
								@ao = ();

								# @an is the sequence of NPMC chunks.
								# We'll use these to connect the
								# current NPMC chunk with the NPH
								# chunk (when we're ensuring that the
								# head noun is the original one)
								@an = ();

								$l = $k + $next->[$k];
								push(@ao, $ay[$l - 1]->[0]);
								for ($m = 0; $m < $next->[$l]; $m++) {
									push(@ao, $ay[$l + $m]->[0]);
								}

								for ($l = $k + 1; $next->[$l] != 0; $l += $next->[$l]) {
									push(@an, $ay[$l]->[0]);
								}

								for ($k = $k + 1; $next->[$k] != 0; $k += $next->[$k]) {
									# take the first token off of @an,
									# since we're currently processing
									# that NPMC chunk
									shift(@an);
									if ($ay[$k]->[1] eq "[NPMC") {
										# @aX: left side of rule
										# @aY: right side of rule
										# @as: string of thing being removed
										@aX = ();
										@aY = ();
										@as = ();
										push(@aX, $ay[$k + 0]->[0]);
										push(@aY, $ay[$k + 0]->[0]);
										for ($l = 1; $l < ($next->[$k] - 1); $l++) {
											push(@aY, join("/", @{$ay[$k + $l]}));
											push(@as, $ay[$k + $l]->[1]);
										}
										push(@aX, $ay[$k + $next->[$k] - 1]->[0]);
										push(@aY, $ay[$k + $next->[$k] - 1]->[0]);
										$x = join(" ", @as);
										if (exists $entmod{$x}) {
											addTransformation("", join(" ", @aX, @an, @ao), join(" ", @aY, @an, @ao), "+NPMOD/$x", \@dep, \@X, \@Y, \@type, \$n);
										} else {
											addTransformation("", join(" ", @aX), join(" ", @aY), "+NPMOD/$x", \@dep, \@X, \@Y, \@type, \$n);
										}
										# update the string and next/prev pointers.  This allows us to process the string left to right
										@ay = @ay[0 .. $k, $k + $next->[$k] - 1 .. $#ay];
										($next, $prev) = getNextPrev(\@ay, 1);
									}
								}
							} else {
								# @aX: left side of rule
								# @aY: right side of rule
								# @as: string of thing being removed
								@aX = ();
								@aY = ();
								@as = ();
								push(@aX, $ay[$k + 0]->[0]);
								push(@aY, $ay[$k + 0]->[0]);
								for ($l = 1; $l < ($next->[$k] - 1); $l++) {
									push(@aY, join("/", @{$ay[$k + $l]}));
									push(@as, $ay[$k + $l]->[1]);
								}
								push(@aX, $ay[$k + $next->[$k] - 1]->[0]);
								push(@aY, $ay[$k + $next->[$k] - 1]->[0]);
								$x = join(" ", @as);
								if (exists $entmod{$x}) {
									# add the NPH chunk (including its tokens) to the left and right sides of the rule
									$l = $k + $next->[$k];
									for ($m = 0; $m < ($next->[$l]); $m++) {
										push(@aX, $ay[$l + $m]->[0]);
										push(@aY, $ay[$l + $m]->[0]);
									}
								}
								addTransformation("", join(" ", @aX), join(" ", @aY), "+NPMOD/$x", \@dep, \@X, \@Y, \@type, \$n);
								
								@ay = @ay[0 .. $k, $k + $next->[$k] - 1 .. $#ay];
								($next, $prev) = getNextPrev(\@ay, 1);
							}
						}
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
