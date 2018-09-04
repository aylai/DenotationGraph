#!/usr/bin/perl

$c = 0;
$neg = 0;
if ($ARGV[1] =~ /^-(.*)/) {
    open(file, $1);
    $neg = 1;
} else {
    open(file, $ARGV[1]);
}

@sharps = ();
$filter = {};
while (<file>) {
    chomp($_);

	@ax = split(/\t/, $_);
	$c = $#ax;
	if ($#ARGV > 1 && $c >= $ARGV[2]) {
		$c = $ARGV[2] - 1;
	}
	$x = $filter;
	for ($i = 0; $i <= $c; $i++) {
		@ay = split(/\#/, $ax[$i]);
		$sharps[$i] = $#ay;
		if (not exists $x->{$ax[$i]}) {
			$x->{$ax[$i]} = {};
		}
		$x = $x->{$ax[$i]};
	}
}
close(file);

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
    @ax = split(/\t/, $_);

	$x = $filter;
	for ($i = 0; $i <= $c; $i++) {
		@ay = split(/\#/, $ax[$i]);
		$id = "";
		for ($j = 0; $j <= $sharps[$i]; $j++) {
			if ($j > 0) {
				$id = $id . "#";
			}
			$id = $id . $ay[$j];
		}

		if (exists $x->{$id}) {
			$x = $x->{$id};
		} else {
			last;
		}
	}

    if ($i > $c) {
		if ($neg == 0) {
			print "$_\n";
		}
    } elsif ($neg == 1) {
		print "$_\n";
    }
}
close(file);
