#!/usr/bin/perl

# ./renumberConll.pl <coref> <conll>

%map = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$x = 0;
	$vp = -1;
	$en = -1;
	$state = 0;
	$depth = 0;
	$i = 0;
	foreach (split(/ /, $ax[1])) {
		@ay = split(/\//, $_);
	    if ($ay[0] =~ /^[\[\]]/) {
			if ($ay[0] =~ /^\[/) {
				$depth++;
				if ($ay[0] eq "[EN") {
					$state = 1;
					$en++;
					$i = 0;
				} elsif ($ay[0] eq "[VP") {
					$state = 2;
					$vp++;
					$i = 0;
				}
			} elsif ($ay[0] eq "]") {
				$depth--;
				if ($depth == 0) {
					$state = 0;
				}
			}
		} else {
			$id = $ax[0] . "#" . ($x + 1);

			if ($state == 1) {
				$map{$id} = "NP$en#$i";
			} elsif ($state == 2) {
				$map{$id} = "VP$vp#$i";
			} else {
				$map{$id} = $x;
			}

			$x++;
			$i++;
		}
	}
}
close(file);

open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if ($#ax == 4) {
		$id = "$ax[0]#$ax[1]";
		if (exists $map{$id}) {
			$ax[1] = $map{$id};
		}

		$id = "$ax[0]#$ax[3]";
		if (exists $map{$id}) {
			$ax[3] = $map{$id};
		}
	}

	print join("\t", @ax), "\n";
}
close(file);
