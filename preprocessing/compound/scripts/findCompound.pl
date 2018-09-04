#!/usr/bin/perl

# ./findCompound.pl <token file>

# N - compound is a noun known to WordNet
# V - compound is a verb known to WordNet
# !n - known noun compound
# !v - known verb compound
# !s - known split term

use WordNet::QueryData;

$wn = WordNet::QueryData->new;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

%nouns = ();
open(file, "$sdir/../data/nouns.txt");
while (<file>) {
	chomp($_);
	$nouns{$_} = 1;
}
close(file);

%verbs = ();
open(file, "$sdir/../data/verbs.txt");
while (<file>) {
	chomp($_);
	$verbs{$_} = 1;
}
close(file);

%split = ();
open(file, "$sdir/../data/split.txt");
while (<file>) {
	chomp($_);
	$split{$_} = 1;
}
close(file);

%c = ();
%word = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	$last = "";
	foreach (split(/ /, $a[1])) {
		$x = lc($_);
		$y = $x;
		$y =~ s/-//g;
		$c{$y}++;
		if (not exists $word{$y}) {
			$word{$y} = {};
		}
		$word{$y}->{$x}++;

		if ($last ne "" && $x ne "") {
			$z = "$last$x";
			$c{$z}++;
			if (not exists $word{$z}) {
				$word{$z} = {};
			}
			$word{$z}->{"$last $x"}++;
		}
		$last = $x;
	}
}
close(file);

foreach (sort { $c{$b} <=> $c{$a} } keys %c) {
	@ax = keys %{$word{$_}};
	if ($#ax > 0) {
		@ay = ();
		foreach $x (sort { $word{$_}->{$b} <=> $word{$_}->{$a} } @ax) {
			$n = 0;
			$v = 0;
			if ($x ne "") {
				@sense = $wn->querySense($x);
				foreach $s (@sense) {
					@az = split(/\#/, $s);
					if ($az[1] eq "n") {
						$n = 1;
					}
					if ($az[1] eq "v") {
						$v = 1;
					}
				}
			}

			$s = $x;
			if ($n == 0) {
				if ($v == 0) {
				} else {
					$s = $s . " (V)";
				}
			} else {
				if ($v == 0) {
					$s = $s . " (N)";
				} else {
					$s = $s . " (NV)";
				}
			}
			push(@ay, sprintf("%5d %-20.20s", $word{$_}->{$x}, $s));
		}
		print join("\t", @ay), "\n";
	}
}

