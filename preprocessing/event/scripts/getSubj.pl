#!/usr/bin/perl

%dep = ();
%subj = ();
%conj = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);

	if ($#ax == 4) {
		if ($ax[4] eq "nsubj") {
			@np = split(/\#/, $ax[1]);
			@vp = split(/\#/, $ax[3]);
			if ($np[0] =~ /^NP/ && $vp[0] =~ /^VP/ && not exists $subj{"$ax[0]#$vp[0]"}) {
				print "$ax[0]#$vp[0]\t$ax[0]#$np[0]\n";
				$subj{"$ax[0]#$vp[0]"} = "$ax[0]#$np[0]";
			}
		} elsif ($ax[4] eq "partmod") {
			@np = split(/\#/, $ax[3]);
			@vp = split(/\#/, $ax[1]);
			if ($np[0] =~ /^NP/ && $vp[0] =~ /^VP/ && not exists $subj{"$ax[0]#$vp[0]"}) {
				print "$ax[0]#$vp[0]\t$ax[0]#$np[0]\n";
				$subj{"$ax[0]#$vp[0]"} = "$ax[0]#$np[0]";
			}
		} elsif ($ax[4] eq "conj" || $ax[4] eq "xcomp") {
			@src = split(/\#/, $ax[1]);
			@trg = split(/\#/, $ax[3]);
			if ($src[0] =~ /^VP/ && $trg[0] =~ /^VP/ && not exists $conj{"$ax[0]#$src[0]"}) {
				$conj{"$ax[0]#$src[0]"} = "$ax[0]#$trg[0]";
			}
		} elsif ($ax[4] eq "dep") {
			@src = split(/\#/, $ax[1]);
			@trg = split(/\#/, $ax[3]);
			if ($src[0] =~ /^VP/ && $trg[0] eq "NP0") {
				$dep{"$ax[0]#$src[0]"} = "$ax[0]#$trg[0]";
			}
		}
	}
}
close(file);

foreach (keys %dep) {
	if (not exists $subj{$_}) {
		$subj{$_} = $dep{$_};
		print "$_\t$subj{$_}\n";
	}
}

foreach (keys %conj) {
	if (exists $subj{$conj{$_}} && not exists $subj{$_}) {
		print "$_\t$subj{$conj{$_}}\n";
	}
}
