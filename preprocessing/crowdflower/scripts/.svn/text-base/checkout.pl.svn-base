#!/usr/bin/perl

use Time::Local;

%header = ();
$worker = ();

open(file, $ARGV[0]);

# grab the headers, and map them to their column #
$_ = <file>;
chomp($_);
@cols = split(/\t/, $_);
for ($i = 0; $i <= $#cols; $i++) {
	$header{$cols[$i]} = $i;
}

# store words that we've spell checked, so we're not constantly
# running aspell
%dict = ();
while (<file>) {
	chomp($_);
	@cols = split(/\t/, $_);
	$i = $cols[$header{"image"}];
	$s = $cols[$header{"please_describe_the_image_in_one_complete_sentence"}];
	# remove extra spaces in the caption
	$s =~ s/ [ ]*/ /g;
	$w = $cols[$header{"_worker_id"}];
	# image ID, caption, worker ID
	print "$i\t$s\t$w";

	# use timelocal to convert the dates into a timestamp, and
	# calculate the number of seconds taken.
	$date = $cols[$header{"_created_at"}];
	$date =~ /(\d+)\/(\d+)\/(\d+) (\d+):(\d+):(\d+)/;
	$end = timelocal($6, $5, $4, $2, $1 - 1, $3);
	$date = $cols[$header{"_started_at"}];
	$date =~ /(\d+)\/(\d+)\/(\d+) (\d+):(\d+):(\d+)/;
	$start = timelocal($6, $5, $4, $2, $1 - 1, $3);
	$delta = $end - $start;

	# length of word
	$l = length($s);
	
	# number of words
	@y = split(/ /, $s);
	$wc = scalar @y;

	print "\t$delta\t$l\t$wc\t";

	# spell check - print misspelled words
	$printed = 0;
	for ($j = 0; $j < $wc; $j++) {
		if (!exists $dict{$y[$j]}) {
			if ($y[$j] =~ /[A-Za-z]/) {
				$word = $y[$j];
				$word =~ s/\'/\\\'/g;
				$word =~ s/\`/\\\'/g;
				$word =~ s/\"/\\\"/g;
				$word =~ s/\(/\\\(/g;
				$word =~ s/\)/\\\)/g;
				$word =~ s/\&/\\\&/g;
				$word =~ s/\;/\\\;/g;
				$word =~ tr/[a-z]/[A-Z]/;
				@asp = `echo $word | aspell -a`;
				if ($asp[1] ne "*\n") {
					$dict{$y[$j]} = 0;
				} else {
					$dict{$y[$j]} = 1;
				}
			} else {
				$dict{$y[$j]} = 1;
			}
		}
		if ($dict{$y[$j]} == 0) {
			if ($printed == 1) {
				print " ";
			}
			print "$y[$j]";
			$printed = 1;
		}
	}

	print "\n";
}
close(file);
