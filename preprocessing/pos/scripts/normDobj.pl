#!/usr/bin/perl

use strict;
use warnings;

# rechunk NPs that are actually VP NP (where the VP is either play or hold)

my $file;

# list of valid direct objects for "play" (or list of valid verb-direct object pairs)
my %dobj = ();
$dobj{"play"} = {};
$dobj{"play"}->{"baseball"} = 1;
$dobj{"play"}->{"cards"} = 1;
$dobj{"play"}->{"card"} = 1;
$dobj{"play"}->{"chess"} = 1;
$dobj{"play"}->{"cricket"} = 1;
$dobj{"play"}->{"drums"} = 1;
$dobj{"play"}->{"football"} = 1;
$dobj{"play"}->{"frisbee"} = 1;
$dobj{"play"}->{"games"} = 1;
$dobj{"play"}->{"guitar"} = 1;
$dobj{"play"}->{"hockey"} = 1;
$dobj{"play"}->{"instruments"} = 1;
$dobj{"play"}->{"maracas"} = 1;
$dobj{"play"}->{"musical instruments"} = 1;
$dobj{"play"}->{"nintendo"} = 1;
$dobj{"play"}->{"poker"} = 1;
$dobj{"play"}->{"soccer"} = 1;
$dobj{"play"}->{"softball"} = 1;
$dobj{"play"}->{"tennis"} = 1;
$dobj{"play"}->{"violas"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @r = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		# for any NP
		if ($ax[$i] eq "[NP") {
			# grab the first token of the NP, and pretend it is a verb and delemmatize it
			my @ay = split(/\//, $ax[$i + 1]);
			my $v = lc($ay[0]);
			my $tag = "VBZ";

			if ($v =~ /^(.*)ing/) {
				$v = $1;
				$tag = "VBG";
			} elsif ($v =~ /^(.*)es/) {
				$v = $1;
				$tag = "VBP";
			} elsif ($v =~ /^(.*)s/) {
				$v = $1;
				$tag = "VBP";
			}

			# grab the rest of the NP - this will be the candidate direct object
			my $j = $i + 2;
			my @az = ();
			while ($j <= $#ax && $ax[$j] ne "]") {
				my @aw = split(/\//, $ax[$j]);
				push(@az, lc($aw[0]));
				$j++;
			}
			my $do = join(" ", @az);

			# non-empty candidate direct object - see if the first token was a recognizable verb
			if ($do ne "") {
				# hold is always rechunked, no matter what the direct object is
				if ($v eq "hold") {
					$ay[1] = $tag;
					push(@r, "[VP");
					push(@r, join("/", @ay));
					push(@r, "]");
					push(@r, "[NP");
					$i = $i + 1;
					next;
				# otherwise see if the verb-direct object pair is one we want to split up
				} elsif (exists $dobj{$v} && exists $dobj{$v}->{$do}) {
					$ay[1] = $tag;
					push(@r, "[VP");
					push(@r, join("/", @ay));
					push(@r, "]");
					push(@r, "[NP");
					$i = $i + 1;
					next;
				}
			}
		}
		push(@r, $ax[$i]);
	}
	print join(" ", @r), "\n";
}
close($file);
