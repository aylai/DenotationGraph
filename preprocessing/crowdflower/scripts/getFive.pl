#!/usr/bin/perl

# grab the five longest captions for each image

# count for each image/caption pair, count the number of times it
# occurred
%sent = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if (not exists $sent{$ax[0]}) {
		$sent{$ax[0]} = {};
	}
	$sent{$ax[0]}->{$ax[1]}++;
}
close(file);

# iterate over images $s
foreach $s (keys %sent) {
	$i = 0;

	do {
		$found = 0;
		# find the longest caption that still hasn't been used as many
		# times as it appeared, and print it.
		foreach (sort { length($b) <=> length ($a) } keys %{$sent{$s}}) {
			if ($sent{$s}->{$_} > 0) {
				print $s, "#", $i, "\t", $_, "\n";
				$sent{$s}->{$_}--;
				$i++;
				$found = 1;
				if ($i >= 5) {
					last;
				}
			}
		}
	} while ($i < 5 && $found == 1);
}
