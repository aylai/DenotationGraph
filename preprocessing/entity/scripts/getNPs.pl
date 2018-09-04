#!/usr/bin/perl

# usage: getNPs.pl <POS file>

# NP file format:
# sentenceID-NP# start end pre-IN coref post-IN (synset det mods head)*

use strict;
use warnings;
use FindBin;
use lib "$FindBin::Bin/../../misc";
use parse;
use util;
use WordNet::QueryData;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

my $wn = WordNet::QueryData->new;

my %symbols = ();
$symbols{"flag"} = 1;
$symbols{"flags"} = 1;
$symbols{"light"} = 1;
$symbols{"lights"} = 1;

my $file;
my %ngram = ();
open($file, "$sdir/../data/synset-lexicon.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($ax[0] =~ / /) {
		$ngram{$ax[0]} = 1;
	}
}
close($file);

open($file, "$sdir/../data/notcnouns.txt");
while (<$file>) {
	chomp($_);
	$ngram{$_} = -1;
}
close($file);

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax >= 1) {
		my @ay = split(/ /, $ax[1]);
		my ($next, $prev) = breakSlash(\@ay, 0);

		my @index = ();
		my $n = 0;
		for (my $i = 0; $i <= $#ay; $i++) {
			$index[$i] = $n;
			if (not $ay[$i]->[0] =~ /^[\[\]]/) {
				$n++;
			}
		}

		my $np = 0;
		my $inext;
		for (my $i = 0; $i <= $#ay; $i = $inext) {
			$inext = $i + 1;
			if ($ay[$i]->[0] eq "[EN" || $ay[$i]->[0] eq "[NP") {
				$inext = $i + $next->[$i];

				print "$ax[0]#NP$np\t$index[$i]\t", $index[$i + $next->[$i] - 1] - 1;
				$np++;

				# check for preceding PP
				print "\t";
				if ($i > 0 && $ay[$i - $prev->[$i - 1]]->[0] eq "[PP") {
					my @az = ();
					my $j = $i - $prev->[$i - 1];
					foreach (@ay[$j + 1 .. $i - 2]) {
						push(@az, join("/", @{$_}));
					}
					print join(" ", @az);
				}

				# print coref ID, if one exists
				print "\t";
				if ($ay[$i]->[0] eq "[EN" && $#{$ay[$i]} > 0) {
					print $ay[$i]->[-1];
				}

				# check for succeeding PP
				print "\t";
				if ($inext <= $#ay && $ay[$inext]->[0] eq "[PP") {
					my @az = ();
					foreach (@ay[$inext + 1 .. $inext + $next->[$inext] - 2]) {
						push(@az, join("/", @{$_}));
					}
					print join(" ", @az);
				}

				my $begin;
				my $end;
				if ($ay[$i]->[0] eq "[EN") {
					$begin = $i;
					$end = $i + $next->[$i];
				} else {
					$begin = $i - 1;
					$end = $i + $next->[$i] + 1;
				}

				for (my $j = $begin + 1; $j < ($end - 1); $j += $next->[$j]) {
					if ($ay[$j]->[0] ne "[NP") {
						print STDERR "$ax[0]: unexpected non-NP chunk\n";
						next;
					}

					# print synset, if any
					print "\t";
					if ($#{$ay[$j]} > 0) {
						print $ay[$j]->[1];
					}

					# by default, there is no determiner
					my $deterB = $j + 1;
					my $deterE = $j;
					# by default, the head word is just the last word
					my $headE = $j + $next->[$j] - 2;
					my $headB = $headE;
					my $last = lc($ay[$headE]->[0]);

					# head can be "X else" ("someone else", "everyone else", etc.)
					if ($last eq "else") {
						if ($deterB < $headE) {
							$headB = $headE - 1;
						}
					# symbols ("flag", "light") should not be compounded.
					# WordNet will have <color> <symbol> entries (i.e,. "black flag",
					# "red light", etc.), so skip checking WordNet.
					} elsif (not exists $symbols{$last}) {
						# find the longest n-gram containing the last word in the
						# NP that is in the noun part of WordNet
						for ($headB = $deterB; $headB < $headE; $headB++) {
							my @ac = ();
							foreach (@ay[$headB .. $headE]) {
								push(@ac, lc($_->[0]));
							}
							my $c = join(" ", @ac);

							if (not exists $ngram{$c}) {
								if ($c =~ /\#/) {
									$ngram{$c} = -1;
								} else {
									# find the form with the most number of senses
									my @forms = $wn->validForms($c . "#n");
									my $max = -1;
									foreach (@forms) {
										my $sg = $_;
										$sg =~ s/_/ /;
										$sg =~ s/\#n$//;
										if (exists $ngram{$sg}) {
											$max = $ngram{$sg};
											last;
										}
										my @senses = $wn->querySense($_);
										if ($#senses > $max) {
											$max = $#senses;
										}
									}
									$ngram{$c} = $max;
								}
							}

							# if we've found a valid WordNet entry, finish
							if ($ngram{$c} >= 0) {
								last;
							}
						}
					}

					# determiner can be a PRP#, CD, "at least", "at least" CD,
					# "a", "the" "an", "a" CD, "a few", "a couple",
					# "the same", "the other", "a dozen", LS, DT, POS,
					# "several", "many", "other", "multiple", "various",
					# "different", "more", "same"
					if ($headB > ($deterE + 1)) {
						if ($ay[$deterE + 1]->[1] eq "PRP\$" || $ay[$deterE + 1]->[1] eq "CD") {
							my $pos = $ay[$deterE + 1]->[1];
							$deterE += 1;
							if ($headB > ($deterE + 2)) {
								if ($ay[$deterE + 1]->[1] eq "CC" && $ay[$deterE + 2]->[1] eq $pos) {
									$deterE += 2;
								}
							}
						} else {
							if ($headB > ($deterE + 2)) {
								if (lc($ay[$deterE + 1]->[0]) eq "at" && lc($ay[$deterE + 2]->[0]) eq "least") {
									$deterE += 2;
								}
							}
							
							if ($headB > ($deterE + 1)) {
								my $z = lc($ay[$deterE + 1]->[0]);
								if ($z eq "a" || $z eq "the" || $z eq "an") {
									$deterE += 1;
									if ($headB > ($deterE + 1)) {
										$z = lc($ay[$deterE + 1]->[0]);
										if ($ay[$deterE + 1]->[1] eq "CD" || $z eq "few" || $z eq "couple" || $z eq "same" || $z eq "other" || $z eq "dozen") {
											$deterE += 1;
										}
									}
								} elsif ($ay[$deterE + 1]->[1] eq "CD") {
									$deterE += 1;
									if ($headB > ($deterE + 2)) {
										if ($ay[$deterE + 1]->[1] eq "CC" && $ay[$deterE + 2]->[1] eq "CD") {
											$deterE += 2;
										}
									}
								} elsif ($ay[$deterE + 1]->[1] eq "LS" || $ay[$deterE + 1]->[1] eq "DT" || $ay[$deterE + 1]->[1] eq "POS" ||
										 $z eq "several" || $z eq "many" || $z eq "other" || $z eq "multiple" || $z eq "various" || $z eq "different" || $z eq "more" || $z eq "same") {
									$deterE += 1;
								}
							}
						}
					}

					print "\t";
					if ($deterE >= $deterB) {
						my @az = ();
						foreach (@ay[$deterB .. $deterE]) {
							push(@az, join("/", @{$_}));
						}
						print join(" ", @az);
					}

					# if there's anything not in the determiner or the
					# head, it's part of the modifier
					print "\t";
					if (($deterE + 1) < $headB) {
						my @az = ();
						foreach (@ay[$deterE + 1 .. $headB - 1]) {
							push(@az, join("/", @{$_}));
						}
						print join(" ", @az);
					}

					my @az = ();
					foreach (@ay[$headB .. $headE]) {
						push(@az, join("/", @{$_}));
					}
					print "\t", join(" ", @az);

					# if the next chunk is not an NP (if there is a next chunk
					# it should be the PP chunk "of"), add it
					if (($j + $next->[$j]) < ($end - 1)) {
						print "\t";
						if ($ay[$j + $next->[$j]]->[0] ne "[NP") {
							$j += $next->[$j];
							if ($next->[$j] == 1) {
								print join("/", @{$ay[$j]});
							} else {
								my @az = ();
								foreach (@ay[$j + 1 .. $j + $next->[$j] - 2]) {
									push(@az, join("/", @{$_}));
								}
								print join(" ", @az);
							}
						}
					}
				}
				print "\n";
			}
		}
	}
}
close($file);
