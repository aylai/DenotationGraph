#!/usr/bin/perl

# fixPunct.pl <token file>

use strict;
use warnings;

my $file;

# store all tokens in the corpus
my %dict = ();
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		foreach (split(/ /, $ax[1])) {
			$dict{$_}++;
		}
	}
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my @az = ();
		for (my $i = 0; $i <= $#ay; $i++) {
			# see if there's an alphanumeric character in this token
			if ($ay[$i] =~ /[A-Za-z0-9]/) {
				# if we're at the end, and the last token ends with a period
				# separate them if we've seen the token without the period
				if ($i == $#ay && $ay[$i] =~ /^(.+)\./) {
					if (exists $dict{$1}) {
						push(@az, $1);
						push(@az, ".");
						next;
					}
				} else {
					# split off leading and trailing " and ,
					my $x = $ay[$i];
					$ay[$i] = "";
					if ($x =~ /^([\"\,])(.+)$/) {
						push(@az, $1);
						$x = $2;
					}
					if ($x =~ /^(.+)([\"\,])$/) {
						push(@az, $1);
						push(@az, $2);
						next;
					} else {
						push(@az, $x);
						next;
					}
				}
			}
			push(@az, $ay[$i]);
		}
		$ax[1] = join(" ", @az);
	}
	print join("\t", @ax), "\n";
}
close($file);
