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
		# check for EN chunks, find the NP chunks in them, find the NPD chunks in them
		for ($i = 0; $i <= $#ay; $i += $next->[$i]) {
			if ($ay[$i]->[1] eq "[EN") {
				for ($j = $i + 1; $next->[$j] != 0; $j += $next->[$j]) {
					if ($ay[$j]->[1] eq "[NP") {
						for ($k = $j + 1; $next->[$k] != 0; $k += $next->[$k]) {
							if ($ay[$k]->[1] eq "[NPD") {
								# @aX - left side of rule
								# @ay - right side of rule
								# @as - string that we're dropping
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
								my $s = join(" " , @as);
								# do not drop "no" or "each"
								if ($s ne "no" && $s ne "each") {
									addTransformation("", join(" ", @aX), join(" ", @aY), "+NPART/$s", \@dep, \@X, \@Y, \@type, \$n);
									
									@ay = @ay[0 .. $k, $k + $next->[$k] - 1 .. $#ay];
									($next, $prev) = getNextPrev(\@ay, 1);
								}
								last;
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
	# read the rule in, if it has the correct index ($n)
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
