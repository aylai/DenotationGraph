#!/usr/bin/perl

# usage ./makeCoref.pl <POS file> <corefed NP file>

%coref = ();
%synset = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	$coref{$a[0]} = $a[4];
	$synset{$a[0]} = ();
	$j = 0;
	for ($i = 6; $i <= $#a; $i += 5) {
		$synset{$a[0]}->[$j] = $a[$i];
		$j++;
	}
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ai = split(/ /, $ax[1]);
	print "$ax[0]\t";
	$en = 0;
	for ($i = 0; $i <= $#ai; $i++) {
		if ($i > 0) {
			print " ";
		}

		@ay = split(/\//, $ai[$i]);
		if ($ay[0] eq "[EN") {
			$ens = "$ax[0]#NP$en";
			if (exists $coref{$ens}) {
				print "$ay[0]/$coref{$ens}";
			} else {
				print "$ay[0]";
			}

			$np = 0;
			$en++;
			next;
		}

		if ($ay[0] eq "[NP") {
			if ($synset{$ens}->[$np] ne "") {
				print "$ay[0]/$synset{$ens}->[$np]";
			} else {
				print "$ay[0]";
			}
			$np++;
			next;
		}

		print "$ai[$i]";
	}
	print "\n";
}
close(file);
