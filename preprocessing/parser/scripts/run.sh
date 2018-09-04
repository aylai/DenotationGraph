#!/bin/bash

# run.sh <corpus name> <# threads> <corpus root dir>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir -p $3/$1/tmp/parser
tmp_dir=$3/$1/tmp/parser

./tokenize.pl $3/$1/$1.coref > $tmp_dir/token.txt
awk -F'\t' '{print $2}' $tmp_dir/token.txt > $tmp_dir/sent.txt
./conllize.pl $3/$1/$1.coref > $tmp_dir/conll.txt
./runlin.sh $tmp_dir/conll.txt $tmp_dir/malt-lin.out 2> $tmp_dir/malt-lin.log
./conllMalt.pl $3/$1/$1.coref $tmp_dir/malt-lin.out > $tmp_dir/malt-lin.conll
./renumberConll.pl $3/$1/$1.coref $tmp_dir/malt-lin.conll > $3/$1/$1-malt-lin.conll
./runpoly.pl $tmp_dir/conll.txt $tmp_dir/malt-poly.out $2
./conllMalt.pl $3/$1/$1.coref $tmp_dir/malt-poly.out > $tmp_dir/malt-poly.conll
./renumberConll.pl $3/$1/$1.coref $tmp_dir/malt-poly.conll > $3/$1/$1-malt-poly.conll
./runStanford.pl $tmp_dir/sent.txt $tmp_dir/stanford.out $2
./conllStanford.pl $tmp_dir/token.txt $tmp_dir/stanford.out > $tmp_dir/stanford.conll
./renumberConll.pl $3/$1/$1.coref $tmp_dir/stanford.conll > $3/$1/$1-stanford.conll

popd > /dev/null
