#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin";
use simple;

$| = 1;

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
	# this is a caption.  Find adverbs in VP chunks and ADVP chunks,
	# and remove them.
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);
		$inext = 0;
		# we go through the caption backwards.  Since we'll be
		# modifying the caption as we go, this makes keeping track of
		# the next token easier (otherwise, when we remove something,
		# the index of the next token is likely to change).
		for ($i = $#ay; $i >= 0; $i = $inext) {
			$inext = $i - $prev->[$i];
			$i1 = $inext + 1;
			# found an ADVP chunk - drop it
			if ($ay[$i1]->[1] eq "[ADVP") {
				# @ax is the left hand side of the rewrite rule
				# @ay is the right hand side
				@aX = ();
				@aY = ();
				@as = ();
				push(@aX, ($i1 > 0) ? $ay[$i1 - 1]->[0] : "B");
				push(@aY, ($i1 > 0) ? $ay[$i1 - 1]->[0] : "B");
				foreach (@ay[$i1 .. $i1 + $next->[$i1] - 1]) {
				    push(@aY, join("/", @{$_}));
					if (not $_->[1] =~ /^[\[\]]/) {
						push(@as, $_->[1]);
					}
				}

				# unless the ADVP chunk is "[ADVP other ]"
				if (lc(join(" ", @as)) eq "other") {
					next;
				}

				push(@aX, (($i1 + $next->[$i1]) <= $#ay) ? $ay[$i1 + $next->[$i1]]->[0] : "E");
				push(@aY, (($i1 + $next->[$i1]) <= $#ay) ? $ay[$i1 + $next->[$i1]]->[0] : "E");

				# if we're at the end of the caption, find the ADVP
				# chunk, and check if the previous thing was a CC - if
				# so, drop that as well.
				$x = 0;
				if ($aX[0] ne "B" && $aX[1] eq "E") {
					for ($k = 0; $k <= $#ay; $k++) {
						if ($ay[$k]->[0] eq $aX[0]) {
							if (exists $ay[$k]->[2] && $ay[$k]->[2] eq "CC") {
								shift(@aX);
								unshift(@aX, ($k > 0) ? $ay[$k - 1]->[0] : "B");

								shift(@aY);
								unshift(@aY, join("/", @{$ay[$k]}));
								unshift(@aY, ($k > 0) ? $ay[$k - 1]->[0] : "B");
								$inext--;
								$x++;
							}
							last;
						}
					}
				}

				addTransformation("", join(" ", @aX), join(" ", @aY), "+ADVP/" . join(" ", @as), \@dep, \@X, \@Y, \@type, \$n);

				@ay = @ay[0 .. $i1 - 1 - $x, $i1 + $next->[$i1] .. $#ay];
				($next, $prev) = getNextPrev(\@ay, 1);
			# if we're in a VP chunk, go remove adverbs
			} elsif ($ay[$i1]->[1] eq "[VP") {
				$jnext = 0;
				for ($j = $i1 + 1; $j < ($i1 + $next->[$i1] - 1); $j = $jnext) {
					$jnext = $j + 1;
					# found an adverb, make a rewrite rule, and remove the adverb
					if ($ay[$j]->[2] eq "RB") {
						# again, @ax is the left hand side of the rewrite rule
						# and @ay is the right hand side of the rewrite rule
						@as = ();
						@aX = ();
						@aY = ();
						push(@aX, $ay[$j - 1]->[0]);
						push(@aY, $ay[$j - 1]->[0]);
						for ($k = $j; $k < ($i1 + $next->[$i1] -1); $k++) {
							if ($ay[$k]->[2] ne "RB") {
								last;
							}
							push(@as, $ay[$k]->[1]);
							push(@aY, join("/", @{$ay[$k]}));
						}
						push(@aX, $ay[$k]->[0]);
						push(@aY, $ay[$k]->[0]);

						if (join(" ", @as) ne "not") {
							addTransformation("", join(" ", @aX), join(" ", @aY), "+RB/" . join(" ", @as), \@dep, \@X, \@Y, \@type, \$n);

							@ay = @ay[0 .. $j - 1, $k .. $#ay];
							($next, $prev) = getNextPrev(\@ay, 1);
							$jnext = $j;
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
	# this is a rule line, store the rule info (assuming the rule ID
	# is correct), and move on
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
