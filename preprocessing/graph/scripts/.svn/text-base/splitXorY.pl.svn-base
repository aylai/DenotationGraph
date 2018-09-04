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
	# caption
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);

		for ($i = 0; $i <= $#ay; $i += $next->[$i]) {
			# grab the next two chunks, and check if it's "EN or EN"
			$j = $i + $next->[$i];
			$k = $j + $next->[$j];
			if ($j <= $#ay && $k <= $#ay) {
				if ($ay[$i]->[1] eq "[EN" && $ay[$j]->[1] eq "or" && $ay[$k]->[1] eq "[EN") {
					# @aX - left hand side of rule
					# @aY - right hand side of rule
					@aX = ();
					@aY = ();
					push(@aX, $ay[$i]->[0]);
					push(@aY, $ay[$i]->[0]);
					# get the internal chunks of the first EN chunk
					for ($x = $i + 1; $next->[$x] != 0; $x += $next->[$x]) {
						push(@aX, $ay[$x]->[0]);
						push(@aY, $ay[$x]->[0]);
					}
					push(@aX, $ay[$x]->[0]);
					push(@aX, $ay[$j]->[0]);
					push(@aX, $ay[$k]->[0]);
					# get the internal chunks of the second EN chunk
					for ($x = $k + 1; $next->[$x] != 0; $x += $next->[$x]) {
						push(@aX, $ay[$x]->[0]);
					}
					push(@aX, $ay[$x]->[0]);
					push(@aY, $ay[$x]->[0]);
					addTransformation("", join(" ", @aX), join(" ", @aY), "-orY", \@dep, \@X, \@Y, \@type, \$n);

					# @aX - left hand side of rule
					# @aY - right hand side of rule
					@aX = ();
					@aY = ();
					push(@aX, $ay[$i]->[0]);
					push(@aY, $ay[$i]->[0]);
					# get the internal chunks of the first EN chunk
					for ($x = $i + 1; $next->[$x] != 0; $x += $next->[$x]) {
						push(@aX, $ay[$x]->[0]);
					}
					push(@aX, $ay[$x]->[0]);
					push(@aX, $ay[$j]->[0]);
					push(@aX, $ay[$k]->[0]);
					# get the internal chunks of the second EN chunk
					for ($x = $k + 1; $next->[$x] != 0; $x += $next->[$x]) {
						push(@aX, $ay[$x]->[0]);
						push(@aY, $ay[$x]->[0]);
					}
					push(@aX, $ay[$x]->[0]);
					push(@aY, $ay[$x]->[0]);
					addTransformation("", join(" ", @aX), join(" ", @aY), "-Xor", \@dep, \@X, \@Y, \@type, \$n);
				}
			} else {
				last;
			}
		}
		printSentence($ax[0], $ax[1], \@ay, \@dep, \@X, \@Y, \@type, $n);

		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# add the rule if the index matches $n
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
