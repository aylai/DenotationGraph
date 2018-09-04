#!/usr/bin/perl

# usage: ./makeHypeLexicon.pl <np file>

# match / game - how to handle?
# transport / transportation system (public transit vs. bus) - how to handle?
# color words / clothing (in red) - handle elsewhere?

use FindBin;
use lib "$FindBin::Bin/../../misc";
use util;
use WordNet::QueryData;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

sub isperson($) {
	local ($x, $i, @hypes, @syns);
	$x = $_[0];

	@syns = $wn->querySense($x, "syns");
	if ($syns[0] eq "person#n#1") {
		return 1;
	} else {
		@hypes = getHypes($x);
		
		for ($i = 0; $i <= $#hypes; $i++) {
			if (isperson($hypes[$i]) == 1) {
				return 1;
			}
		}

	}
	return 0;
}

sub isconcrete($) {
	local ($x, $i, $j, @hypes);
	$x = $_[0];
	
	if ($x eq "event#n#1" || $x eq "physical_entity#n#1" || $x eq "visual_property#n#1") {
		return 1;
	} elsif ($x eq "abstraction#n#6") {
		return 0;
	} elsif ($x =~ /^[A-Z]/) {
		return 0;
	} else {
		@hypes = getHypes($x);

		for ($i = 0; $i <= $#hypes; $i++) {
			if (isconcrete($hypes[$i]) == 1) {
				return 1;
			}
		}
	}
	return 0;
}

$wn = WordNet::QueryData->new;

%candidates = ();
%cooccur = ();
%count = ();

$candidates{"another"} = {};
$candidates{"another"}->{"person#n#1"} = {};
$candidates{"another"}->{"person#n#1"}->{"person#n#1"} = 1;
$candidates{"another"}->{"animal#n#1"} = {};
$candidates{"another"}->{"animal#n#1"}->{"animal#n#1"} = 1;

$candidates{"self"} = {};
$candidates{"self"}->{"person#n#1"} = {};
$candidates{"self"}->{"person#n#1"}->{"person#n#1"} = 1;
$candidates{"self"}->{"animal#n#1"} = {};
$candidates{"self"}->{"animal#n#1"}->{"animal#n#1"} = 1;

$candidates{"einstein"} = {};
$candidates{"einstein"}->{"person#n#1"} = {};
$candidates{"einstein"}->{"person#n#1"}->{"person#n#1"} = 1;

$candidates{"labrador"} = {};
$candidates{"labrador"}->{"labrador_retriever#n#1"} = {};
$candidates{"labrador"}->{"labrador_retriever#n#1"}->{"labrador_retriever#n#1"} = 1;

$candidates{"levi"} = {};
$candidates{"levi"}->{"levis#n#1"} = {};
$candidates{"levi"}->{"levis#n#1"}->{"levis#n#1"} = 1;

$candidates{"me"} = {};

# handle proper nouns in WordNet (like "New York")
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);
ProperNoun:
	for ($i = 9; $i <= $#ai; $i = $i + 5) {
		$w = tokenize($ai[$i]);
		nlemmaAdd($w);

		if (exists $candidates{$w} || $ai[$i - 3] ne "") {
			next;
		}

		foreach (split(/ /, $ai[$i])) {
			@ax = split(/\//, $_);
			if ($ax[1] ne "NNP") {
				next ProperNoun;
			}
		}

		@forms = $wn->validForms($w . "#n");
		if ($#forms == 0) {
			@senses = $wn->querySense($forms[0]);
			foreach $sense (@senses) {
				@insts = $wn->querySense($sense, "inst");
				if ($#insts == -1) {
					next ProperNoun;
				}
			}

			$candidates{$w} = {};
			for ($j = 0; $j <= $#senses; $j++) {
				@insts = $wn->querySense($senses[$j], "inst");
				foreach $inst (@insts) {
					if (not exists $candidates{$w}->{$inst}) {
						$candidates{$w}->{$inst} = {};
					}
					if (j == 0) {
						$candidates{$w}->{$inst}->{$inst} = 1;
					}
				}
			}
		}
	}
}

%person = ();
open(file, "$sdir/../data/person.txt");
while (<file>) {
	chomp($_);
	$person{$_} = 1;
}
close(file);

%notperson = ();
open(file, "$sdir/../data/notperson.txt");
while (<file>) {
	chomp($_);
	$notperson{$_} = 1;
}
close(file);

