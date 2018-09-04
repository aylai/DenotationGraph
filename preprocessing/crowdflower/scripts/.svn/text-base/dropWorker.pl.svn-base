#!/usr/bin/perl

# takes a list of TSV files and produces one after dropping workers in
# .drop files

$first = 1;
for ($i = 0; $i <= $#ARGV; $i++) {
	%filter = ();
	$f = $ARGV[$i];
	$f =~ s/\.tsv$//;

	# get the workers to filter out for this TSV file
	open(file, "$f.drop");
	while (<file>) {
		chomp($_);
		$filter{$_} = 1;
	}
	close(file);

	open(file, $ARGV[$i]);
	$_ = <file>;
	# print headers
	if ($first == 1) {
		print "$_";
		$first = 0;
	}
	chomp($_);
	@h = split(/\t/, $_);
	%header = ();
	for ($j = 0; $j <= $#h; $j++) {
		$header{$h[$j]} = $j;
	}
	while (<file>) {
		chomp($_);
		@a = split(/\t/, $_);
		# ignore workers in %filter
		if (!exists $filter{$a[$header{"_worker_id"}]}) {
			# check if "no photo", "no image", "no picture" "image is
			# broken", or "link is broken" appear
			$d = lc($a[$header{"please_describe_the_image_in_one_complete_sentence"}]);
			if ($d =~ /no photo/ || $d =~ /no image/ || $d =~ /no picture/ || $d =~ /image is broken/ || $d =~ /link is broken/) {
			} else {
				print "$_\n";
			}
		}
	}
	close(file);
}
