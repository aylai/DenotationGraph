#!/usr/bin/perl

# untokenize.pl <token>

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$ax[1] = ucfirst($ax[1]);
	@ai = split(/ /, $ax[1]);
	$s = "";
	$match = "";
	$skip = 0;
	for ($i = 0; $i <= $#ai; $i++) {
		if ($s ne "") {
			if ($skip == 1) {
				$z = "";
				$skip = 0;
			} else {
				$z = " ";
			}

			if ($match eq $ai[$i]) {
				$match = "";
			} elsif ($ai[$i] eq "(" || $ai[$i] eq "\"" || $ai[$i] eq "'") {
				$match = $ai[$i];
				if ($match eq "(") {
					$match = ")";
				}
				for ($j = $i + 1; $j <= $#ai; $j++) {
					if ($ai[$j] eq $match) {
						last;
					}
				}

				if ($j > $#ai) {
					$match = "";
				} else {
					$skip = 1;
				}
				if ($ai[$i] eq "'" && $match eq "") {
				} else {
					$s = $s . $z;
				}
			} elsif ($ai[$i] eq "'s" || $ai[$i] eq "n't") {
			} elsif ($ai[$i] eq "!" || $ai[$i] eq "." || $ai[$i] eq "?" || $ai[$i] eq "," || $ai[$i] eq ";") {
			} elsif ($ai[$i] eq "#") {
				$s = $s . $z;
				$skip = 1;
			} else {
				$s = $s . $z;
			}
		}

		$s = $s . $ai[$i];
	}

	print "$ax[0]\t$s\n";
}
close(file);
