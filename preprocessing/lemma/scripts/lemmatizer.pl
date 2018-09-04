#!/usr/bin/perl

# ./lemmatizer.pl <POS file> <output>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;

my $file;
$| = 1;

# build the noun lexicon for the noun lemmatizer
open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	foreach (split(/ /, $ax[1])) {
		if (not $_ =~ /^[\[\]]/) {
			my @ay = split(/\//, $_);
			if ($ay[1] =~ /^N/) {
				nlemmaAdd(lc($ay[0]));
			}
		}
	}
}
close($file);

# apply the noun lemmatizer to nouns
# apply the verb lemmatizer to verbs
# store the lemmatizations
my $out;
my %noun = ();
my %verb = ();
open($file, $ARGV[0]);
open($out, ">$ARGV[1].lemma");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	my @ao = ();
	foreach (split(/ /, $ax[1])) {
        if (not $_ =~ /^[\[\]]/) {
			my @ay = split(/\//, $_);
			if ($ay[1] =~ /^N/) {
				my $x = nlemma(lc($ay[0]));
				if ($x ne lc($ay[0])) {
					$noun{lc($ay[0])} = $x;
				}
				push(@ao, "$x/$ay[1]");
			} elsif ($ay[1] =~ /^V/) {
				my $x = vlemma(lc($ay[0]));
                if ($x ne lc($ay[0])) {
					$verb{lc($ay[0])} = $x;
				}
				push(@ao, "$x/$ay[1]");
			} else {
				push(@ao, lc($ay[0]) . "/$ay[1]");
			}
		}
	}

	print $out $ax[0], "\t", join(" ", @ao), "\n";
}
close($out);
close($file);

open($out, ">$ARGV[1].nlemma");
foreach (sort keys %noun) {
	print $out "$_\t$noun{$_}\n";
}
close($out);

open($out, ">$ARGV[1].vlemma");
foreach (sort keys %verb) {
	print $out "$_\t$verb{$_}\n";
}
close($out);
