#!/usr/bin/perl

use strict;
use warnings;

# if the first token in an NP chunk is a particle (and this is preceded by a VP chunk)
# rechunk it as a PRT or SBAR

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $file;

# load particles
my %prt = ();
open($file, "$sdir/../data/prt.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $w = shift(@ax);
	$prt{$w} = {};
	foreach (@ax) {
		$prt{$w}->{$_} = 1;
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	# @ax - caption
	# @ay - output caption
	# $last - last chunk seen
	# $last2 - second to last chunk seen
	my @ax = split(/ /, $_);
	my @ay = ();
	my $last = "";
	my $last2 = "";
	for (my $i = 0; $i <= $#ax; $i++) {
		my @az = split(/\//, $ax[$i]);

		# see if this token is a possible particle
		# and check if it is the first token in an NP that is preceded by another chunk
		if (exists $prt{lc($az[0])} && $#ay >= 1) {
			if ($ay[$#ay - 0] eq "[NP" && $ay[$#ay - 1] eq "]") {
				if ($i < $#ax) {
					# make sure this isn't part of a compound term we want to keep as an NP
					my @aw = split(/\//, $ax[$i + 1]);
					if (exists $prt{lc($az[0])}->{lc($aw[0])}) {
						push(@ay, $ax[$i]);
						next;
					}

					# see if this is a "particle CC particle" case - if so, just change the NP chunk to a PRT chunk
					if ($#aw == 1 && $aw[1] eq "CC" && $i <= ($#ax - 2)) {
						@aw = split(/\//, $ax[$i + 2]);
						if (exists $prt{lc($aw[0])}) {
							pop(@ay);
							push(@ay, "[PRT");
							push(@ay, $ax[$i]);
							next;
						}
					}
				}

				# make sure the thing before the NP chunk is a VP chunk
				if ($last2 eq "[VP") {
					pop(@ay);
					# rechunk the first token as either a PRT or an SBAR
					if (lc($az[0]) eq "as") {
						push(@ay, "[SBAR");
					} else {
						push(@ay, "[PRT");
					}
					push(@ay, $ax[$i]);
					if ($i < $#ax && $ax[$i + 1] ne "]") {
						push(@ay, "]");
						push(@ay, "[NP");
					}
					next;
				}
			}
		}

		if ($ax[$i] =~ /^\[/) {
			$last2 = $last;
			$last = $ax[$i];
		}

		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
