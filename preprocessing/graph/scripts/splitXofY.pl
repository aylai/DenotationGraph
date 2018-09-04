#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin";
use simple;

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
	# this is a caption
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);

		# look for an EN chunk with a "[PP of ]" as its second chunk
		for ($i = 0; $i <= $#ay; $i += $next->[$i]) {
			if ($ay[$i]->[1] eq "[EN") {
				$j = $i + 1;
				if ($ay[$j + $next->[$j] + 0]->[1] eq "[PP" &&
					$ay[$j + $next->[$j] + 1]->[1] eq "of" &&
					$ay[$j + $next->[$j] + 2]->[1] eq "]") {
					# $j1 - first NP chunk (X)
					# $j2 - [PP of ] chunk
					# $j3 - second NP chunk (Y)
					# $j4 - end of the EN chunk
					$j1 = $j;
					$j2 = $j1 + $next->[$j1];
					$j3 = $j2 + $next->[$j2];
					$j4 = $j3 + $next->[$j3];

					# check if it's "body of water".  We don't want the
					# "body of water" -> "body rule"
					$x = entityString(\@ay, $next, $i);
					if ($x ne "body/water") {
						# generated the X of Y -> X rule
						# @aX - left side of rule
						# @aY - right side of rule
						@aX = ();
						@aY = ();
						push(@aX, $ay[$i]->[0]);
						push(@aY, $ay[$i]->[0]);
						push(@aX, $ay[$j1]->[0]);
						push(@aY, $ay[$j1]->[0]);
						push(@aX, $ay[$j2]->[0]);
						for ($k = $j3; $k < $j4; $k++) {
							push(@aX, $ay[$k]->[0]);
						}
						push(@aX, $ay[$j4]->[0]);
						push(@aY, $ay[$j4]->[0]);
						addTransformation("", join(" ", @aX), join(" ", @aY), "-ofY", \@dep, \@X, \@Y, \@type, \$n);
					}

					# generated the X of Y -> Y rule
					# @aX - left side of rule
					# @aY - right side of rule
					@aX = ();
					@aY = ();
					push(@aX, $ay[$i]->[0]);
					push(@aY, $ay[$i]->[0]);
					for ($k = $j1; $k < $j2; $k++) {
						push(@aX, $ay[$k]->[0]);
					}
					push(@aX, $ay[$j2]->[0]);
					push(@aX, $ay[$j3]->[0]);
					push(@aY, $ay[$j3]->[0]);
					push(@aX, $ay[$j4]->[0]);
					push(@aY, $ay[$j4]->[0]);
					addTransformation("", join(" ", @aX), join(" ", @aY), "-Xof", \@dep, \@X, \@Y, \@type, \$n);
				}
			}
		}

		printSentence($ax[0], $ax[1], \@ay, \@dep, \@X, \@Y, \@type, $n);

		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# this is a rule - add it if the index is correct ($n)
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
