#!/bin/bash

# ./run.sh <corpus name> <corpus root dir>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir -p $2/$1/tmp/event
tmp_dir=$2/$1/tmp/event

./getVPs.pl $2/$1/$1.coref > $tmp_dir/vp.txt
./getSubj.pl $2/$1/$1-malt-poly.conll > $tmp_dir/presubj.txt
./getDobj.pl $2/$1/$1.coref $2/$1/$1.np $2/$1/$1-malt-poly.conll > $tmp_dir/dobj.txt
rm -f $tmp_dir/*.np
./getNPtype.pl $2/$1/$1.np ../data/agentref.txt > $tmp_dir/agentref.np
./getNPtype.pl $2/$1/$1.np ../data/animal.txt > $tmp_dir/animal.np
./getNPtype.pl $2/$1/$1.np ../data/clothing.txt > $tmp_dir/clothing.np
./getNPtype.pl $2/$1/$1.np ../data/person.txt > $tmp_dir/person.np
./getNPtype.pl $2/$1/$1.np ../data/vehicle.txt > $tmp_dir/vehicle.np
./getNPtype.pl $2/$1/$1.np ../data/who.txt > $tmp_dir/who.np
./getNPtype.pl $2/$1/$1.np ../data/that-which.txt > $tmp_dir/that-which.np
sort -u $tmp_dir/*.np > $tmp_dir/all.np
sort -u $tmp_dir/agentref.np $tmp_dir/animal.np $tmp_dir/person.np > $tmp_dir/agent.np
./getSubjNP.pl $tmp_dir/presubj.txt $tmp_dir/clothing.np > $tmp_dir/clothing.subj
./getSubjNP.pl $tmp_dir/presubj.txt $tmp_dir/vehicle.np > $tmp_dir/vehicle.subj
./getSubjNP.pl $tmp_dir/presubj.txt $tmp_dir/all.np > $tmp_dir/all.subj
../../misc/gimme.pl $tmp_dir/presubj.txt -$tmp_dir/all.subj > $tmp_dir/not.subj
../../misc/gimme.pl $tmp_dir/vp.txt -$tmp_dir/presubj.txt 1 | awk '{print $1}' > $tmp_dir/nosubj.vp
./findNewSubj.pl $tmp_dir/clothing.subj $tmp_dir/agent.np $tmp_dir/vp.txt $2/$1/$1.coref ../data/exclverb-clothing.txt > $tmp_dir/clothing-agent.subj
./findNewSubj.pl $tmp_dir/vehicle.subj $tmp_dir/agent.np $tmp_dir/vp.txt $2/$1/$1.coref ../data/exclverb-vehicle.txt > $tmp_dir/vehicle-agent.subj
./findNewSubj.pl $tmp_dir/not.subj $tmp_dir/agent.np $tmp_dir/vp.txt $2/$1/$1.coref ../data/exclverb-not.txt > $tmp_dir/not-agent.subj
./findNewSubj2.pl $tmp_dir/nosubj.vp $tmp_dir/agent.np $tmp_dir/vp.txt $2/$1/$1.coref ../data/exclverb-not.txt $tmp_dir/presubj.txt > $tmp_dir/nosubj-agent.subj
./replaceSubj.pl $tmp_dir/presubj.txt $tmp_dir/clothing-agent.subj $tmp_dir/vehicle-agent.subj $tmp_dir/not-agent.subj | sort -u > $tmp_dir/presubj2.txt
./getSubjNP.pl $tmp_dir/presubj2.txt $tmp_dir/who.np > $tmp_dir/who.subj
./findNewSubj3.pl $tmp_dir/who.subj $tmp_dir/person.np $2/$1/$1.coref > $tmp_dir/who-person.subj
./replaceSubj.pl $tmp_dir/presubj2.txt $tmp_dir/who-person.subj | sort -u > $tmp_dir/subj.txt
./getNPs.pl $2/$1/$1.np > $tmp_dir/np.txt
./getSVO.pl $tmp_dir/np.txt $tmp_dir/vp.txt $tmp_dir/subj.txt $tmp_dir/dobj.txt > $tmp_dir/svo.txt

cp $tmp_dir/vp.txt $2/$1/$1.vp
cp $tmp_dir/subj.txt $2/$1/$1.subj
cp $tmp_dir/dobj.txt $2/$1/$1.dobj
cp $tmp_dir/svo.txt $2/$1/$1.svo

popd > /dev/null
