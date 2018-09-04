#!/usr/bin/perl

# HTML check file sorted by workers

%worktotal = ();
%workbad = ();

# get flagged/total caption counts for each worker
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@cols = split(/\t/, $_);

	$w = $cols[2];
	if (!exists $worktotal{$w}) {
		$worktotal{$w} = 0;
		$workbad{$w} = 0;
	}

	$worktotal{$w}++;
	if ($cols[3] < 60 || $cols[4] < 20 || $cols[5] < 5 || $cols[6] ne "") {
		$workbad{$w}++;
	}
}
close(file);

print "<html>\n";
print "<body>\n";

# sort by flag percentage
@k = sort { ($workbad{$b} / $worktotal{$b}) <=> ($workbad{$a} / $worktotal{$a}) } keys %worktotal;

for ($i = 0; $i <= $#k; $i++) {
	if ($workbad{$k[$i]} == 0) {
		last;
	}

	print "<a name=\"$k[$i]\">$k[$i] ($workbad{$k[$i]} / $worktotal{$k[$i]})</a><br>\n";

	open(file, $ARGV[0]);
	while (<file>) {
		chomp($_);
		@cols = split(/\t/, $_);

		if ($cols[2] ne $k[$i]) {
			next;
		}

		# check if flagged
		if ($cols[3] < 60 || $cols[4] < 20 || $cols[5] < 5 || $cols[6] ne "") {
			print "<font color=\"red\">";
			if ($cols[6] ne "") {
				# get misspelled words
				%mis = ();
				@c = split(/ /, $cols[6]);
				for ($j = 0; $j <= $#c; $j++) {
					$mis{$c[$j]} = 1;
				}
				@c = split(/ /, $cols[1]);
				for ($j = 0; $j <= $#c; $j++) {
					if ($j > 0) {
						print " ";
					}

					if (exists $mis{$c[$j]}) {
						print "<b>$c[$j]</b>";
					} else {
						print "$c[$j]";
					}
				}
			} else {
				print "$cols[1]";
			}

			# print additional flags
			if ($cols[3] < 60) {
				print " ($cols[3] seconds)";
			}

			if ($cols[4] < 20) {
				print " ($cols[4] chars)";
			}

			if ($cols[5] < 5) {
				print " ($cols[5] words)";
			}

			print "</font>";
			print "<br>\n";
		} else {
			print "$cols[1]<br>\n";
		}
	}
	close(file);

	print "<br>\n";
}

print "</body>\n";
print "</html>\n";
