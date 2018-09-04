#!/usr/bin/perl

# ./findNewSubj2.pl <subj> <NP> <vp> <coref> <excl> <orig subj>

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

%bound = ();
open(file, "$sdir/../data/clause-boundary.txt");
while (<file>) {
	chomp($_);
	$bound{$_} = 1;
}
close(file);

%subj = ();
open(file, $ARGV[5]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$subj{$ax[0]} = $ax[1];
}
close(file);

%excl = ();
open(file, "$sdir/../data/exclverb-all.txt");
while (<file>) {
	chomp($_);
	$excl{$_} = 1;
}
close(file);
open(file, $ARGV[4]);
while (<file>) {
	chomp($_);
	$excl{$_} = 1;
}
close(file);

%sent = ();
open(file, $ARGV[3]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$sent{$ax[0]} = $ax[1];
}
close(file);

%vp = ();
open(file, $ARGV[2]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$vp{$ax[0]} = $ax[1];
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

	if (not exists $excl{$vp{$ax[0]}}) {
		@ay = split(/\#/, $ax[0]);
		$id = "$ay[0]#$ay[1]";

		$vp = 0;
		$np = 0;
		$lnp = -1;
		foreach (split(/ /, $sent{$id})) {
			@ay = split(/\//, $_);
			if ($ay[0] eq "[EN") {
				if ($#ax == 1 && $ax[1] eq "$id#NP$np") {
					if ($lnp != -1) {
						print "$s\t$id#NP$lnp\n";
					}
					last;
				}

				if (exists $np{"$id#NP$np"}) {
					$lnp = $np;
				}
				$np++;
			} elsif ($ay[0] eq "[VP") {
				if ($#ax == 0 && $ax[0] eq "$id#VP$vp") {
					if ($lnp != -1) {
						print "$s\t$id#NP$lnp\n";
					} elsif ($boundary eq "while") {
						if (exists $subj{"$id#VP0"}) {
							print "$s\t", $subj{"$id#VP0"}, "\n";
						}
					}
					last;
				}
				
				$vp++;
			} elsif (exists $bound{$ay[0]}) {
				$boundary = $ay[0];
				$lnp = -1;
			}
		}
	}
}
close(file);
