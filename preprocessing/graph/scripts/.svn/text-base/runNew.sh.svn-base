#!/bin/bash

# ./runNew.sh <corpus name>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

./makeOutputNP.pl ../tmp/$1 ../../corpora/$1/graph
./propogateImage.pl ../../corpora/$1/graph/np-tree.txt ../../corpora/$1/graph/np-cap.map
./makeOutputVP.pl ../../corpora/$1/$1 ../tmp/$1 ../../corpora/$1/graph
./propogateImage.pl ../../corpora/$1/graph/vp-tree.txt ../../corpora/$1/graph/vp-cap.map
./makeOutput.pl ../tmp/$1 ../../corpora/$1/graph
./makeOutputOrig.pl ../../corpora/$1/$1 ../tmp/$1 ../../corpora/$1/graph
./propogateImage.pl ../../corpora/$1/graph/node-tree.txt ../../corpora/$1/graph/node-cap.map
./makeSentImg.pl ../../corpora/$1/graph
./flipMap.pl ../../corpora/$1/graph/node-cap.map > ../../corpora/$1/graph/cap-node.map
./combineChunk.pl ../../corpora/$1/graph/node-chunk.txt ../../corpora/$1/graph/np-chunk.txt ../../corpora/$1/graph/vp-chunk.txt > ../../corpora/$1/graph/node-chunk2.txt
./typeChunk.pl ../../corpora/$1/graph/node-chunk2.txt > ../../corpora/$1/graph/type-chunk.txt
TRAIN=`./makeSubgraph.pl ../../corpora/$1/$1.split | grep '^train$'`
if [ $TRAIN = "train" ]; then
	./countNodes.pl ../../corpora/$1/graph/train 10 1 > ../../corpora/$1/graph/train/node-image.cnt
	./calcPMI.pl ../../corpora/$1/graph/train/node-image.cnt > ../../corpora/$1/graph/train/node-image.pmi
fi
#./countWords.pl ../../corpora/$1/graph/train 10 1 > ../../corpora/$1/graph/train/word-image.cnt
#./calcPMI.pl ../../corpora/$1/graph/train/word-image.cnt > ../../corpora/$1/graph/train/word-image.pmi
#./countWordsCap.pl ../../corpora/$1/graph/train 10 1 > ../../corpora/$1/graph/train/wordcap-image.cnt
#./calcPMI.pl ../../corpora/$1/graph/train/wordcap-image.cnt > ../../corpora/$1/graph/train/wordcap-image.pmi

popd > /dev/null
