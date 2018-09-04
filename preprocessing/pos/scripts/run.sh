#!/bin/bash

# run.sh <corpus name> <corpus root dir>

# check the number of lines in the output, in case script failed
function check {
#	if [ `wc -l $tmp_dir/$2 | awk '{print $1}'` -ne `wc -l ../../corpora/$1/$1.token | awk '{print $1}'` ]
	if [ `wc -l $tmp_dir/$2 | awk '{print $1}'` -ne `wc -l $4/$1/$1.token | awk '{print $1}'` ]
	then
		echo "Truncated file - $3 failed." 
		popd > /dev/null
		exit
	fi
}

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir -p $2/$1/tmp/pos
tmp_dir=$2/$1/tmp/pos

./fixHyphen.pl $2/$1/$1.token > $tmp_dir/hyphen.txt
check $1 "hyphen.txt" "fixHyphen.pl" $2
./fixThough.pl $tmp_dir/hyphen.txt > $tmp_dir/though.txt
check $1 "though.txt" "fixThough.pl" $2
awk -F'\t' '{print $1}' $tmp_dir/though.txt > $tmp_dir/url.txt
awk -F'\t' '{print $2}' $tmp_dir/though.txt > $tmp_dir/sent.txt
./fixPunct.pl $tmp_dir/sent.txt > $tmp_dir/punct.txt
check $1 "punct.txt" "fixPunct.pl" $2
./fixNumbers.pl $tmp_dir/punct.txt > $tmp_dir/numbers.txt
check $1 "numbers.txt" "fixNumbers.pl" $2
./postag.sh $tmp_dir/numbers.txt > $tmp_dir/pos.txt
check $1 "pos.txt" "Tagger" $2
./fixWatch.pl $tmp_dir/pos.txt > $tmp_dir/watch.txt
check $1 "watch.txt" "fixWatch.pl" $2
./fixUH.pl $tmp_dir/watch.txt > $tmp_dir/UH.txt
check $1 "UH.txt" "fixUH.pl" $2
./fixing.pl $tmp_dir/UH.txt > $tmp_dir/ing.txt
check $1 "ing.txt" "fixing.pl" $2
./fixPPngram.pl $tmp_dir/ing.txt > $tmp_dir/ngram.txt
check $1 "ngram.txt" "fixPPngram.pl" $2
./fixStandNN.pl $tmp_dir/ngram.txt 1> $tmp_dir/stand.txt 2> $tmp_dir/stand.log
check $1 "stand.txt" "fixStandNN.pl" $2
./fixIs.pl $tmp_dir/stand.txt 1> $tmp_dir/is.txt 2> $tmp_dir/is.log
check $1 "is.txt" "fixIs.pl" $2
./forcePOS.pl $tmp_dir/is.txt > $tmp_dir/force.txt
check $1 "force.txt" "forcePOS.pl" $2
./fixNotDT-V.pl $tmp_dir/force.txt > $tmp_dir/notdt.txt
check $1 "notdt.txt" "fixNotDT-V.pl" $2
./fixBuilding.pl $tmp_dir/notdt.txt > $tmp_dir/building.txt
check $1 "building.txt" "fixBuilding.pl" $2
./fixTO-VBX.pl $tmp_dir/building.txt > $tmp_dir/to-vbx.txt
check $1 "to-vbx.txt" "fixTO-VBX.pl" $2
./fixV-TO.pl $tmp_dir/to-vbx.txt > $tmp_dir/v-to.txt
check $1 "v-to.txt" "fixV-TO.pl" $2
./fixTO-VB.pl $tmp_dir/v-to.txt > $tmp_dir/to-vb.txt
check $1 "to-vb.txt" "fixTO-VB.pl" $2
./fixAttempt.pl $tmp_dir/to-vb.txt > $tmp_dir/attempt.txt
check $1 "attempt.txt" "fixAttempt.pl" $2
./fixVerbPP.pl $tmp_dir/attempt.txt > $tmp_dir/vpp.txt
check $1 "vpp.txt" "fixVerbPP.pl" $2
./fixConverse.pl $tmp_dir/vpp.txt > $tmp_dir/cnv.txt
check $1 "cnv.txt" "fixConverse.pl" $2
./fixWearing.pl $tmp_dir/cnv.txt > $tmp_dir/wear.txt
check $1 "wear.txt" "fixWearing.pl" $2
./fixSled.pl $tmp_dir/wear.txt 1> $tmp_dir/sled.txt 2> $tmp_dir/sled.log
check $1 "sled.txt" "fixSled.pl" $2
./fixSign.pl $tmp_dir/sled.txt > $tmp_dir/sign.txt
check $1 "sign.txt" "fixSign.pl" $2
./fixDown.pl $tmp_dir/sign.txt > $tmp_dir/down.txt
check $1 "down.txt" "fixDown.pl" $2
./joinCompoundNouns.pl $tmp_dir/down.txt > $tmp_dir/njoin.txt
check $1 "njoin.txt" "joinCompoundNouns.pl" $2
./fixLeft.pl $tmp_dir/njoin.txt > $tmp_dir/left.txt
check $1 "left.txt" "fixLeft.pl" $2
./joinCompoundVerbs.pl $tmp_dir/left.txt > $tmp_dir/vjoin.txt
check $1 "vjoin.txt" "joinCompoundVerbs.pl" $2
./chunker.sh $tmp_dir/vjoin.txt > $tmp_dir/chunk.txt
check $1 "chunk.txt" "chunker.sh" $2
./splitCompoundVerbs.pl $tmp_dir/left.txt $tmp_dir/chunk.txt > $tmp_dir/vsplit.txt
check $1 "vsplit.txt" "splitCompoundVerbs.pl" $2
./splitCompoundNouns.pl $tmp_dir/vsplit.txt > $tmp_dir/nsplit.txt
check $1 "nsplit.txt" "splitCompoundNouns.pl" $2
./makePPngram.pl $tmp_dir/nsplit.txt > $tmp_dir/tried.txt
check $1 "tried.txt" "makePPngram.pl" $2
./fixInOrderTo.pl $tmp_dir/tried.txt > $tmp_dir/inorder.txt
check $1 "inorder.txt" "fixInOrderTo.pl" $2
./fixSbar.pl $tmp_dir/inorder.txt > $tmp_dir/sbar.txt
check $1 "sbar.txt" "fixSbar.pl" $2
./fixNPHeadNouns.pl $tmp_dir/sbar.txt 1> $tmp_dir/hnoun.txt 2> $tmp_dir/hnoun.log
check $1 "hnoun.txt" "fixNPHeadNouns.pl" $2
./fixNPHeadNouns2.pl $tmp_dir/hnoun.txt 1> $tmp_dir/hnoun2.txt 2> $tmp_dir/hnoun2.log
check $1 "hnoun2.txt" "fixNPHeadNouns2.pl" $2
./fixDressed.pl $tmp_dir/hnoun2.txt 1> $tmp_dir/dress.txt 2> $tmp_dir/dress.log
check $1 "dress.txt" "fixDressed.pl" $2
./fixCooks.pl $tmp_dir/dress.txt > $tmp_dir/cooks.txt
check $1 "cooks.txt" "fixCooks.pl" $2
./fixShop.pl $tmp_dir/cooks.txt > $tmp_dir/shop.txt
check $1 "shop.txt" "fixShop.pl" $2
./fixVPaux.pl $tmp_dir/shop.txt > $tmp_dir/vpaux.txt
check $1 "vpaux.txt" "fixVPaux.pl" $2
./fixTO.pl $tmp_dir/vpaux.txt > $tmp_dir/to.txt
check $1 "to.txt" "fixTO.pl" $2
./normDobj.pl $tmp_dir/to.txt > $tmp_dir/dobj.txt
check $1 "dobj.txt" "normDobj.pl" $2
./fixWearingChunk.pl $tmp_dir/dobj.txt 1> $tmp_dir/wearchunk.txt 2> $tmp_dir/wearchunk.log
check $1 "wearchunk.txt" "fixWearingChunk.pl" $2
./fixDetHead.pl $tmp_dir/wearchunk.txt > $tmp_dir/dthead.txt
check $1 "dthead.txt" "fixDetHead.pl" $2
./fixToPerform.pl $tmp_dir/dthead.txt > $tmp_dir/perform.txt
check $1 "perform.txt" "fixToPerform.pl" $2
./fixLay.pl $tmp_dir/perform.txt > $tmp_dir/lay.txt
check $1 "lay.txt" "fixLay.pl" $2
./breakCC-NPs.pl $tmp_dir/lay.txt > $tmp_dir/break.txt
check $1 "break.txt" "breakCC-NPs.pl" $2
./fixRefVerbs.pl $tmp_dir/break.txt > $tmp_dir/ref1.txt
check $1 "ref1.txt" "fixRefVerbs.pl" $2
./fixRefVerbs.pl $tmp_dir/ref1.txt > $tmp_dir/ref2.txt
check $1 "ref2.txt" "fixRefVerbs.pl" $2
./fixVerbs.pl $tmp_dir/ref2.txt > $tmp_dir/verbs1.txt
check $1 "verbs1.txt" "fixVerbs.pl" $2
./fixPlay.pl $tmp_dir/verbs1.txt > $tmp_dir/play.txt
check $1 "play.txt" "fixPlay.pl" $2
./fixVerbs.pl $tmp_dir/play.txt > $tmp_dir/verbs2.txt
check $1 "verbs2.txt" "fixVerbs.pl" $2
./fixWhile.pl $tmp_dir/verbs2.txt > $tmp_dir/while.txt
check $1 "while.txt" "fixWhile.pl" $2
./fixQuotes.pl $tmp_dir/while.txt > $tmp_dir/quote.txt
check $1 "quote.txt" "fixQuotes.pl" $2
./fixVP-RB.pl $tmp_dir/quote.txt > $tmp_dir/vp-rb.txt
check $1 "vp-rb.txt" "fixVP-RB.pl" $2
./fixPRP-VP.pl $tmp_dir/vp-rb.txt > $tmp_dir/vp-prp.txt
check $1 "vp-prp.txt" "fixPRP-VP.pl" $2
./fixPrt.pl $tmp_dir/vp-prp.txt > $tmp_dir/prt.txt
check $1 "prt.txt" "fixPrt.pl" $2
./fixPP-Prt.pl $tmp_dir/prt.txt > $tmp_dir/pp-prt.txt
check $1 "pp-prt.txt" "fixPP-Prt.pl" $2
./fixDetHead.pl $tmp_dir/pp-prt.txt > $tmp_dir/dthead2.txt
check $1 "dthead2.txt" "fixDetHead.pl" $2
./fixSomeX.pl $tmp_dir/dthead2.txt > $tmp_dir/some.txt
check $1 "some.txt" "fixSomeX.pl" $2
./breakVPs.pl $tmp_dir/some.txt > $tmp_dir/breakVP.txt
check $1 "breakVP.txt" "breakVPs.pl" $2
./fixTO-end.pl $tmp_dir/breakVP.txt > $tmp_dir/to-end.txt
check $1 "to-end.txt" "fixTO-end.pl" $2
./fixVPVP-TO.pl $tmp_dir/to-end.txt > $tmp_dir/vpvp-to.txt
check $1 "vpvp-to.txt" "fixVPVP-TO.pl" $2
./fixFunction.pl $tmp_dir/vpvp-to.txt > $tmp_dir/function.txt
check $1 "function.txt" "fixFunction.pl" $2
./fixPP-CC.pl $tmp_dir/function.txt > $tmp_dir/pp-cc.txt
check $1 "pp-cc.txt" "fixPP-CC.pl" $2
./checkPunctTag.pl $tmp_dir/pp-cc.txt > $tmp_dir/punct.check
../../misc/splice.pl $tmp_dir/url.txt $tmp_dir/pp-cc.txt > $2/$1/$1.pos

popd > /dev/null
