#!/bin/bash

# ./runPre.sh <corpus name> <corpus root dir>
BASEDIR=$(dirname $0)
rootdir=`pwd`
pushd $BASEDIR > /dev/null
graphdir=`pwd`

mkdir -p $2/$1/graph
mkdir -p $2/$1/tmp/graph
tmp_dir=$2/$1/tmp/graph

./assignWordIDs.pl $2/$1/$1.coref > $tmp_dir/pre.id
./addIDs.pl $tmp_dir/pre.id > $tmp_dir/pre.add
./lemmaNouns.pl $tmp_dir/pre.add $2/$1/$1.np $2/$1/$1.ent-mod ../data/ent-mod.txt 1> $tmp_dir/pre.nlm 2> $tmp_dir/pre.nlm.log
./lemmaVerbs.pl $tmp_dir/pre.nlm > $tmp_dir/pre.vlm
./lowerCase.pl $tmp_dir/pre.vlm > $tmp_dir/pre.lc
./dropPunct.pl $tmp_dir/pre.lc > $tmp_dir/pre.punct
./lemmaTeens.pl $tmp_dir/pre.punct > $tmp_dir/pre.teen
./typeGirl.pl $tmp_dir/pre.teen > $tmp_dir/pre.girl
cd $rootdir
java -cp target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar preprocessing.FixGirlChild $2 $1
cd $graphdir
./dropThereBe.pl $tmp_dir/pre.girl.new > $tmp_dir/pre.there
cd $rootdir
java -cp target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar preprocessing.LemmaWear $2 $rootdir $1
cd $graphdir
cp $tmp_dir/pre.wear.new $tmp_dir/pre.final
./tokenize.pl $tmp_dir/pre.final > $tmp_dir/pre.token
./dropEventMods.pl $tmp_dir/pre.final > $tmp_dir/trans01.txt
./dropEntityMods.pl $tmp_dir/trans01.txt ../data/entmod.txt > $tmp_dir/trans02.txt
./dropEntityArticle.pl $tmp_dir/trans02.txt > $tmp_dir/trans03.txt
./liftEntity.pl $tmp_dir/trans03.txt $2/$1/$1.lexicon ../data/lexicon.txt $2/$1/$1.subj > $tmp_dir/trans04.txt
./splitXofY.pl $tmp_dir/trans04.txt > $tmp_dir/trans05.txt
./splitXorY.pl $tmp_dir/trans05.txt > $tmp_dir/trans06.txt
./dropPPs.pl $tmp_dir/trans06.txt $2/$1/$1.subj > $tmp_dir/trans07.txt
./dropWearDress.pl $tmp_dir/trans07.txt > $tmp_dir/trans08.txt
./dropTail.pl $tmp_dir/trans08.txt > $tmp_dir/trans10.txt
./splitSubjVerb.pl $tmp_dir/trans10.txt $2/$1/$1 1> $tmp_dir/trans11.txt 2> $tmp_dir/trans11.err
./emptyBrackets.pl $tmp_dir/pre.final $tmp_dir/trans*.txt > $tmp_dir/empty.txt
cp $tmp_dir/trans06.txt $tmp_dir/trans.np
cp $tmp_dir/trans11.txt $tmp_dir/trans.final
cp $tmp_dir/trans11.txt $2/$1/graph/initial.rewrite
cp $tmp_dir/pre.id $2/$1/graph/initial.coref
cp $tmp_dir/pre.token $2/$1/graph/token.txt
awk -F'\t' '{print $1}' $2/$1/$1.coref | awk -F'#' '{print $1}' | sort -u > $2/$1/graph/img.lst

popd > /dev/null
