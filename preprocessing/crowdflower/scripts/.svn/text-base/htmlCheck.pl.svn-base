#!/usr/bin/perl

# HTML check file page sorted by image

print "<html>\n";
print "<body>\n";
print "<table>\n";

$last = "";
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@cols = split(/\t/, $_);
	# check if this is a new image
	if ($cols[0] ne $last) {
		if ($last ne "") {
			print "</td>\n";
			print "</tr>\n";
		}

		$last = $cols[0];
		print "<tr>\n";
		print "<td><img src=\"images2/$cols[0]\"></td>\n";
		print "<td>\n";
	}

	# check if we should flag
	if ($cols[3] < 60 || $cols[4] < 20 || $cols[5] < 5 || $cols[6] ne "") {
		print "<a href=\"results_1k-worker.html\#$cols[2]\">";
		print "<font color=\"red\">";
		if ($cols[6] ne "") {
			# get misspelled words
			%wrong = ();
			@d = split(/ /, $cols[6]);
			for ($i = 0; $i <= $#d; $i++) {
				$wrong{$d[$i]} = 1;
			}
			@d = split(/ /, $cols[1]);
			for ($i = 0; $i <= $#d; $i++) {
				if ($i > 0) {
					print " ";
				}
				if (!exists $wrong{$d[$i]}) {
					print "$d[$i]";
				} else {
					print "<b>$d[$i]</b>";
				}
			}
		} else {
			print "$cols[1]";
		}
		print "</font>";

		# print additional flags
		if ($cols[3] < 60) {
			print " ($cols[3] seconds)";
		}

		if ($cols[4] < 20) {
			print " ($cols[4] characters)";
		}

		if ($cols[5] < 5) {
			print " ($cols[5] words)";
		}

		print "</a>";
		print "<br>\n";
	} else {
		print "$cols[1]<br>\n";
	}
}
print "</td>\n";
print "</tr>\n";
close(file);

print "</table>\n";
print "</body>\n";
print "</html>\n";
