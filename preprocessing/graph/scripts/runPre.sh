#!/bin/bash

# ./runPre.sh <corpus name>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir ../tmp/$1
mkdir ../../corpora/$1/graph
./assignWordIDs.pl ../../corpora/$1/$1.coref > ../tmp/$1/pre.id
./addIDs.pl ../tmp/$1/pre.id > ../tmp/$1/pre.add
./lemmaNouns.pl ../tmp/$1/pre.add ../../corpora/$1/$1.np ../../corpora/$1/$1.ent-mod ../data/ent-mod.txt 1> ../tmp/$1/pre.nlm 2> ../tmp/$1/pre.nlm.log
./lemmaVerbs.pl ../tmp/$1/pre.nlm > ../tmp/$1/pre.vlm
./lowerCase.pl ../tmp/$1/pre.vlm > ../tmp/$1/pre.lc
./dropPunct.pl ../tmp/$1/pre.lc > ../tmp/$1/pre.punct
./lemmaTeens.pl ../tmp/$1/pre.punct > ../tmp/$1/pre.teen
./typeGirl.pl ../tmp/$1/pre.teen > ../tmp/$1/pre.girl
./dropThereBe.pl ../tmp/$1/pre.girl > ../tmp/$1/pre.there
./lemmaWear.pl ../tmp/$1/pre.there > ../tmp/$1/pre.wear
cp ../tmp/$1/pre.wear ../tmp/$1/pre.final
./tokenize.pl ../tmp/$1/pre.final > ../tmp/$1/pre.token
./dropEventMods.pl ../tmp/$1/pre.final > ../tmp/$1/trans01.txt
./dropEntityMods.pl ../tmp/$1/trans01.txt ../data/entmod.txt > ../tmp/$1/trans02.txt
./dropEntityArticle.pl ../tmp/$1/trans02.txt > ../tmp/$1/trans03.txt
./liftEntity.pl ../tmp/$1/trans03.txt ../../corpora/$1/$1.lexicon ../data/lexicon.txt ../../corpora/$1/$1.subj > ../tmp/$1/trans04.txt
./splitXofY.pl ../tmp/$1/trans04.txt > ../tmp/$1/trans05.txt
./splitXorY.pl ../tmp/$1/trans05.txt > ../tmp/$1/trans06.txt
./dropPPs.pl ../tmp/$1/trans06.txt ../../corpora/$1/$1.subj > ../tmp/$1/trans07.txt
./dropWearDress.pl ../tmp/$1/trans07.txt > ../tmp/$1/trans08.txt
#./dropThereIs.pl ../tmp/$1/trans08.txt > ../tmp/$1/trans09.txt
./dropTail.pl ../tmp/$1/trans08.txt > ../tmp/$1/trans10.txt
./splitSubjVerb.pl ../tmp/$1/trans10.txt ../../corpora/$1/$1 1> ../tmp/$1/trans11.txt 2> ../tmp/$1/trans11.err
./emptyBrackets.pl ../tmp/$1/pre.final ../tmp/$1/trans*.txt > ../tmp/$1/empty.txt
cp ../tmp/$1/trans06.txt ../tmp/$1/trans.np
cp ../tmp/$1/trans11.txt ../tmp/$1/trans.final
cp ../tmp/$1/trans11.txt ../../corpora/$1/graph/initial.rewrite
cp ../tmp/$1/pre.id ../../corpora/$1/graph/initial.coref
cp ../tmp/$1/pre.token ../../corpora/$1/graph/token.txt
awk -F'\t' '{print $1}' ../../corpora/$1/$1.coref | awk -F'#' '{print $1}' | sort -u > ../../corpora/$1/graph/img.lst

popd > /dev/null
