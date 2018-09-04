#!/usr/bin/perl

use strict;
use warnings;

# try to move double quote marks outside of chunks.
# Mainly, however, we want either both quote marks inside the same chunk, or outside of chunks entirely

my $file;

open($file, $ARGV[0]);
while (<$file>) {
	chomp($_);

	# do a quick count of the number of double quotes in the caption
	# we want them to be balanced.  We assume no nesting.
	my @ax = split(/ /, $_);
	my $c = 0;
	foreach (@ax) {
		my @ay = split(/\//, $_);
		if ($ay[0] eq "\"") {
			$c++;
		}
	}

	if ($c > 0 && ($c % 2) == 0) {
		# @az - output caption
		# $s - 0 - outside a pair of double quotes, 1 - inside a pair of double quotes
		# $c - current chunk we're in
		my @az = ();
		my $s = 0;
		my $c = "";

		for (my $i = 0; $i <= $#ax; $i++) {
			my @ay = split(/\//, $ax[$i]);

			push(@az, $ax[$i]);

			# update the current chunk
			if ($ay[0] =~ /^\[(.*)/) {
				$c = $1;
			} elsif ($ay[0] =~ /^\]/) {
				$c = "";
			}

			# check if we've seen a double quotes
			if ($ay[0] eq "\"") {
				# if we're only interested in the opening double quote - we'll handle its closing double quote at the same time
				# so we can ignore closing double quotes
				if ($s == 0) {
					# if we're not inside a chunk...
					if ($c eq "") {

						# $state - bit field of what are the chunks between the opening and closing double quotes
						#   1 - non-chunked token
						#   2 - non-NP or ADJP chunk
						#   4 - NP chunk
						#   8 - ADJP chunk
						# $c - now indicates whether we're inside or outside a chunk
						my $state = 0;
						# process the caption until we encounter the closing double quote
						for (my $j = $i + 1; $j <= $#ax; $j++) {
							@ay = split(/\//, $ax[$j]);
							
							if ($ay[0] =~ /^\[/) {
								$c = "IN";
								if ($ay[0] eq "[NP") {
									$state |= 4;
								} elsif ($ay[0] eq "[ADJP") {
									$state |= 8;
								} else {
									$state |= 2;
								}
							} elsif ($ay[0] =~ /^\]/) {
								$c = "";
							} elsif ($ay[0] eq "\"") {
								last;
							} elsif ($c eq "" && $ay[0] =~ /^[A-Za-z0-9]/) {
								$state |= 1;
							}
						}

						# closing double quote is outside of a chunk
						# and we've seen either NP or ADJP chunks between the two of them, as well as a token outside of the chunks...
						# then we're going to stick everything that starts with an alphanumeric character inside NP chunks
						if ($c eq "" && ($state == 5 || $state == 9)) {
							$i++;
							$state = 0;
							while ($i <= $#ax) {
								@ay = split(/\//, $ax[$i]);
								if ($ay[0] =~ /^[\[\]]/) {
								} elsif ($ay[0] =~ /^[0-9A-Za-z]/) {
									if ($state == 0) {
										push(@az, "[NP");
										$state = 1;
									}
									if ($ay[1] =~ /^[^A-Z]/) {
										$ay[1] = "NNP";
									}
									push(@az, join("/", @ay));
								} else {
									if ($state == 1) {
										push(@az, "]");
										# [NP X 's ] [NP Y ]
										if ($ay[1] eq "POS") {
											push(@az, "[NP");
										} else {
											$state = 0;
										}
									}
									push(@az, join("/", @ay));
									if ($ay[0] eq "\"") {
										last;
									}
								}
								$i++;
							}
							
							next;
						}

						$c = "";
					# if we're inside an NP chunk...
					} elsif ($c eq "NP") {
						# $state - bitfield - holds information about chunks
						#  1 - we've seen a chunk end boundary
						#  2 - we've seen an NP chunk
						# $c - are we in a chunk or not
						my $state = 0;
						my $j;
						
						for ($j = $i + 1; $j <= $#ax; $j++) {
							@ay = split(/\//, $ax[$j]);
							
							if ($ay[0] =~ /^\[/) {
								$c = "IN";
								if ($ay[0] ne "[NP") {
									$state |= 2;
								}
							} elsif ($ay[0] =~ /^\]/) {
								$c = "";
								$state |= 1;
							} elsif ($ay[0] eq "\"") {
								last;
							}
						}

						# if we're outside of the chunk now
						# most common thing is [NP " ... ] " -> " [NP ... ] "
						if ($state == 1 && $c eq "") {
							# is the double quote next to another NP chunk?
							# in that case, we may be seeing something like:
							# [NP a " stop ] " [NP sign ], which is actually one long NP chunk
							# also, we'll retag items in between the double quotes as NNP
							if ($j < $#ax && $ax[$j + 1] eq "[NP") {
								for ($j = $i + 1; $j <= $#ax; $j++) {
									@ay = split(/\//, $ax[$j]);

									if ($ay[0] =~ /^\]/ || $ay[0] =~ /^\[/) {
										next;
									}

									if ($ay[0] =~ /^[0-9A-Za-z]/ && $ay[1] =~ /^[^A-Z]/) {
										$ay[1] = "NNP";
									}

									push(@az, join("/", @ay));

									if ($ay[0] eq "\"") {
										last;
									}
								}
								$i = $j + 1;
								next;
							# otherwise, the most common case is:
							# [NP " ... ] " -> " [NP ... ] "
							} else {
								$state = 0;

								# remove the opening double quote from the output caption
								pop(@az);

								# was it at the beginning of a chunk boundary?  If so, remove that, as well.
								if ($az[$#az] =~ /^\[/) {
									pop(@az);
								# previous token was not a chunk boundary or tagged as a noun
								} elsif ($az[$#az] =~ /\/[^N].*$/) {
									$state = 1;
								# otherwise, close the boundary - we'll be moving the opening double quote outside of the chunk it was in
								} else {
									push(@az, "]");
								}

								# add the double quote back to the output caption
								push(@az, $ax[$i]);

								# make NP chunks out of everything inside the two double quotes
								$i++;
								while ($i <= $#ax) {
									@ay = split(/\//, $ax[$i]);
									if ($ay[0] =~ /^[\[\]]/) {
									} elsif ($ay[0] =~ /^[0-9A-Za-z]/) {
										if ($state == 0) {
											push(@az, "[NP");
											$state = 1;
										}
										if ($ay[1] =~ /^[^A-Z]/) {
											$ay[1] = "NNP";
										}
										push(@az, join("/", @ay));
									} else {
										if ($state == 1) {
											push(@az, "]");
											# [NP X 's ] [NP Y ]
											if ($ay[1] eq "POS") {
												push(@az, "[NP");
											} else {
												$state = 0;
											}
										}
										push(@az, join("/", @ay));
										if ($ay[0] eq "\"") {
											last;
										}
									}
									$i++;
								}
								
								next;
							}
						} else {
							$c = "NP";
						}
					}
				}

				$s = 1 - $s;
			}
		}

		print join(" ", @az), "\n";
	} else {
		print "$_\n";
	}
}
close($file);
