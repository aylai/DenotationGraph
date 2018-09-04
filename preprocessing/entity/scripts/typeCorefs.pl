#!/usr/bin/perl

# usage: ./typeCorefs.pl <NP file>

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use WordNet::QueryData;

$| = 1;

%parents = ();
%parts = ();

sub ispar($$) {
	my $x = $_[0];
	my $y = $_[1];

	if (isparent($x, $y)) {
		return 1;
	}

	if (not exists $parts{$x}) {
		$parts{$x} = {};
		foreach my $h ($wn->querySense($x, "hprt")) {
			$parts{$x}->{$h} = 1;
		}
	}

	foreach (keys %{$parts{$x}}) {
		if (isparent($x, $_)) {
			return 1;
		}
	}
	return 0;
}

sub isparent($$) {
	my $x = $_[0];
	my $y = $_[1];

	if (not exists $parents{$x}) {
		$parents{$x} = {};

		my @q = ();
		push(@q, $x);
		$parents{$x}->{$x} = 1;

		while ($#q >= 0) {
			my $z = pop(@q);

			foreach my $h (getHypes($z)) {
				if (not exists $parents{$x}->{$h}) {
					$parents{$x}->{$h} = 1;
					push(@q, $h);
				}
			}
		}
	}

	return exists $parents{$x}->{$y};
}

$wn = WordNet::QueryData->new;

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);
	for ($i = 9; $i <= $#ai; $i = $i + 5) {
		$w = tokenize($ai[$i]);
		nlemmaAdd($w);
	}
}
close(file);

%cooccur = ();
%words = ();
%coref = ();

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);
	$w = "";
	for ($i = 9; $i <= $#ai; $i = $i + 5) {
		$w = nlemma(tokenize($ai[$i]));
	}

	$words{$w} = 0;
	$cooccur{$w} = {};

	if (not exists $coref{$ai[3]}) {
		$coref{$ai[3]} = {};
	}
	$coref{$ai[3]}->{$w} = 1;
}
close(file);

foreach $ref (keys %coref) {
	@word = keys %{$coref{$ref}};
	for ($i = 0; $i <= $#word; $i++) {
		$words{$word[$i]}++;
		for ($j = $i + 1; $j <= $#word; $j++) {
			$cooccur{$word[$i]}->{$word[$j]}++;
			$cooccur{$word[$j]}->{$word[$i]}++;
		}
	}
}

%assign = ();
%candidates = ();
foreach $w (keys %words) {
	if (not exists $candidates{$w}) {
		$candidates{$w} = ();
		@sense = $wn->querySense($w . "#n");
		if ($#sense == -1) {
			@forms = $wn->validForms($w . "#n");
			foreach $f (@forms) {
				$i = 0;
				@sense = $wn->querySense($f);
				foreach $s (@sense) {
					@synset = $wn->querySense($s, "syns");
					$candidates{$w}->[$i] = $synset[0];
					if ($i == 0) {
						$assign{$w} = $synset[0];
					}
					$i++;
				}
			}
		} else {
			for ($i = 0; $i <= $#sense; $i++) {
				@synset = $wn->querySense($sense[$i], "syns");
				$candidates{$w}->[$i] = $synset[0];
				if ($i == 0) {
					$assign{$w} = $synset[0];
				}
			}
		}
	}
}

%scores = ();
foreach $w (sort keys %assign) {
	$senses = $candidates{$w};
	if ($#$senses > 0) {
		@neighbors = keys %{$cooccur{$w}};
		$scores{$w} = ();
		for ($i = 0; $i <= $#$senses; $i++) {
			$s = $senses->[$i];
			$scores{$w}->[$i] = 0;
			foreach $n (@neighbors) {
				if (exists $assign{$n}) {
					if (isparent($s, $assign{$n}) || isparent($assign{$n}, $s)) {
					} elsif (ispar($s, $assign{$n}) || ispar($assign{$n}, $s)) {
						$scores{$w}->[$i] += $cooccur{$w}->{$n} / 2;
					} else {
						$scores{$w}->[$i] += $cooccur{$w}->{$n};
					}
				}
			}
		}
	}
}


# EMish type resolution

do {
	$update = 0;

	foreach $w (sort keys %assign) {
		$ws = $scores{$w};
		if ($#$ws <= 0) {
			next;
		}

		$b = 0;
		for ($i = 1; $i <= $#$ws; $i++) {
			if ($ws->[$i] < $ws->[$b]) {
				$b = $i;
			}
		}

		$old = $assign{$w};
		$new = $candidates{$w}->[$b];
		if ($old ne $new) {
			$assign{$w} = $new;

			@neighbors = keys %{$cooccur{$w}};
			foreach $n (@neighbors) {
				$ns = $scores{$n};
				if ($#$ns <= 0) {
					next;
				}

				for ($i = 0; $i <= $#$ns; $i++) {
					$ni = $candidates{$n}->[$i];

					if (isparent($old, $ni) || isparent($ni, $old)) {
					} elsif (ispar($old, $ni) || ispar($ni, $old)) {
						$ns->[$i] -= $cooccur{$w}->{$n} / 2;
					} else {
						$ns->[$i] -= $cooccur{$w}->{$n};
					}

					if (isparent($new, $ni) || isparent($ni, $new)) {
					} elsif (ispar($new, $ni) || ispar($ni, $new)) {
						$ns->[$i] += $cooccur{$w}->{$n} / 2;
					} else {
						$ns->[$i] += $cooccur{$w}->{$n};
					}
				}
			}

			$update++;
		}
	}
} while ($update > 0);

foreach $w (sort keys %assign) {
	print $w, "\t", $assign{$w}, "\n";
}
