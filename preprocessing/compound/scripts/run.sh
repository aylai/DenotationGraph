#!/bin/bash

# ./run.sh <corpus name> <corpus root dir>

# this'll run all of the fixups in the correct order.  Technically it
# shouldn't matter which order you run the noun and verb fixups (and
# in fact, if it does, then something's probably wrong), but we do
# noun fixups first arbitrarily.

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir -p $2/$1/tmp/compound
tmp_dir=$2/$1/tmp/compound

./fixPunct.pl $2/$1/$1.pretoken > $tmp_dir/punct.txt
./fixNouns.pl $tmp_dir/punct.txt > $tmp_dir/noun.txt
./fixVerbs.pl $tmp_dir/noun.txt > $tmp_dir/nv.txt
./fixAwomen.pl $tmp_dir/nv.txt > $tmp_dir/women.txt
./fixSplit.pl $tmp_dir/women.txt > $tmp_dir/split.txt
./fixTshirt.pl $tmp_dir/split.txt > $tmp_dir/tshirt.txt
./addHyphen.pl $tmp_dir/tshirt.txt > $tmp_dir/hyphen.txt
./fixInFromOf.pl $tmp_dir/hyphen.txt > $tmp_dir/fromof.txt
./dropHyphen.pl $tmp_dir/fromof.txt > $tmp_dir/drophyph.txt
./replaceBrackets.pl $tmp_dir/drophyph.txt > $tmp_dir/brackets.txt
./stripSlash.pl $tmp_dir/brackets.txt > $tmp_dir/slash.txt
./fixWhitespace.pl $tmp_dir/slash.txt > $tmp_dir/whitespace.txt
./checkHash.pl $tmp_dir/whitespace.txt
cp $tmp_dir/whitespace.txt $2/$1/$1.token

popd > /dev/null
