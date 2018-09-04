#!/usr/bin/perl

# converts a CSV file to a TSV file.  It's pretty much a bunch of
# regular expressions.  Should handle multi-line expressions.


# ampersanded characters
%amp = ();
$amp{"amp"} = "&";
$amp{"gt"} = ">";
$amp{"lt"} = "<";

open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	$x = $_;
	while ($x ne "") {

		# are we dealing with quoted strings?
		# $y should hold the output string.
		# $z should hold the unprocessed part of the line
		if ($x =~ /^\"/) {
		    do {
				# we need an ending double quote to close the string.
				# So try to find the ending double quote (double
				# double quotes are actually just a single double
				# quote), and if we can't find it, add the next line,
				# and repeat until we do.

				$x =~ m/^\"(([^\"]|\"\")*)\"(.*)/;
				$y = $1;
				$z = $3;
				$y =~ s/\"\"/\"/g;
				$y =~ s/\r//g;

				# if $1 and $3 are empty, we still haven't found the
				# ending double quote.  Add a line.
				if ($y eq "" && $z eq "") {
					$_ = <file>;
					chomp($_);
					$x = "$x $_";
				}
			} while ($y eq "" && $z eq "");

			# fix &amp; cases
			$x = "";
			while ($y =~ /^([^&]*)&([a-z]*);(.*)$/) {
				if (exists $amp{$2}) {
					$x = $x . $1 . $amp{$2};
				} else {
					$x = $x . $1 . "&" . $2 . ";";
				}
				$y = $3;
			}
			$x = $x . $y;
			$y = $x;
		} else {
			# non-quoted string - grab everything up to the next comma
			# $1 is the stuff before the comma
			# $2 is the command and onward

			$x =~ m/^([^,]*)(.*)/;
			$y = $1;
			$z = $2;
		}

		print "$y\t";

		# kill the comma, if there is one.  Otherwise just print out
		# the remainder of the line.
		if ($z =~ /^,/) {
			$z =~ s/^,//;
		} else {
			print "$z\n";
			$z = "";
		}
		$x = $z;
	}
}
close(file);
