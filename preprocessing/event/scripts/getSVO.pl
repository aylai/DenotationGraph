#!/usr/bin/perl

# ./getStats.pl <NP> <VP> <subj> <dobj>

%npref = ();
%ref = ();
%np = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$s = shift(@ax);
	$id = shift(@ax);
	$n = join("/", @ax);
	$np{$s} = $n;
	$npref{$s} = $id;
	if (not exists $ref{$id}) {
		$ref{$id} = {};
	}
	$ref{$id}->{$n} = 1;
}
close(file);

%vp = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	$vp{$ax[0]} = $ax[1];
}
close(file);

%subj = ();
open(file, $ARGV[2]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/\#/, $ax[0]);
	@az = split(/\#/, $ax[1]);
	if ($ay[2] =~ /^VP/ && $az[2] =~ /^NP/) {
		$subj{$ax[0]} = $ax[1];
	}
}
close(file);

%dobj = ();
open(file, $ARGV[3]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	@ay = split(/\#/, $ax[0]);
	@az = split(/\#/, $ax[2]);
	if ($ay[2] =~ /^VP/ && $az[2] =~ /^NP/) {
		$dobj{$ax[0]} = $ax[1];
	}
}
close(file);

%c = ();
%x = ();
foreach $i (keys %subj) {
	$v = $vp{$i};
	$r = $npref{$subj{$i}};
	if (not exists $x{$v}) {
		$c{$v} = 0;
		$x{$v} = {};
	}
	$c{$v}++;
	foreach (keys %{$ref{$r}}) {
		$x{$v}->{$_}++;
	}
}

foreach $v (keys %vp) {
	print "$v";
	if (exists $subj{$v}) {
		print "\t$np{$subj{$v}}";
	} else {
		print "\t"
	}
	print "\t$vp{$v}";
	if (exists $dobj{$v}) {
		print "\t$np{$dobj{$v}}";
	} else {
		print "\t";
	}
	print "\n";
}
