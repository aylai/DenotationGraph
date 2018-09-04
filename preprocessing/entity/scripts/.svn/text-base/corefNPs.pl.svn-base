#!/usr/bin/perl

# usage: ./corefNPs.pl <NP file> <lexicon file>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use WordNet::QueryData;

$index = 0;

sub corefimg() {
	my (%coref, %synsets, %urls, %words, @best, @group, @lex, @syns, @syns0, @syns1, @syns2, $start);
	my (%hx, @aj, @ak, $g, $i, $j, $k, $s, $x);

	# for each reference ID, we want to know
	# 1) which sentences do you cover (and how many mentions in each sentence)
	# 2) which words do you cover
	# 3) what is a consistent set of synsets that covers you
	%synsets = ();
	%urls = ();
	%words = ();
	for ($i = 0; $i <= $#url; $i++) {
		@lex = ();
		@aj = split(/\//, $word[$i]);
		for ($j = 0; $j <= $#aj; $j++) {
			if (exists $lexicon{$aj[$j]}) {
				$lex[$j] = ();
				@ak = keys %{$lexicon{$aj[$j]}};
				for ($k = 0; $k <= $#ak; $k++) {
					$lex[$j]->[$k] = $ak[$k];
				}
			} else {
				last;
			}
		}

		if ($j > $#aj && $#lex >= 0) {
			if (not exists $synsets{$ref[$i]}) {
				$synsets{$ref[$i]} = ();
				$sentences{$ref[$i]} = {};
				$words{$ref[$i]} = {};
			}

			$start = $#lex * ($#lex + 1) / 2;
			for ($j = 0; $j <= $#lex; $j++) {
				if (not exists $synsets{$ref[$i]}->[$start + $j]) {
					$synsets{$ref[$i]}->[$start + $j] = {};
					foreach (@{$lex[$j]}) {
						$synsets{$ref[$i]}->[$start + $j]->{$_} = 1;
					}
				} else {
					$x = $synsets{$ref[$i]}->[$start + $j];
					$synsets{$ref[$i]}->[$start + $j] = {};
					foreach (@{$lex[$j]}) {
						if (exists $x->{$_}) {
							$synsets{$ref[$i]}->[$start + $j]->{$_} = 1;
						}
					}
				}
			}

			$urls{$ref[$i]}->{$url[$i]} = 1;
			$words{$ref[$i]}->{$word[$i]} = 1;
		}
	}

	# build a reverse lookup table of synsets -> references
	@syns = ();
	for ($i = 0; $i < 3; $i++) {
		$syns[$i] = {};
	}
	@group = keys %synsets;
	foreach $g (@group) {
		if (exists $synsets{$g}->[0]) {
			if (exists $synsets{$g}->[1]) {	
				foreach $s (keys %{$synsets{$g}->[0]}) {
					if (exists $synset{$g}->[1]->{$s} || exists $synset{$g}->[2]->{$s}) {
						if (not exists $syns[0]->{$s}) {
							$syns[0]->{$s} = {};
						}
						$syns[0]->{$s}->{$g} = 1;
					}
				}
				for ($i = 1; $i < 3; $i++) {
					foreach $s (keys %{$synsets{$g}->[$i]}) {
						if (not exists $syns[$i]->{$s}) {
							$syns[$i]->{$s} = {};
						}
						$syns[$i]->{$s}->{$g} = 1;
					}
				}
			} else {
				foreach $s (keys %{$synsets{$g}->[0]}) {
					if (not exists $syns[0]->{$s}) {
						$syns[0]->{$s} = {};
					}
					$syns[0]->{$s}->{$g} = 1;
				}
			}
		} elsif (exists $synsets{$g}->[1]) {
			for ($i = 1; $i < 3; $i++) {
				foreach $s (keys %{$synsets{$g}->[$i]}) {
					if (not exists $syns[$i]->{$s}) {
						$syns[$i]->{$s} = {};
					}
					$syns[$i]->{$s}->{$g} = 1;
				}
			}
		}
	}

	# repeatedly find "best" synset
	# delete words entry to mark a group off
	@syns0 = keys %{$syns[0]};
	@syns1 = keys %{$syns[1]};
	@syns2 = keys %{$syns[2]};
	%coref = ();
	do {
		@best = ();

		foreach $s (@syns0) {
			%synurls = ();
			%synwords = ();

			@group = keys %{$syns[0]->{$s}};
			foreach $g (@group) {
				if (exists $words{$g}) {
					foreach (keys %{$words{$g}}) {
						$synwords{$_} = 1;
					}
					foreach (keys %{$urls{$g}}) {
						$synurls{$_} += $urls{$g}->{$_};
					}
				}
			}

			@aj = keys %synurls;
			if ($#aj >= 0) {
				$x = $#aj + (1 / ((scalar keys %synwords) + 1));
				if ($#best == -1 || $x > $score) {
					$score = $x;
					@best = @group;
				}
			}
		}

		foreach $i (@syns1) {
			foreach $j (@syns2) {
				%synurls = ();
				%synwords = ();

				%hx = ();
				foreach (keys %{$syns[1]->{$i}}) {
					if (exists $syns[2]->{$j}->{$_} && 
						((not exists $synsets{$_}->[0]) || exists $synsets{$_}->[0]->{$i} || exists $synsets{$_}->[0]->{$j})) {
						$hx{$_} = 1;
					}
				}
				foreach (keys %{$syns[0]->{$i}}) {
					if (not exists $synsets{$_}->[1]) {
						$hx{$_} = 1;
					}
				}
				foreach (keys %{$syns[0]->{$j}}) {
					if (not exists $synsets{$_}->[1]) {
						$hx{$_} = 1;
					}
				}

				foreach $g (keys %hx) {
					if (exists $words{$g}) {
						foreach (keys %{$words{$g}}) {
							$synwords{$_} = 1;
						}
						foreach (keys %{$urls{$g}}) {
							$synurls{$_} += $urls{$g}->{$_};
						}
					}
				}

				@aj = keys %synurls;
				if ($#aj >= 0) {
					$x = $#aj + (1 / ((scalar keys %synwords) + 1));
					if ($#best == -1 || $x > $score) {
						$score = $x;
						@best = keys %hx;
					}
				}
			}
		}

		if ($#best != -1) {
#			print join(",", @best), "\n";
			foreach $x (@best) {
				if (exists $words{$x}) {
					$coref{$x} = $index;
					delete $words{$x};
				}
			}
			$index++;
		}
	} while ($#best != -1);

	for ($i = 0; $i <= $#url; $i++) {
		@aj = split(/\t/, $line[$i]);
		for ($j = 0; $j <= $#aj; $j++) {
			if ($j > 0) {
				print "\t";
			}
			if ($j == 4) {
				if (exists $coref{$ref[$i]}) {
					print "$coref{$ref[$i]}";
				}
			} else {
				print "$aj[$j]";
			}
		}
		print "\n";
	}
}

$wn = WordNet::QueryData->new;

%matches = ();

%lexicon = {};

open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);
	if (not exists $lexicon{$ai[0]}) {
		nlemmaAdd($ai[0]);
		$lexicon{$ai[0]} = {};
	}
	for ($i = 2; $i <= $#ai; $i++) {
		$lexicon{$ai[0]}->{$ai[$i]} = 1;
	}
}
close(file);

$img = "";
$i = 0;
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	@b = split(/\#/, $a[0]);
	if ($b[0] ne $img) {
		corefimg();

		@url = ();
		@word = ();
		@line = ();
		@ref = ();
		$i = 0;
		$img = $b[0];
	}

	if ($a[6] ne "") {
		$w = $a[6];
	} else {
		$w = nlemma(tokenize($a[9]));
	}
	for ($j = 14; $j <= $#a; $j = $j + 5) {
		if ($a[$j - 3] ne "") {
			$w = $w . "/" . $a[$j - 3];
		} else {
			$w = $w . "/" . nlemma(tokenize($a[$j]));
		}
	}

	@b = split(/\#/, $a[0]);
	$url[$i] = "$b[0]#$b[1]";
	$word[$i] = $w;
	if ($a[4] ne "") {
		$ref[$i] = $a[4];
	} else {
		$ref[$i] = $a[0];
	}
	$line[$i] = $_;
	$i++;
}
close(file);

corefimg();
