#!/usr/bin/perl

use FindBin;
use lib "$FindBin::Bin";
use simple;

my @adir = split(/\//, $0);
pop(@adir);
my $sdir = join("/", @adir);

# load entity head nouns that can be grouped together, like "man and woman", etc.
$group = ();
$x = 0;
open(file, "$sdir/../data/ccGroup.txt");
while (<file>) {
	chomp($_);
	if ($_ eq "") {
		$x++;
	} else {
		$group{$_} = $x;
	}
}
close(file);

$| = 1;

@dep = ();
@X = ();
@Y = ();
@type = ();
$n = 0;
open(file, $ARGV[0]);
while (<file>) {
	chomp($_);
	@ax = split(/\t/, $_);
	if ($#ax == 1) {
		$ax[2] = "";
	}
	# if this is a caption, we want to drop the "wear X" and "dressed PP X" cases
	if ($#ax == 2) {
		@ay = split(/ /, $ax[2]);
		($next, $prev) = breakSlash(\@ay, 1);
		$inext = 0;
		# go through the caption looking for the VP chunks "dressed" or "wear"
		for ($i = 0; $i <= $#ay; $i = $inext) {
			$inext = $i + $next->[$i];
			if ($ay[$i]->[1] eq "[VP" && $next->[$i] == 3 && ($i + 3) <= $#ay) {
				if (lc($ay[$i + 1]->[1]) eq "dressed") {
					$type = "DRESS";
					# if the next thing is an EN chunk, grab the entity head noun
					if ($ay[$i + 3]->[1] eq "[EN") {
						$j = $i + 3;
						$k = $j + $next->[$j] - 1;
						$en1 = entityString(\@ay, $next, $j);
					# otherwise, check if we're dealing with "dressed in/as/for"
					# and the next thing after the PP chunk is an EN chunk,
					# in which case, grab the entity head noun
					} elsif (($i + 6) <= $#ay && $ay[$i + 3]->[1] eq "[PP" && 
							 (lc($ay[$i + 4]->[1]) eq "in" || lc($ay[$i + 4]->[1]) eq "as" || lc($ay[$i + 4]->[1]) eq "for") &&
							 $ay[$i + 6]->[1] eq "[EN") {
						$j = $i + 6;
						$k = $j + $next->[$j] - 1;
						$en1 = entityString(\@ay, $next, $j);
					} else {
						next;
					}
				# VP chunk "wear" + the next thing is an EN chunk, whose head noun we grab
				} elsif (lc($ay[$i + 1]->[1]) eq "wear" && $ay[$i + 3]->[1] eq "[EN") {
					$type = "WEAR";
					$j = $i + 3;
					$k = $j + $next->[$j] - 1;
					$en1 = entityString(\@ay, $next, $j);
				} else {
					next;
				}

				# check if the direct object is actually an "X CC Y" case
				# must be separated by a CC, and the head nouns must be groupable
				# same group type, according to ../data/ccGroup.txt
				if (($k + 2) <= $#ay && $ay[$k + 1]->[2] eq "CC" && $ay[$k + 2]->[1] eq "[EN") {
					$en2 = entityString(\@ay, $next, $k + 2);
					if (exists $group{$en1} && exists $group{$en2} && $group{$en1} == $group{$en2}) {
						$k = ($k + 2) + $next->[$k + 2] - 1;
					}
				}

				if ($i > 0 || $k < $#ay) {
					# @aX - left side of rule
					# @aY - right side of rule
					@aX = ();
					@aY = ();
					push(@aX, ($i - 1 >= 0) ? $ay[$i - 1]->[0] : "B");
					push(@aY, ($i - 1 >= 0) ? $ay[$i - 1]->[0] : "B");
					foreach (@ay[$i .. $k]) {
						push(@aY, join("/", @{$_}));
					}
					push(@aX, ($k < $#ay) ? $ay[$k + 1]->[0] : "E");
					push(@aY, ($k < $#ay) ? $ay[$k + 1]->[0] : "E");
					@as = ();
					for (@ay[$j .. $k]) {
						if (not $_->[1] =~ /^[\[\]]/) {
							push(@as, $_->[1]);
						}
					}
					
					if ($aX[0] ne "B" && $aX[1] eq "E") {
						for ($l = 0; $l <= $#ay; $l++) {
							if ($ay[$l]->[0] eq $aX[0]) {
								if (exists $ay[$l]->[2] && $ay[$l]->[2] eq "CC") {
									shift(@aX);
									unshift(@aX, ($l > 0) ? $ay[$l - 1]->[0] : "B");

									shift(@aY);
									unshift(@aY, join("/", @{$ay[$l]}));
									unshift(@aY, ($l > 0) ? $ay[$l - 1]->[0] : "B");
									$i--;
								}
								last;
							}
						}
					}

					addTransformation("", join(" ", @aX), join(" ", @aY), "+" . $type . "/" . join(" ", @as), \@dep, \@X, \@Y, \@type, \$n);
					
#					for ($z = $k; $z <= $#ay; $z++) {
#						if ($ay[$z]->[1] =~ /^[\[\]]/) {
#						} elsif ($ay[$z]->[1] eq "that" || $ay[$z]->[1] eq "which" || $ay[$z]->[1] eq "who" && $ay[$z]->[1] eq "whose") {
#							last;
#						} else {
#							$z = $#ay + 1;
#							last;
#						}
#					}
					
					@ay = @ay[0 .. $i - 1, $k + 1 .. $#ay];
					($next, $prev) = getNextPrev(\@ay, 1);
					$inext = $i;
					next;
				}
			}
		}
		printSentence($ax[0], $ax[1], \@ay, \@dep, \@X, \@Y, \@type, $n);

		@dep = ();
		@X = ();
		@Y = ();
		@type = ();
		$n = 0;
	# save the rule if it has the correct index ($n)
	} elsif ($#ax == 4) {
		if ($ax[0] == $n) {
			$dep[$ax[0]] = $ax[1];
			$X[$ax[0]] = $ax[2];
			$Y[$ax[0]] = $ax[3];
			$type[$ax[0]] = $ax[4];
			$n++;
		}
	}
}

close(file);
