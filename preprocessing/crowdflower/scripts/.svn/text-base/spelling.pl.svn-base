#!/usr/bin/perl

# ./spelling.pl <txt> <dict> [final]

# read the dictionary
# %dict is the corrections
# %fix is the corrected terms, so we don't count them as mispelled
%dict = ();
%fix = ();
open(file, $ARGV[1]);
while (<file>) {
	chomp($_);
	@a = split(/\t/, $_);
	$dict{$a[1]} = $a[0];
	$fix{$a[0]} = 1;
}
close(file);

open(file, $ARGV[0]);
@lines = <file>;
close(file);

chomp(@lines);

# replace words in dictionary
for ($i = 0; $i <= $#lines; $i++) {
	@a = split(/\t/, $lines[$i]);
	@b = split(/ /, $a[1]);
	for ($j = 0; $j <= $#b; $j++) {
		if (exists $dict{$b[$j]}) {
			$b[$j] = $dict{$b[$j]};
		}
	}
	$a[1] = join(' ', @b);
	$lines[$i] = join("\t", @a);
}

# get word counts (global and by sentence) - will be used to guess the
# best term.
%word = ();
$sent = {};
for ($i = 0; $i <= $#lines; $i++) {
	@a = split(/\t/, $lines[$i]);
	if (not exists $sent->{$a[0]}) {
		$sent->{$a[0]} = {};
	}

	@b = split(/ /, $a[1]);
	for ($j = 0; $j <= $#b; $j++) {
		$w = $b[$j];
		$w =~ s/^[^a-zA-Z]*//;
		$w =~ s/[^a-zA-Z]*$//;
		if ($w ne "") {
			$w =~ tr/[A-Z]/[a-z]/;
			if (not exists $word{$w}) {
				$word{$w} = 0;
			}
			$word{$w} = $word{$w} + 1;
			if (not exists $sent->{$a[0]}->{$w}) {
				$sent->{$a[0]}->{$w} = 0;
			}
			$sent->{$a[0]}->{$w} = $sent->{$a[0]}->{$w} + 1;
		}
	}
}

for ($i = 0; $i <= $#lines; $i++) {
	@a = split(/\t/, $lines[$i]);
	@b = split(/ /, $a[1]);
	for ($j = 0; $j <= $#b; $j++) {
		# ignore if corrected term in dictionary
		if (not exists $fix{$b[$j]}) {
			$w = $b[$j];
			$w =~ s/^([^a-zA-Z]*)//;
			$x = $1;
			$w =~ s/([^a-zA-Z]*)$//;
			$y = $1;
			$w =~ s/\'/\\\'/g;
			$w =~ s/\`/\\\'/g;
			$w =~ s/\"/\\\"/g;
			$w =~ s/\(/\\\(/g;
			$w =~ s/\)/\\\)/g;
			$w =~ s/\&/\\\&/g;
			$w =~ s/\;/\\\;/g;
			$w =~ tr/[a-z]/[A-Z]/;

			if ($w ne "") {
				# check if misspelled
				@asp = `echo $w | aspell -a`;
				if ($asp[1] ne "*\n") {
					$corr = $asp[1];
					$corr =~ s/^.*: //;
					chomp($corr);
					# get list of candidate corrections
					@c = split(/, /, $corr);
					for ($k = 0; $k <= $#c; $k++) {
						$c[$k] =~ tr/[A-Z]/[a-z]/;
					}

					# pick the correction most used in the sentence
					$best = "";
					$score = 0;
					for ($k = 0; $k <= $#c; $k++) {
						if (exists $sent->{$a[0]}->{$c[$k]}) {
							if ($score < $sent->{$a[0]}->{$c[$k]}) {
								$score = $sent->{$a[0]}->{$c[$k]};
								$best = $c[$k];
							}
						}
					}

					# otherwise pick the correction most used globally
					if ($score == 0) {
						for ($k = 0; $k <= $#c; $k++) {
							if (exists $word{$c[$k]}) {
								if ($score < $word{$c[$k]}) {
									$score = $word{$c[$k]};
									$best = $c[$k];
								}
							}
						}
					}

					if ($best ne "") {
						# print (if needed) and replace
						if ($ARGV[2] eq "") {
							print "$x$best$y\t$b[$j]\t$a[1]\n";
						}
						$b[$j] = "$x$best$y";
					} else {
						# print (if needed) and ignore
						if ($ARGV[2] eq "") {
							print "###\t$b[$j]\t$a[1]\n";
						}
#						$b[$j] = "###$b[$j]###";
					}
				} else {
					$dict{$b[$j]} = 1;
				}
			}
		}
	}
	$s = join(' ', @b);
	# print string if we're doing the final run.
	if ($ARGV[2] ne "") {
		print "$a[0]\t$s\n";
	}
}
