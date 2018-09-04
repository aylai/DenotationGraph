#!/usr/bin/perl

package util;

use WordNet::QueryData;
use Exporter;

@ISA = ("Exporter");
@EXPORT = ("getHypes", "tokenize", "nlemmaAdd", "nlemmaValid", "nlemma", "vlemma");

sub getHypes($) {
	my (@hypes);
	@hypes = $wn->querySense($_[0], "hype");

	if ($_[0] eq "kitten#n#1") {
		$hypes[$#hypes + 1] = "cat#n#1";
	} elsif ($_[0] eq "homo#n#2" || $_[0] eq "operator#n#2") {
		$hypes[$#hypes + 1] = "person#n#1";
	} elsif ($_[0] eq "white_water#n#1") {
		$hypes[$#hypes + 1] = "rapid#n#1";
	}

	return @hypes;
}

sub tokenize($) {
	my ($s, @ax);

	$s = "";
	foreach (split(/ /, $_[0])) {
		if ($s ne "") {
			$s = $s . " ";
		}
		@ax = split(/\//, $_);
		$s = $s . lc($ax[0]);
	}

	return $s;
}

my (%noun, %nouns);

%noun = ();
%nouns = ();

sub nlemmaAdd($) {
	$nouns{$_[0]} = 1;
}

sub nlemmaValid($) {
	if (exists $nouns{$_[0]}) {
		return 1;
	}
	return 0;
}

sub nlemma($) {
	my (@ax, @forms, $w, $f, $best);

	$w = $_[0];
	if ($w =~ /^[_\s]*$/) {
		$noun{$w} = $w;
	}
	if (not exists $noun{$w}) {
		# hack - WordNet considers these their own terms
		if (lc($w) eq "people" || lc($w) eq "peoples") {
			$noun{$w} = "person";
		} else {
			# find the smallest form, do not mess with "-glasses" or "woods"
			# since, "sunglasses" are "sunglasses" and not "sunglass"
			# and "woods" are not the same as "wood".
			@forms = $wn->validForms($w . "#n");
			$best = $w;
			if ($#forms >= 0 && (not $w =~ /glasses$/) && $w ne "woods") {
				foreach $f (@forms) {
					$f =~ s/_/ /g;
					@ax = split(/\#/, $f);

					if ($ax[1] eq "n" && exists $nouns{$ax[0]} &&
					    (length($best) > length($ax[0]) || 
						 ($ax[0] =~ /man$/ && lc($best) =~ /men$/) ||
						 ($ax[0] =~ /person$/ && lc($best) =~ /people$/))) {
						$best = $ax[0];
					}
				}
			# if we can't find anything, try chopping of "-es" or "-s"
			} elsif ($#forms == -1) {
				if ($w =~ /es$/) {
					my $x = $w;
					$x =~ s/es$//;
					if (exists $nouns{$x}) {
						$best = $x;
					}
				} elsif ($w =~ /s$/) {
					my $x = $w;
					$x =~ s/s$//;
					if (exists $nouns{$x}) {
						$best = $x;
					}
				}
			}

			# if there was only one valid form, try chopping off "-s"
			# or replacing "-men" with "-man".
			if ($w eq $best && $#forms == 0) {
				@ax = split(/\#/, $forms[0]);
				$x = $w;
				$x =~ s/s$//;
				if ($ax[0] eq $x) {
					$best = $ax[0];
				}
				$x = $w;
				$x =~ s/men$/man/;
				if ($ax[0] eq $x) {
					$best = $ax[0];
				}
			}
			$noun{$w} = $best;
		}
	}
	return $noun{$w};
}

my (%verb, %oververb);

%verb = ();
%oververb = ();
$oververb{"swinge"} = "swing";
$oververb{"singe"} = "sing";

sub vlemma($) {
	local (@ax, @forms, $f, $w, $best);

	$w = $_[0];
	if ($w =~ /^[_\s]*$/) {
		$verb{$w} = $w;
	}
	if (not exists $verb{$w}) {
		$verb{$w} = $w;
		@forms = $wn->validForms($w . "#v");
		if ($#forms >= 0) {
			$best = "";
			foreach $f (@forms) {
				@ax = split(/\#/, $f);

				# if there's an override for a form, use that
				if (exists $oververb{$best} && $ax[0] eq $oververb{$best}) {
					$best = $ax[0];
				} elsif (exists $oververb{$ax[0]} && $best eq $oververb{$ax[0]}) {
				# otherwise, choose the longest form
				} elsif ($ax[1] eq "v" && length($best) < length($ax[0])) {
					$best = $ax[0];
				}
			}

			if ($best ne "") {
				$verb{$w} = $best;
			}
		}
	}
	$verb{$w};
}

$wn = WordNet::QueryData->new;