$url = "";

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ai = split(/\t/, $_);

	for ($i = 9; $i <= $#ai; $i = $i + 5) {
		if ($ai[$i - 3] eq "") {
			@ax = split(/ /, $ai[$i]);
			@ax = split(/\//, $ax[$#ax]);
			$pos = $ax[1];
			if ($pos eq "PRP") {
				next;
			}

			$w = nlemma(tokenize($ai[$i]));
		} else {
			$w = $ai[$i - 3];
		}

		@ax = split(/[\#]/, $ai[0]);
		if ($url ne $ax[0]) {
			$url = $ax[0];
			@words = ();
		}

		if (not exists $words[$ax[1]]) {
			$words[$ax[1]] = {};
		}
		$words[$ax[1]]->{$w} = 1;

		$count{$w}++;
		if (not exists $cooccur{$w}) {
			$cooccur{$w} = {};
		}

		for ($j = 0; $j <= $#words; $j++) {
			if ($j == $ax[1]) {
				next;
			}
			foreach (keys %{$words[$j]}) {
				$cooccur{$w}->{$_}++;
				$cooccur{$_}->{$w}++;
			}
		}

		if (not exists $candidates{$w}) {
			$candidates{$w} = {};
			
			if ($w eq $ai[$i - 3]) {
				$candidates{$w}->{$w} = {};
				$candidates{$w}->{$w}->{$w} = 1;
			} else {
				$synsets = 0;

				$x = $w;
				%forms = ();
				foreach ($wn->validForms($x . "#n")) {
					$forms{$_} = 1;
				}

				$x = $w;
				$x =~ s/-/ /g;
				if ($x ne $w) {
					foreach ($wn->validForms($x . "#n")) {
						$forms{$_} = 1;
					}
				}

				$x = $w;
				$x =~ s/-//g;
				if ($x ne $w) {
					foreach ($wn->validForms($x . "#n")) {
						$forms{$_} = 1;
					}
				}
				@forms = keys %forms;
				foreach $form (@forms) {
					@sense = $wn->querySense($form);
					if ($#sense >= 0) {
						$freqZero = int($wn->frequency($sense[0]) / 10);
						if ($notperson{$form} == 1) {
							$p = 0;
						} else {
							$p = isperson($sense[0]);
						}
						
						for ($j = 0; $j <= $#sense && $j < 4; $j++) {
							$freq = $wn->frequency($sense[$j]);
							if ($freq >= $freqZero && isconcrete($sense[$j]) == 1) {
								if ($person{$form} == 1 || $p == isperson($sense[$j])) {
									@synset = $wn->querySense($sense[$j], "syns");
									if (not exists $candidates{$w}->{$synset[0]}) {
										$candidates{$w}->{$synset[0]} = {};
									}
									if ($synsets == 0) {
										$candidates{$w}->{$synset[0]}->{$synset[0]} = 1;
									}
									$synsets++;
#								} elsif ($p == 1) {
#									@glos = $wn->querySense($sense[$j], "glos");
#									$z = tokenize($ai[$i - 2]) . " " . tokenize($ai[$i - 1]) . " " . tokenize($ai[$i]);
#									print "$form\t[$z]\t@glos\n";
								}
							}
						}
					}
				}
				
				if ($synsets == 0 && $pos =~ /^N/) {
					if ($#forms >= 0) {
						$candidates{$w}->{"$w#n#1"} = {};
						$candidates{$w}->{"$w#n#1"}->{"$w#n#1"} = 1;
						$synsets++;
					} else {
						if ($w =~ /^(.*)er$/) {
							$v = $1;
							@b = $wn->validForms($v . "e#v");
							@c = $wn->validForms($v . "#v");
							if (($#b >= 0 || $#c >= 0) && 
								$v ne "raz" && $v ne "scoop") {
								$candidates{$w}->{"person#n#1"} = {};
								$candidates{$w}->{"person#n#1"}->{"person#n#1"} = 1;
								$synsets++;
							}
						}
					}
				}
				
				if ($synsets == 0) {
					$candidates{$w}->{"_$w#n#1"} = {};
					$candidates{$w}->{"_$w#n#1"}->{"_$w#n#1"} = 1;
				}
			}
		}
	}
}
close(file);

foreach $w (keys %cooccur) {
	foreach $s (keys %{$candidates{$w}}) {
		%visit = ();
		@queue = ();

		$visit{$s} = 1;
		push(@queue, $s);

		foreach $q (@queue) {
			if ($q ne "living_thing#n#1" && $q ne "person#n#1" && $q ne "plant#n#1" && $q ne "animal#n#1") {
				@hypes = getHypes($q);
				foreach $h (@hypes) {
					if (not exists $visit{$h}) {
						$visit{$h} = 1;
						push(@queue, $h);
					}
				}
			}
		}

		foreach $x (keys %{$cooccur{$w}}) {
			if ($x eq $w) {
				next;
			}

			foreach $t (keys %{$candidates{$x}}) {
				if (exists $visit{$t}) {
					if ($t ne "object#n#1" && $t ne "clothing#n#1") {
						$candidates{$w}->{$s}->{$t} = 1;
						$candidates{$x}->{$t}->{$t} = 1;
# attempt to calculate "goodness" of an upwards link - not working so far.	Try again later.
# the idea here is, an upward link is good if it's a common replacement.  If there's 1000 mentions of X
# and 1 mention of hypernym of X, then the hypernym may not be such a common way of referring to X.
					}
				}
			}
		}

		if (exists $visit{"person#n#1"}) {
			$candidates{$w}->{$s}->{"person#n#1"} = 1;
		}
	}
}

foreach $w (sort keys %candidates) {
	foreach $s (sort { scalar keys %{$candidates{$w}->{$b}} <=> scalar keys %{$candidates{$w}->{$a}} } keys %{$candidates{$w}}) {
		@synsets = sort keys %{$candidates{$w}->{$s}};
		if ($#synsets >= 0) {
			print "$w\t$s\t", join("\t", @synsets), "\n";
		}
	}
}
