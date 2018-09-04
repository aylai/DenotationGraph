#!/usr/bin/perl

use strict;
use warnings;

# fix cases where an NP consists of only a determiner
# converts [NP a ] [VP washed ] [PRT out ] [NP bridge ] to [NP a washed out bridge ]

my $file;

# list of tokens which can be either verbs or nouns
my %nouns = ();
$nouns{"can"} = 1;
$nouns{"can"} = 1;
$nouns{"face"} = 1;
$nouns{"left"} = 1;
$nouns{"pose"} = 1;
$nouns{"saw"} = 1;
$nouns{"sifter"} = 1;
$nouns{"set"} = 1;
$nouns{"shed"} = 1;
$nouns{"shot"} = 1;
$nouns{"tattoo"} = 1;
$nouns{"try"} = 1;
$nouns{"win"} = 1;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
	for (my $i = 0; $i <= $#ax; $i++) {
		if ($i <= ($#ax - 3)) {
			# are we at [NP DT ]?
			if (lc($ax[$i + 1]) eq "an/dt" || lc($ax[$i + 1]) eq "a/dt" || lc($ax[$i + 1]) eq "the/dt") {
				if ($ax[$i + 0] eq "[NP" && $ax[$i + 2] eq "]") {
					# if the next chunk is a PP or PRT which is followed by an NP chunk
					# combine all three chunks into a single NP chunk
					if ($ax[$i + 3] eq "[PP" || $ax[$i + 3] eq "[PRT") {
						my $j;

						for ($j = $i + 3; $j <= $#ax; $j++) {
							if ($ax[$j] eq "]") {
								last;
							}
						}

						if ($j < $#ax && $ax[$j + 1] eq "[NP") {
							push(@ay, $ax[$i + 0]);
							push(@ay, $ax[$i + 1]);
							for (my $k = $i + 4; $k < $j; $k++) {
								push(@ay, $ax[$k]);
							}
							$i = $j + 1;
							next;
						}
					# if the next chunk is a VP chunk
					} elsif ($ax[$i + 3] eq "[VP") {
						my $j;
						my @aw = ();

						# grab the verb
						for ($j = $i + 4; $j <= $#ax; $j++) {
							if ($ax[$j] eq "]") {
								last;
							}
							my @az = split(/\//, $ax[$j]);
							push(@aw, lc($az[0]));
						}

						# if the verb is potentially a noun, join the NP and VP chunks into an NP chunk
						if (exists $nouns{join(" ", @aw)}) {
							push(@ay, $ax[$i + 0]);
							push(@ay, $ax[$i + 1]);
							for (my $k = $i + 4; $k < $j; $k++) {
								my @az = split(/\//, $ax[$k]);
								push(@ay, "$az[0]/NN");
							}
							push(@ay, $ax[$j]);
							$i = $j;
							next;
						}

						# otherwise, if the sequence is [NP DT ] [VP] [PP/PRT] [NP ..., combine everything into a single NP chunk
						if ($j < $#ax && ($ax[$j + 1] eq "[PP" || $ax[$j + 1] eq "[PRT")) {
							my $k;
							for ($k = $j + 2; $k <= $#ax; $k++) {
								if ($ax[$k] eq "]") {
									last;
								}
							}
							
							if ($k < $#ax && $ax[$k + 1] eq "[NP") {
								push(@ay, $ax[$i + 0]);
								push(@ay, $ax[$i + 1]);
								for (my $l = $i + 4; $l < $j; $l++) {
									push(@ay, $ax[$l]);
								}
								for (my $l = $j + 2; $l < $k; $l++) {
									push(@ay, $ax[$l]);
								}
								$i = $k + 1;
								next;
							}
						}
					}
				}
			}
		}

		push(@ay, $ax[$i]);
	}

	print join(" ", @ay), "\n";
}
close($file);
