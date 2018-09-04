#!/usr/bin/perl

use strict;
use warnings;

my $file;
my %fix = ();
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my @ay = split(/ /, $ax[8]);

	my $new = "";
	my $i = $#ay;
	if ($i > 0 && $ay[$i] eq ",/," && $ay[$i - 1] =~ /\/NN[^P]*$/) {
		my @n = (@ay[$i + 1 .. $#ay], split(/ /, $ax[9]));
		$new = " ] $ay[$i] [NP " . join(" ", @n) . " ]";
	} else {
		for ($i = $#ay; $i > 0; $i--) {
			if (lc($ay[$i]) eq "and/cc") {
				my @az = split(/\//, $ay[$i - 1]);
				
				if ($az[1] =~ /^NN/) {
					my @n1 = (split(/ /, $ax[7]), @ay[0 .. $i - 1]);
					my @n = (@ay[$i + 1 .. $#ay], split(/ /, $ax[9]));
					$new = " ] $ay[$i] [NP " . join(" ", @n) . " ]";
				}
				last;
			}
		}
	}

	if ($new ne "") {
		$i--;

		while ($i > 0 && $ay[$i - 1] eq ",/," && $ay[$i] =~ /\/NN[^P]*$/) {
			$new = " ] $ay[$i - 1] [NP $ay[$i]" . $new;
			$i -= 2;
		}

		my @n = (split(/ /, $ax[7]), @ay[0 .. $i]);

		$fix{$ax[0]} = "[NP " . join(" ", @n) . $new;
		print STDERR "$fix{$ax[0]}\n";
	}
}
close($file);

open($file, $ARGV[1]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	my @az = ();
	my $suppress = 0;
	my $np = 0;
	foreach (split(/ /, $ax[1])) {
		if ($_ eq "[NP") {
			if (exists $fix{"$ax[0]#NP$np"}) {
				push(@az, $fix{"$ax[0]#NP$np"});
				$suppress = 1;
			}
			$np++;
		}

		if ($suppress == 0) {
			push(@az, $_);
		}

		if ($_ eq "]") {
			$suppress = 0;
		}
	}

	print "$ax[0]\t", join(" ", @az), "\n";
}
close($file);
