#!/bin/bash

# run.sh <file name> <corpus root dir> (skip the extension)

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir -p $2/$1/tmp/entity
tmp_dir=$2/$1/tmp/entity

./fixNPs.pl $2/$1/$1.pos 1> $tmp_dir/NP.pos 2> $tmp_dir/NP.log
./getNPs.pl $tmp_dir/NP.pos > $tmp_dir/NP.np
./fixANNS.pl $tmp_dir/NP.np $tmp_dir/NP.pos 1> $tmp_dir/ANNS.pos 2> $tmp_dir/ANNS.log
./getNPs.pl $tmp_dir/ANNS.pos > $tmp_dir/def.np
./fixAndCC.pl $tmp_dir/def.np $tmp_dir/ANNS.pos 1> $tmp_dir/and.pos 2> $tmp_dir/and.log
./getNPs.pl $tmp_dir/and.pos > $tmp_dir/and.np
./fixVerbing.pl $tmp_dir/and.np $tmp_dir/and.pos 1> $tmp_dir/verb.pos 2> $tmp_dir/verb.log
./getNPs.pl $tmp_dir/verb.pos > $tmp_dir/verb.np
./fixAdvp.pl $tmp_dir/verb.np $tmp_dir/verb.pos 1> $tmp_dir/advp.pos 2> $tmp_dir/advp.log
./getNPs.pl $tmp_dir/advp.pos > $tmp_dir/advp.np
./makeEntity.pl $tmp_dir/advp.np $tmp_dir/advp.pos 1> $tmp_dir/ent.pos 2> $tmp_dir/ent.log
./getNPs.pl $tmp_dir/ent.pos > $tmp_dir/ent.np
./easyCorefs.pl $tmp_dir/ent.np > $tmp_dir/easy.np
./makeHypeLexicon.pl $tmp_dir/easy.np > $tmp_dir/lexicon.txt
./coverCorefs.pl $tmp_dir/easy.np $tmp_dir/lexicon.txt > $tmp_dir/easy.cover
./corefNPs.pl $tmp_dir/easy.np $tmp_dir/lexicon.txt > $2/$1/$1.np
./makeCoref.pl $tmp_dir/ent.pos $2/$1/$1.np > $2/$1/$1.coref

popd > /dev/null
