#!/bin/bash

# runLimited.sh <corpus name> <url list>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir ../tmp/$1-$2
../../misc/gimme.pl ../../corpora/$1/$1.coref ../../corpora/$1/$2 > ../tmp/$1-$2/coref.txt
./tokenize.pl ../tmp/$1-$2/coref.txt > ../tmp/$1-$2/token.txt
awk -F'\t' '{print $2}' ../tmp/$1-$2/token.txt > ../tmp/$1-$2/sent.txt
./conllize.pl ../tmp/$1-$2/coref.txt > ../tmp/$1-$2/conll.txt
./runlin.sh ../tmp/$1-$2/conll.txt ../tmp/$1-$2/malt-lin.out
./conllMalt.pl ../tmp/$1-$2/coref.txt ../tmp/$1-$2/malt-lin.out > ../tmp/$1-$2/malt-lin.conll
./renumberConll.pl ../tmp/$1-$2/coref.txt ../tmp/$1-$2/malt-lin.conll > ../tmp/$1-$2/malt-lin2.conll
./runpoly.sh ../tmp/$1-$2/conll.txt ../tmp/$1-$2/malt-poly.out
./conllMalt.pl ../tmp/$1-$2/coref.txt ../tmp/$1-$2/malt-poly.out > ../tmp/$1-$2/malt-poly.conll
./renumberConll.pl ../tmp/$1-$2/coref.txt ../tmp/$1-$2/malt-poly.conll > ../tmp/$1-$2/malt-poly2.conll
./runStanford.sh ../tmp/$1-$2/sent.txt ../tmp/$1-$2/stanford.out
./conllStanford.pl ../tmp/$1-$2/token.txt ../tmp/$1-$2/stanford.out > ../tmp/$1-$2/stanford.conll
./renumberConll.pl ../tmp/$1-$2/coref.txt ../tmp/$1-$2/stanford.conll > ../tmp/$1-$2/stanford2.conll
cp ../../corpora/$1/$1-malt-lin.conll ../tmp/$1-$2/orig-malt-lin.conll
cp ../../corpora/$1/$1-malt-poly.conll ../tmp/$1-$2/orig-malt-poly.conll
cp ../../corpora/$1/$1-stanford.conll ../tmp/$1-$2/orig-stanford.conll
./spliceConll.pl ../tmp/$1-$2/orig-malt-lin.conll ../tmp/$1-$2/malt-lin2.conll > ../../corpora/$1/$1-malt-lin.conll
./spliceConll.pl ../tmp/$1-$2/orig-malt-poly.conll ../tmp/$1-$2/malt-poly2.conll > ../../corpora/$1/$1-malt-poly.conll
./spliceConll.pl ../tmp/$1-$2/orig-stanford.conll ../tmp/$1-$2/stanford2.conll > ../../corpora/$1/$1-stanford.conll

popd > /dev/null
