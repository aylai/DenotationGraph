#!/usr/bin/perl

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use parse;

# check if the start is "[EN [NP [NPH there/this/here ] ] ] [VP be ]",
# if so, drop it.
my $file;
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	if ($#ax == 2) {
		my @ay = split(/ /, $ax[2]);
		my ($next, $prev) = breakSlash(\@ay, 1);
		if ($#ay >= 9 &&
			$ay[0]->[1] eq "[EN" &&
			$ay[1]->[1] eq "[NP" &&
			$ay[2]->[1] eq "[NPH" &&
			$ay[4]->[1] eq "]" &&
			$ay[5]->[1] eq "]" &&
			$ay[6]->[1] eq "]" &&
			$ay[7]->[1] eq "[VP" &&
			$ay[8]->[1] eq "be" &&
			$ay[9]->[1] eq "]") {
			if ($ay[3]->[1] eq "there" || $ay[3]->[1] eq "this" || $ay[3]->[1] eq "here") {
				@ay = @ay[10 .. $#ay];
			}
		}

		for (my $i = 0; $i <= $#ay; $i++) {
			$ay[$i] = join("/", @{$ay[$i]});
		}

		print "$ax[0]\t$ax[1]\t", join(" ", @ay), "\n";
	}
}

close($file);
