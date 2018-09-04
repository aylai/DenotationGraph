#!/usr/bin/perl

# ./typedPMI.pl <dir>
# use calcPMI.pl to generate the .pmi file

use strict;
use warnings;

$| = 1;

my $file;

print "Reading tree\n";

my %state = ();
my %index = ();
open($file, "$ARGV[0]/node.idx");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if ($#ax == 0) {
		$ax[1] = "";
	}
	$index{$ax[0]} = $ax[1];
	$state{$ax[0]} = 0;
}
close($file);

my %link = ();
open($file, "$ARGV[0]/node-tree.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	if (not exists $link{$ax[2]}) {
		$link{$ax[2]} = {};
	}
	$link{$ax[2]}->{$ax[0]} = 1;
	$state{$ax[2]} = 1;
}
close($file);

my %vp = ();
my %en = ();
my %sn = ();
open($file, "$ARGV[0]/type-chunk.txt");
while (<$file>) {
	chomp($_);
	my @ax = split(/\t/, $_);
	my $id = shift(@ax);
	my $type = shift(@ax);
	shift(@ax);

	if ($type eq "VP") {
		if (not exists $vp{$id}) {
			$vp{$id} = {};
		}
		foreach (@ax) {
			my @ay = split(/\#/, $_);
			$vp{$id}->{$ay[0]} = 1;
		}
	} elsif ($type eq "EN") {
		if (not exists $en{$id}) {
			$en{$id} = {};
		}
		foreach (@ax) {
			my @ay = split(/\#/, $_);
			$en{$id}->{$ay[0]} = 1;
		}
	} elsif ($type eq "SN") {
		if (not exists $sn{$id}) {
			$sn{$id} = {};
		}
		foreach (@ax) {
			my @ay = split(/\#/, $_);
			$sn{$id}->{$ay[0]} = 1;
		}
	}
}
close($file);

foreach (keys %vp) {
	$vp{$_} = scalar keys %{$vp{$_}};
}
foreach (keys %en) {
	$en{$_} = scalar keys %{$en{$_}};
}
foreach (keys %sn) {
	$sn{$_} = scalar keys %{$sn{$_}};
}

print "Getting descendents\n";

my %descend = ();
foreach (keys %state) {
	if ($state{$_} == 0) {
		$descend{$_} = {};
		delete $state{$_};
	}
}

while (scalar keys %state > 0) {
	foreach my $i (keys %state) {
		my $good = 1;
		foreach my $c (keys %{$link{$i}}) {
			if (exists $state{$c}) {
				$good = 0;
				last;
			}
		}
		if ($good == 1) {
			$descend{$i} = {};
			foreach my $c (keys %{$link{$i}}) {
				$descend{$i}->{$c} = 1;
				foreach (keys %{$descend{$c}}) {
					$descend{$i}->{$_} = 1;
				}
			}
			delete $state{$i};
		}
	}
}

print "Reading PMIs\n";

my %c = ();
my %pmi = ();
my %pxy = ();

open($file, "$ARGV[0]/node-image.pmi");
while (<$file>) {
	chomp($_);

	my @ax = split(/\t/, $_);

	$c{$ax[4]} = $ax[3];
	$c{$ax[6]} = $ax[5];

	if (not exists $pmi{$ax[4]}) {
		$pmi{$ax[4]} = {};
	}
	$pmi{$ax[4]}->{$ax[6]} = $ax[0];

	if (not exists $pmi{$ax[6]}) {
		$pmi{$ax[6]} = {};
	}
	$pmi{$ax[6]}->{$ax[4]} = $ax[0];

	if (not exists $pxy{$ax[4]}) {
		$pxy{$ax[4]} = {};
	}
	$pxy{$ax[4]}->{$ax[6]} = $ax[1];

	if (not exists $pxy{$ax[6]}) {
		$pxy{$ax[6]} = {};
	}
	$pxy{$ax[6]}->{$ax[4]} = $ax[2];
}
close($file);

foreach (keys %en) {
	if (not exists $c{$_}) {
		delete $en{$_};
	}
}

foreach (keys %vp) {
	if (not exists $c{$_}) {
		delete $vp{$_};
	}
}

foreach (keys %sn) {
	if (not exists $c{$_}) {
		delete $sn{$_};
	}
}

print "Generating output\n";

system("rm -rf $ARGV[0]/pmi");
mkdir("$ARGV[0]/pmi");

my $out;

my @X = ( \%en, \%en, \%vp );
my @Y = ( \%en, \%vp, \%vp );
my @Xa = ( "en", "en", "vp" );
my @Ya = ( "en", "vp", "vp" );

for (my $i = 0; $i <= $#X; $i++) {
	open($out, ">$ARGV[0]/pmi/$Xa[$i]-$Ya[$i].pmi");
	foreach my $s (keys %{$X[$i]}) {
		foreach my $t (keys %{$Y[$i]}) {
			if ($s >= $t && $Xa[$i] eq $Ya[$i]) {
				next;
			}

			my $descend = "";
			if (exists $descend{$s}->{$t} || exists $descend{$t}->{$s}) {
				$descend = "D";
			}

			my $overlap = "";
			my %hx = ();
			foreach my $x (split(/ /, $index{$s})) {
				$hx{$x} = 1;
			}
			foreach my $y (split(/ /, $index{$t})) {
				if (exists $hx{$y}) {
					$overlap = "O";
					last;
				}
			}

			if (exists $pmi{$s} && exists $pmi{$s}->{$t}) {
				printf($out "%.3f\t%.3f\t%.3f\t%d\t%d\t%s\t%d\t%d\t%s\t%s\t%s\n",
					   $pmi{$s}->{$t}, $pxy{$s}->{$t}, $pxy{$t}->{$s},
					   $c{$s}, $X[$i]->{$s}, $index{$s}, $c{$t}, $Y[$i]->{$t}, $index{$t},
					   $descend, $overlap);
			}

		}
	}
	close($out);
}
