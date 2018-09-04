#!/usr/bin/perl

# easyCorefs.pl <NP file>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use WordNet::QueryData;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

sub coref() {
	my ($c, $w, $sent, @sents);

	foreach $w (keys %occurs) {
		@sents = keys %{$occurs{$w}};
		if ($#sents > 0 && (not exists $plural{$w})) {
			$c = -1;
			foreach $sent (@sents) {
				if ($occurs{$w}->{$sent} > 1) {
					last;
				}
				$c++;
			}

			if ($c == $#sents) {
				foreach (keys %{$entity{$w}}) {
					$coref{$_} = $n;
				}
				$n++;
			}
		}
	}
}

$wn = WordNet::QueryData->new;

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);
	for ($i = 9; $i <= $#ai; $i += 5) {
		nlemmaAdd(tokenize($ai[$i]));
	}
}
close(file);

%synset = ();
open(file, "$sdir/../data/synset-lexicon.txt");
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$synset{$ax[0]} = $ax[1];
}
close(file);

%coref = ();
$n = 0;
$url = "";
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);

	@ax = split(/\#/, $ai[0]);
	if ($url ne $ax[0]) {
		coref();
		%occurs = ();
		%plural = ();
		%entity = ();
		$url = $ax[0];
	}

	for ($i = 9; $i <= $#ai; $i += 5) {
		$w = tokenize($ai[$i]);
		if (not exists $synset{$w}) {
			$synset{$w} = "";
			@forms = $wn->validForms($w . "#n");
			if ($#forms == 0) {
				@senses = $wn->querySense($forms[0]);
				if ($#senses == 0) {
					@syns = $wn->querySense($senses[0], "syns");
					$synset{$w} = $syns[0];
				}
			}
		}
	}

	$sg = 1;
	$x = tokenize($ai[9]);
	$w = $x;
	@ax = split(/ /, $ai[9]);
	@ax = split(/\//, $ax[$#ax]);
	@lemma = ();
	$lemma[$#lemma + 1] = nlemma($x);
	if ($ax[1] eq "NNS") {
		$sg = 0;
	}
	for ($i = 14; $i <= $#ai; $i += 5) {
		if ($ai[$i - 4] ne "") {
			$w = $w . " " ;
			$w = $w . tokenize($ai[$i - 4]);
		}
		$x = tokenize($ai[$i]);
		$w = $w . " " ;
		$w = $w . $x;
		$lemma[$#lemma + 1] = nlemma($x);
		@ax = split(/ /, $ai[$i]);
		@ax = split(/\//, $ax[$#ax]);
		if ($ax[1] eq "NNS") {
			$sg = 0;
		}
	}

	if ($sg == 0) {
		foreach (@lemma) {
			$plural{$_} = 1;
		}
	}

	@ax = split(/-/, $ai[0]);

	if (not exists $occurs{$w}) {
		$occurs{$w} = {};
		$entity{$w} = {};
	}
	$occurs{$w}->{$ax[0]}++;
	$entity{$w}->{$ai[0]} = 1;
	foreach (@lemma) {
		if ($_ ne $w) {
			if (not exists $occurs{$_}) {
				$occurs{$_} = {};
			}
			$occurs{$_}->{$ax[0]}++;
		}
	}
}
close(file);

coref();

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);

	print "$a[0]";
	for ($i = 1; $i <= $#a; $i++) {
		print "\t";
		if ($i == 4) {
			if (exists $coref{$a[0]}) {
				print "$coref{$a[0]}";
				next;
			}
		} elsif (($i % 5) == 1 && $a[$i] eq "" && $i > 1) {
			$w = tokenize($a[$i + 3]);
			if (exists $synset{$w} && $synset{$w} ne "") {
				print $synset{$w};
			}
		}
		print "$a[$i]";
	}
	print "\n";
}
close(file);
