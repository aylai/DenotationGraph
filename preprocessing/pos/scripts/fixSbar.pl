#!/usr/bin/perl

use strict;
use warnings;

my $file;

# list of verbs and their associated PRT chunks
my %prt = ();
$prt{"hold"} = ();
$prt{"hold"}->{"up"} = 0;
$prt{"toss"} = ();
$prt{"toss"}->{"up"} = 0;

# list of valid SBAR chunks
my %sbar = ();
$sbar{"after"} = 0;
$sbar{"although"} = 1;
$sbar{"as"} = 1;
$sbar{"because"} = 1;
$sbar{"before"} = 0;
$sbar{"even though"} = 1;
$sbar{"for"} = 0;
$sbar{"if"} = 1;
$sbar{"in order to"} = 1;
$sbar{"just as"} = 0;
$sbar{"like"} = 0;
$sbar{"since"} = 1;
$sbar{"so"} = 0;
$sbar{"than"} = 0;
$sbar{"that"} = 0;
$sbar{"though"} = 1;
$sbar{"unless"} = 1;
$sbar{"where"} = 0;
$sbar{"whether"} = 1;
$sbar{"while"} = 1;
$sbar{"whilst"} = 1;
$sbar{"who"} = 0;
$sbar{"with"} = 0;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my $vp = "";
	for (my $i = 0; $i <= $#ax; $i++) {
		# manipulate SBAR chunks
		if ($ax[$i] eq "[SBAR") {
			my @ay = ();
			for (my $j = $i + 1; $j <= $#ax; $j++) {
				if ($ax[$j] =~ /^\]/) {
					last;
				}
				my @az = split(/\//, $ax[$j]);
				push(@ay, lc($az[0]));
			}

			my $w = join(" ", @ay);

			# if this is a recognized SBAR, leave it alone
			if (exists $sbar{$w} || exists $sbar{$ay[0]}) {
			} else {
				# otherwise retag it as a PP chunk or a PRT chunk
				# if its a verb + particle combination we recognize
				$ax[$i] = "[PP";
				if ($vp ne "") {
					foreach (keys %prt) {
						if ($vp =~ /^$_/ && exists $prt{$_}->{$w}) {
							$ax[$i] = "[PRT";
							last;
						}
					}
				}
			}
#		} elsif ($ax[$i] eq "[PP") {
#			my @ay = ();
#			for (my $j = $i + 1; $j <= $#ax; $j++) {
#				if ($ax[$j] =~ /^\]/) {
#					last;
#				}
#				my @az = split(/\//, $ax[$j]);
#				push(@ay, lc($az[0]));
#			}
#
#			my $w = join(" ", @ay);
#
#			if (exists $sbar{$w} && $sbar{$w} == 1) {
#				$ax[$i] = "[SBAR";
#			}
		}

		# store the previous VP, if the previous chunk is a VP.
		if ($ax[$i] eq "[VP") {
			my @ay = ();
			for (my $j = $i + 1; $j <= $#ax; $j++) {
				if ($ax[$j] =~ /^\]/) {
					last;
				}
				my @az = split(/\//, $ax[$j]);
				push(@ay, lc($az[0]));
			}

			$vp = join(" ", @ay);
		} elsif ($ax[$i] =~ /^\[/) {
			$vp = "";
		}
	}

	print join(" ", @ax), "\n";
}
close($file);
