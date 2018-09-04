#!/usr/bin/perl

use strict;
use warnings;

# break up NPs of the form [NP <x> wearing ... ] into [NP <x> ] [VP wearing ] [NP ... ]
# (if <x> is "'s", then just make a [VP 's wearing ] chunk instead)

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);
	my @ax = split(/ /, $_);
	my @ay = ();
LOOP:
	for (my $i = 0; $i <= $#ax; $i++) {
		# find the NP chunks
		if ($ax[$i] eq "[NP") {
			# process the NP chunks - we're going to look for "wearing" in the middle of one, and break up the NP chunk as appropriate
			for (my $j = $i + 1; $j <= $#ax; $j++) {
				# end of chunk, done
				if ($ax[$j] eq "]") {
					last;
				# otherwise, make sure we're not at the last token in the caption
				# (which we shouldn't be - there should always be a "]" token somewhere ahead of us)
				} elsif ($j > ($i + 1)) {
					my @aw = ();
					my @az = split(/\//, $ax[$j]);
					# if this token is "wearing"
					if (lc($az[0]) eq "wearing") {
						# if the first two tokens are "'s wearing", then the VP is "'s wearing"
						if ($j == ($i + 2)) {
							@az = split(/\//, $ax[$j - 1]);
							if (lc($az[0]) eq "'s") {
								# form the VP chunk
								push(@aw, "[VP");
								push(@aw, "$az[0]/VBZ");
								push(@aw, $ax[$j]);
								push(@aw, "]");
								# if there's still more to the NP, that forms a new NP chunk
								if ($ax[$j + 1] ne "]") {
									push(@aw, "[NP");
									do {
										$j++;
										push(@aw, $ax[$j]);
									} while ($ax[$j] ne "]" && $j < $#ax);
								} else {
									$j++;
								}
								print STDERR join(" ", @aw), "\n";
								@ay = (@ay, @aw);
								$i = $j;
								next LOOP;
							}
						}

						# create a new NP chunk out of everything up to "wearing"
						while ($i < $j) {
							push(@aw, $ax[$i]);
							$i++;
						}
						push(@aw, "]");

						# create a new VP chunk out of "wearing"
						push(@aw, "[VP");
						push(@aw, $ax[$j]);
						push(@aw, "]");

						# if there's more left of the original NP chunk, make a new NP chunk out of it
						if ($ax[$j + 1] ne "]") {
							push(@aw, "[NP");
							do {
								$j++;
								push(@aw, $ax[$j]);
							} while ($ax[$j] ne "]" && $j < $#ax);
						} else {
							$j++;
						}
						print STDERR join(" ", @aw), "\n";
						@ay = (@ay, @aw);
						$i = $j;
						next LOOP;
					}
				}
			}
		}
		push(@ay, $ax[$i]);
	}
	print join(" ", @ay), "\n";
}
close($file);
