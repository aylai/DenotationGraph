#!/usr/bin/perl

# ./findNewSubj2.pl <subj> <NP> <coref>

%sent = ();
open(file, $ARGV[2]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$sent{$ax[0]} = $ax[1];
}
close(file);

%np = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	$np{$_} = 1;
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	$s = $_;
	@ax = split(/\t/, $_);
	@ay = split(/\#/, $ax[0]);
	$id = "$ay[0]#$ay[1]";

	$np = 0;
	$depth = 0;
	$prior = -1;
	foreach (split(/ /, $sent{$id})) {
		@ay = split(/\//, $_);

		if ($ay[0] =~ /^\[/) {
			if ($ay[0] eq "[EN") {
				if ($ax[1] eq "$id#NP$np") {
					if ($prior != -1) {
						print "$ax[0]\t$id#NP$np\t$id#NP$prior\n";
					}
					last;
				}
				$prior = $np;
				$np++;
			} elsif ($depth == 0) {
				$prior = -1;
			}

			$depth++;
		}

		if ($ay[0] =~ /^\]/) {
			$depth--;
		}
	}
}
close(file);
