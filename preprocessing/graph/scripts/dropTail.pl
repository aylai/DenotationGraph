#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin";
use simple;

# words at the end of captions that we want to drop
%drop = ();
$drop{"and"} = 1;
$drop{"while"} = 1;

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
	# caption - check if the last token is something we want to drop
	# if so, add a rule to drop it
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);
		if ($#ay >= 1 && exists $drop{$ay[$#ay]->[1]}) {
			@aX = ();
			@aY = ();
			push(@aX, $ay[$#ay - 1]->[0]);
			push(@aY, $ay[$#ay - 1]->[0]);
			push(@aY, join("/", @{$ay[$#ay]}));
			push(@aX, "E");
			push(@aY, "E");
			addTransformation("", join(" ", @aX), join(" ", @aY), "+DROP/" . $ay[$#ay]->[1], \@dep, \@X, \@Y, \@type, \$n);
			delete $ay[$#ay];
		}
		printSentence($ax[0], $ax[1], \@ay, \@dep, \@X, \@Y, \@type, $n);

		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# read rule, assuming it has the correct index ($n)
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
