#!/usr/bin/perl

# ./getSVON.pl <graph dir>

use strict;
use warnings;

my $file;
my %node = ();;
my %np = ();
my %subj = ();
my %vp = ();
my %verb = ();
my %dobj = ();

open($file, "$ARGV[0]/cap-node.map");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	$node{$id} = {};
	$np{$id} = {};
	$subj{$id} = {};
	$vp{$id} = {};
	$verb{$id} = {};
	$dobj{$id} = {};
	foreach (@ax) {
		$node{$id}->{$_} = {};
	}
}
close($file);

open($file, "$ARGV[0]/node-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);

	my $x = shift(@ax);
	my $label = shift(@ax);
	my $y = shift(@ax);

	if ($label eq "SENT") {
		foreach (@ax) {
			if (exists $node{$_}->{$y}) {
				$np{$_}->{$y} = 1;
			}
		}
	} elsif ($label eq "VERB") {
		foreach (@ax) {
			if (exists $node{$_}->{$y}) {
				$subj{$_}->{$y} = 1;
			}
		}
	} elsif ($label eq "SUBJ" || $label eq "COMPLEX-VERB") {
		foreach (@ax) {
			if (exists $node{$_}->{$y}) {
				$vp{$_}->{$y} = 1;
			}
		}
	} elsif ($label eq "DOBJ") {
		foreach (@ax) {
			if (exists $node{$_}->{$y}) {
				$verb{$_}->{$y} = 1;
			}
		}
	} elsif ($label eq "TVERB") {
		foreach (@ax) {
			if (exists $node{$_}->{$y}) {
				$dobj{$_}->{$y} = 1;
			}
		}
	}

	foreach (@ax) {
		my @ay = split(/\#/, $_);
		$_ = join("#", @ay[0..1]);
		if (exists $node{$_}->{$x}) {
			if ($label eq "SENT") {
				$node{$_}->{$x}->{$y} |= 1;
			} else {
				$node{$_}->{$x}->{$y} |= 2;
			}
		}
	}
}
close($file);

foreach my $id (keys %dobj) {
	if ((scalar keys %{$dobj{$id}}) == 0) {
		foreach (keys %{$vp{$id}}) {
			$verb{$id}->{$_} = 1;
		}
	}
}

foreach my $id (sort keys %node) {
	my $delta;

	do {
		$delta = 0;
		foreach my $x (keys %{$subj{$id}}) {
			if (exists $node{$id}->{$x}) {
				foreach my $y (keys %{$node{$id}->{$x}}) {
					if (($node{$id}->{$x}->{$y} & 2) != 0) {
						if (not exists $subj{$id}->{$y}) {
							$subj{$id}->{$y} = 1;
							$delta = 1;
						}
					}
				}
			}
		}
	} while ($delta == 1);

	do {
		$delta = 0;
		foreach my $x (keys %{$vp{$id}}) {
			if (exists $node{$id}->{$x}) {
				foreach my $y (keys %{$node{$id}->{$x}}) {
					if (($node{$id}->{$x}->{$y} & 2) != 0 && (!exists $dobj{$id} || !exists $dobj{$id}->{$y})) {
						if (not exists $vp{$id}->{$y}) {
							$vp{$id}->{$y} = 1;
							$delta = 1;
						}
					}
				}
			}
		}
	} while ($delta == 1);

	do {
		$delta = 0;
		foreach my $x (keys %{$verb{$id}}) {
			if (exists $node{$id}->{$x}) {
				foreach my $y (keys %{$node{$id}->{$x}}) {
					if (($node{$id}->{$x}->{$y} & 2) != 0) {
						if (not exists $verb{$id}->{$y}) {
							$verb{$id}->{$y} = 1;
							$delta = 1;
						}
					}
				}
			}
		}
	} while ($delta == 1);

	do {
		$delta = 0;
		foreach my $x (keys %{$dobj{$id}}) {
			if (exists $node{$id}->{$x}) {
				foreach my $y (keys %{$node{$id}->{$x}}) {
					if (($node{$id}->{$x}->{$y} & 2) != 0) {
						if (not exists $dobj{$id}->{$y}) {
							$dobj{$id}->{$y} = 1;
							$delta = 1;
						}
					}
				}
			}
		}
	} while ($delta == 1);

	foreach (keys %{$np{$id}}) {
		if (exists $dobj{$id}->{$_} || exists $subj{$id}->{$_}) {
			delete $np{$id}->{$_};
		}
	}
	do {
		$delta = 0;
		foreach my $x (keys %{$np{$id}}) {
			if (exists $node{$id}->{$x}) {
				foreach my $y (keys %{$node{$id}->{$x}}) {
					if (($node{$id}->{$x}->{$y} & 2) != 0) {
						if (not exists $np{$id}->{$y}) {
							$np{$id}->{$y} = 1;
							$delta = 1;
						}
					}
				}
			}
		}
	} while ($delta == 1);

	my %hx;
	my %hy;
	my @ay;
	
	print $id;

	@ay = ();
	%hx = ();
	%hy = ();
	foreach (keys %{$subj{$id}}) {
		foreach (keys %{$node{$id}->{$_}}) {
			if (exists $subj{$id}->{$_}) {
				$hx{$_} = 1;
			}
		}
	}
	foreach (keys %{$subj{$id}}) {
		if (not exists $hx{$_}) {
			$hy{$_} = 1;
		}
	}
	if ((scalar keys %hy) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hy));
	}
	if ((scalar keys %hx) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hx));
	}
	print "\t", join("/", @ay);

	@ay = ();
	%hx = ();
	%hy = ();
	foreach (keys %{$vp{$id}}) {
		foreach (keys %{$node{$id}->{$_}}) {
			if (exists $vp{$id}->{$_}) {
				$hx{$_} = 1;
			}
		}
	}
	foreach (keys %{$vp{$id}}) {
		if (not exists $hx{$_}) {
			$hy{$_} = 1;
		}
	}
	if ((scalar keys %hy) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hy));
	}
	if ((scalar keys %hx) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hx));
	}
	print "\t", join("/", @ay);

	@ay = ();
	%hx = ();
	%hy = ();
	foreach (keys %{$verb{$id}}) {
		foreach (keys %{$node{$id}->{$_}}) {
			if (exists $verb{$id}->{$_}) {
				$hx{$_} = 1;
			}
		}
	}
	foreach (keys %{$verb{$id}}) {
		if (not exists $hx{$_}) {
			$hy{$_} = 1;
		}
	}
	if ((scalar keys %hy) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hy));
	}
	if ((scalar keys %hx) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hx));
	}
	print "\t", join("/", @ay);

	@ay = ();
	%hx = ();
	%hy = ();
	foreach (keys %{$dobj{$id}}) {
		foreach (keys %{$node{$id}->{$_}}) {
			if (exists $dobj{$id}->{$_}) {
				$hx{$_} = 1;
			}
		}
	}
	foreach (keys %{$dobj{$id}}) {
		if (not exists $hx{$_}) {
			$hy{$_} = 1;
		}
	}
	if ((scalar keys %hy) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hy));
	}
	if ((scalar keys %hx) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hx));
	}
	print "\t", join("/", @ay);

	@ay = ();
	%hx = ();
	%hy = ();
	foreach (keys %{$np{$id}}) {
		foreach (keys %{$node{$id}->{$_}}) {
			if (exists $np{$id}->{$_}) {
				$hx{$_} = 1;
			}
		}
	}
	foreach (keys %{$np{$id}}) {
		if (not exists $hx{$_}) {
			$hy{$_} = 1;
		}
	}
	if ((scalar keys %hy) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hy));
	}
	if ((scalar keys %hx) > 0) {
		push(@ay, join(",", sort {$a <=> $b} keys %hx));
	}
	print "\t", join("/", @ay), "\n";
}
