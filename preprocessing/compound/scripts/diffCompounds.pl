#!/usr/bin/perl

# usage: ./diffCompounds.pl <orig token file> <new token file>

%cap = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	$cap{$a[0]} = $a[1];
}
close(file);

print "<html>\n";
print "<body>\n";
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	$s = $cap{$a[0]};
	if ($a[1] ne $s) {
		@b = split(/ /, $a[1]);
		@c = split(/ /, $s);
		$i = 0;
		$j = 0;
		while ($i <= $#b && $j <= $#c) {
			if ($b[$i] eq $c[$j]) {
				print " $b[$i]";
				$i++;
				$j++;
			} else {
				print " <b><font color=\"red\">$b[$i]</font></b>";
				$i++;
				$j++;
				$j++;
			}
		}
		print "<br>\n";
	}
}
close(file);
print "</body>\n";
print "</html>\n";
