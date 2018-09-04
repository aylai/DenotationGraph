#!/usr/bin/perl

# usage ./splitCompoundVerbs.pl <pre-split POS> <post-chunked POS>

# grab captions with unhyphenated compound verbs
$n = 0;
@sent = ();
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	$sent[$n] = $_;
	$n++;
}
close(file);

$n = 0;
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);

	# @b is the unhyphenated version of the caption
	# @c is the hyphentated and chunked version of the caption
	# $i is our position in @c
	# $j is our position in @b
	@b = split(/ /, $sent[$n]);
	@c = split(/ /, $_);

	$j = 0;
	for ($i = 0; $i <= $#c; $i++) {
		if ($i > 0) {
			print " ";
		}

		@d = split(/\//, $c[$i]);
		if ($#d == 1) {
			# if they're the same token, advance both pointers
			if ($b[$j] eq $c[$i]) {
				$j++;
			} else {
				# check if it's the current token in the unhyphenated
				# caption and the next token can be compounded to form
				# the current token in the hyphenated/chunked caption
				# if it can, unhyphenate and retag.  Otherwise that's
				# a problem.				
				@e = split(/\//, $b[$j]);
				@f = split(/\//, $b[$j + 1]);
				$w = $e[0] . "-" . $f[0] . "/" . $f[1];
				if ($w eq $c[$i]) {
					print "$b[$j] $b[$j + 1]";
					$j = $j + 2;
					next;
				} else {
					print "FAIL\n";
					exit;
				}
			}
		}
		print "$c[$i]";
	}
	print "\n";

	$n++;
}
close(file);
