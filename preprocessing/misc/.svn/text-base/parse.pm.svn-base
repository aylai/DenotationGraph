#!/usr/bin/perl

package parse;

use Exporter;

@ISA = ("Exporter");
@EXPORT = ("parse", "unparse", "flatten", "breakSlash", "getNextPrev");

sub parse($$$) {
	local ($arr, $i, $p, $r, $s, $t, $x, $n, $l, @a);
	
	$arr = $_[0];
	$i = $_[1];
	$p = $_[2];
	
	$r = ();
	$r->[0] = "";
	$r->[1] = "";
	# starting index
	$r->[2] = $p;

	# are we dealing with a chunk?
	if ($arr->[$i] =~ /^\[(.*)$/) {
		#if so, parse the internals until we hit the end of the chunk
		$l = $1;
		$n = 0;
		$t = ();
		$i++;
		while ($arr->[$i] ne "]") {
			($x, $i, $p) = parse($arr, $i, $p);
			$t->[$n] = $x;
			$n++;
		}
		$i++;

		# if there's only a single token/internal, just make its internals your internals
		if ($n == 1 && $t->[0]->[0] eq "") {
			$r->[0] = $l;
			$r->[1] = $t->[0]->[1];
		# otherwise store the internals
		} else {
			$r->[0] = $l;
			$r->[1] = $t;
		}
	} else {
		# grab tokens until we hit a chunk boundary
		$s = "";
		while ($i <= $#$arr && not $arr->[$i] =~ /^[\[\]]/) {
			if ($s ne "") {
				$s = $s . " ";
			}
			$s = $s . $arr->[$i];

			$i++;
			$p++;
		}
		
		$r->[0] = "";
		$r->[1] = $s;
	}

	# ending index
	$r->[3] = $p - 1;
	
	return ($r, $i, $p);
}

sub unparse($) {
	local ($x, $r, $i);

	$x = $_[0];

	# check if we're a chunk - if so, add chunk boundaries
	if ($x->[0] ne "") {
		$r = "[" . $x->[0];
	}

	# if our internals is an array...
	if (ref($x->[1]) eq "ARRAY") {
		# use unparse to unparse the internals
		for ($i = 0; $i <= $#{$x->[1]}; $i++) {
			if ($r ne "") {
				$r = $r . " ";
			}
			$r = $r . unparse($x->[1]->[$i]);
		}
	# otherwise, just add the string
	} else {
		if ($r ne "") {
			$r = $r . " ";
		}
		$r = $r . $x->[1];
	}

	# if this was a chunk, add close chunk boundary
	if ($x->[0] ne "") {
		$r = $r . " ]";
	}
	return $r;
}

sub flatten($) {
	local ($x, $r, $i);

	$x = $_[0];
	$r = "";

	# ignore chunk boundaries

	# check if our internals are an array
	if (ref($x) eq "ARRAY") {
		# use flatten to return the internal string
		for ($i = 0; $i <= $#$x; $i++) {
			if ($i > 0) {
				$r = $r . " ";
			}
			$r = $r . flatten($x->[$i]->[1]);
		}
		return $r;
	# otherwise just return the string that is our internals
	} else {
		return $x;
	}
}

# take a reference to an array of tokens, and split each token at the '/'s
# also, return the next/prev pointers.
sub breakSlash($$) {
	my $ax = $_[0];
	my $t = $_[1];
	my $i;
	my $x = scalar @{$ax};

	for ($i = 0; $i < $x; $i++) {
		my @ay = split(/\//, $ax->[$i]);
		$ax->[$i] = \@ay;
	}

	return getNextPrev($ax, $t);
}

# get the next/prev pointers for a reference to an array of slash broken tokens
sub getNextPrev($$) {
	my $ax = $_[0];
	my $t = $_[1];
	my $i, $j;
	my $depth;
	my $x = scalar @{$ax};
	my @next = ();
	my @prev = ();

	for ($i = 0; $i < $x; $i++) {
		# move forward through the rest of the string until you hit the same depth
		# this means that you're at the "next" token.
		$depth = 0;
		for ($j = $i; $j < $x; $j++) {
			if ($ax->[$j]->[$t] =~ /^\[/) {
				$depth++;
			} elsif ($ax->[$j]->[$t] =~ /^\]/) {
				$depth--;
			}
			if ($depth <= 0) {
				last;
			}
		}
		# if you've got a negative depth, pull back the next pointer
		# (i.e., it typically means you were an end of chunk boundary
		# in which case we consider your next token to be yourself)
		$next[$i] = ($j - $i + 1) + $depth;

		# opposite of the above - move backwards (which reverses how the
		# depth changes), until you get back to depth 0 (or less)
		$depth = 0;
		for ($j = $i; $j >= 0; $j--) {
			if ($ax->[$j]->[$t] =~ /^\[/) {
				$depth--;
			} elsif ($ax->[$j]->[$t] =~ /^\]/) {
				$depth++;
			}
			if ($depth <= 0) {
				last;
			}
		}
		# again, if you have negative depth, you're a beginning of
		# chunk boundary, and the previous token is yourself.
		$prev[$i] = ($i - $j + 1) + $depth;
	}

	return (\@next, \@prev);
}
