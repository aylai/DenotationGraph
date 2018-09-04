#!/usr/bin/perl

# ./coverCorefs.pl <NP> <lexicon>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;

%lexicon = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$w = shift(@ax);
	if (not exists $lexicon{$w}) {
		$lexicon{$w} = {};
	}
	shift(@ax);
	foreach (@ax) {
		$lexicon{$w}->{$_} = 1;
	}
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	for ($i = 9; $i <= $#ax; $i += 5) {
		nlemmaAdd(tokenize($ax[$i]));
	}
}
close(file);

%coref = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if ($ax[4] eq "") {
		$ax[4] = $ax[0];
	}

	if (not exists $coref{$ax[4]}) {
		$coref{$ax[4]} = {};
	}
	@aw = ();
	for ($i = 6; $i <= $#ax; $i += 5) {
		if ($ax[$i] ne "") {
			push(@aw, $ax[$i]);
		} else {
			push(@aw, nlemma(tokenize($ax[$i + 3])));
		}
	}
	$coref{$ax[4]}->{join("/", @aw)} = 1;
}
close(file);

foreach $id (sort { $a <=> $b } keys %coref) {
	@cover = ();

	for ($i = 1; $i >= 0; $i--) {
		foreach (keys %{$coref{$id}}) {
			@ax = split(/\//, $_);
			if ($#ax == $i) {
				if ($#cover == -1) {
					for ($j = 0; $j <= $i; $j++) {
						$cover[$j] = {};
						foreach (keys %{$lexicon{$ax[$j]}}) {
							$cover[$j]->{$_} = 1;
						}
					}
				} elsif ($#cover == $#ax) {
					for ($j = 0; $j <= $i; $j++) {
						foreach (keys %{$lexicon{$ax[$j]}}) {
							if (exists $cover[$j]->{$_}) {
								$cover[$j]->{$_} = 1;
							}
						}
					}
				} else {
					%selected = ();
					for ($j = 0; $j <= $#ax; $j++) {
						%left = ();
						for ($k = 0; $k <= $#cover; $k++) {
							foreach (keys %{$lexicon{$ax[$j]}}) {
								if (exists $cover[$k]->{$_}) {
									$left{$k}++;
								}
							}
						}

						@aleft = sort { $left{$b} <=> $left{$a} } keys %left;
						if ($#aleft >= 0) {
							foreach (keys %{$lexicon{$ax[$j]}}) {
								if (exists $cover[$aleft[0]]->{$_}) {
									$cover[$aleft[0]]->{$_} = 1;
								}
							}
							$selected{$aleft[0]} = 1;
						}
					}

					for ($j = 0; $j <= $#cover; $j++) {
						if (not exists $selected{$j}) {
							foreach (keys %{$cover[$j]}) {
								$cover[$j]->{$_} = 1;
							}
						}
					}
				}

				for ($j = 0; $j <= $#cover; $j++) {
					foreach (keys %{$cover[$j]}) {
						if ($cover[$j]->{$_} == 1) {
							$cover[$j]->{$_} = 0;
						} else {
							delete $cover[$j]->{$_};
						}
					}
				}
			}
		}
	}

	print "$id";
	for ($j = 0; $j <= $#cover; $j++) {
		print "\t", join(",", sort keys %{$cover[$j]});
	}
	print "\n";
}
