#!/usr/bin/perl

# ./breakVPs.pl <chunked file>

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use parse;
use util;

# break up VP chunks that contain multiple events
# [VP sits eating ] -> [VP sits ] [VP eating ]

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load list of verbs that can be split up (e.g., verbs like "sit" in the above example)
my %split = ();
open($file, "$sdir/../data/split.txt");
while (<$file>) {
	chomp($_);
	$split{$_} = 1;
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	my @ax = split(/ /, $_);

	# run the parse routine - mostly handles breaking things up into chunks.
	# would probably be easier to just get the chunks in the caption, but...
	my $s = ();
	my $i = 0;
	my $n = 0;
	my $p = 0;
	my @b = ();
	my @e = ();
	my $x;
	while ($i <= $#ax) {
		($x, $i, $p) = parse(\@ax, $i, $p);
		$s->[$n] = $x;
		$n++;
	}

	my @sent = ();
	# iterate through the chunks
	for (my $i = 0; $i < $n; $i++) {
		# is this chunk a VP?
		if ($s->[$i]->[0] eq "VP") {
			# @block - complete output
			# @current - reverse of current VP chunk being built
			# $state - have we seen a TO
			# @ay - reverse of the text in the VP chunk
			my @block = ();
			my @current = ();
			my $state = 0;
			my @ay = reverse(split(/ /, $s->[$i]->[1]));

			# proceed backwards through the VP chunk - we're going to look for points where we can break up the VP
			for (my $j = 0; $j <= $#ay; $j++) {
				my @az = split(/\//, $ay[$j]);
				# if the current VP chunk we're building is empty, just push the token onto it
				if ($#current == -1) {
					push(@current, $ay[$j]);
				# if we run into a TO, note that we have, and note that
				# we don't want to break things up any further, unless we encounter a conjunction
				} elsif ($az[1] eq "TO") {
					$state = 1;
					push(@current, $ay[$j]);
				# if we run into a conjunction
				} elsif ($az[1] eq "CC") {
					# if this is adverb CC adverb, it stays in the VP
					if ($current[$#current] =~ /\/RB$/ && $j < $#ay && $ay[$j + 1] =~ /\/RB$/) {
						push(@current, $ay[$j]);
					# otherwise, assume we're joining together two different verbs, and break things up
					} else {
						push(@block, "[VP " . join(" ", reverse(@current)) . " ]");
						push(@block, $ay[$j]);
						@current = ();
						$state = 0;
					}
				# if we encounter a verb that's not preceding a TO...
				} elsif ($az[1] =~ /^V/ && $state == 0) {
					# count the number of verbs (well, non-adverbs) in the current VP
					my $nrb = 0;
					foreach my $q (@current) {
						my @aq = split(/\//, $q);
						if ($aq[1] ne "RB") {
							$nrb++;
						}
					}

					# if there are already non-adverbs in the current VP
					# and this verb is one that we can split (%split)
					# end the current VP and start a new one
					my $w = lc(vlemma($az[0]));
					if (exists $split{$w} && $nrb > 0) {
						push(@block, "[VP " . join(" ", reverse(@current)) . " ]");
						@current = ();
						push(@current, $ay[$j]);
					} else {
						push(@current, $ay[$j]);
					}
				} else {
					push(@current, $ay[$j]);
				}
			}
			if ($#current > -1) {
				push(@block, "[VP " . join(" ", reverse(@current)) . " ]");
			}
			push(@sent, join(" ", reverse(@block)));
		} else {
			push(@sent, unparse($s->[$i]));
		}
	}

	print join(" ", @sent), "\n";
}
close($file);
