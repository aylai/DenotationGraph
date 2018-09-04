#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use parse;

my $file;

# list of other terms likely to indicate that "girl" refers to a woman
my %woman = ();
$woman{"bride"} = 1;
$woman{"lady"} = 1;
$woman{"woman"} = 1;
$woman{"young lady"} = 1;
$woman{"young woman"} = 1;

# go through the captions of each image, and look for one of the
# indicator terms.  Flag images (%cwoman) where "girl" (if it exists)
# should be changed to "girl_woman"
my %cwoman = ();
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay;

	@ay = split(/\#/, $ax[0]);
	my $id = $ay[0];

	if ($#ax >= 1) {
		@ay = split(/ /, $ax[2]);
		breakSlash(\@ay, 1);
		for (my $i = 0; $i <= $#ay; $i++) {
			if ($ay[$i]->[1] eq "[NPH") {
				my @aw = ();
				for (my $j = $i + 1; $j <= $#ay; $j++) {
					if ($ay[$j]->[1] eq "]") {
						last;
					} else {
						push(@aw, $ay[$j]->[1]);
					}
				}

				my $w = join(" ", @aw);
				if (exists $woman{$w}) {
					$cwoman{$id} = 1;
				}
			}
		}
	}
}
close($file);

# actual perform the changes
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay;

	@ay = split(/\#/, $ax[0]);
	my $id = $ay[0];

	if ($#ax >= 2) {
		@ay = split(/ /, $ax[2]);
		breakSlash(\@ay, 1);
		for (my $i = 0; $i <= ($#ay - 3); $i++) {
			if ($ay[$i + 0]->[1] eq "[NPH" && $ay[$i + 2]->[1] eq "]") {
				if ($ay[$i + 1]->[1] eq "girl") {
					if (exists $cwoman{$id}) {
						$ay[$i + 1]->[1] = "girl_woman";
					} else {
						$ay[$i + 1]->[1] = "girl_child";
					}
				}
			}
		}
		for (my $i = 0; $i <= $#ay; $i++) {
			$ay[$i] = join("/", @{$ay[$i]});
		}
		$ax[2] = join(" ", @ay);
	}

	print join("\t", @ax), "\n";
}
close($file);
