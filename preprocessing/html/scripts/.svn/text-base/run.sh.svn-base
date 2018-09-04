#!/bin/bash

# ./run.sh <corpus name> <max size> <min size> <image URL>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

mkdir ../../corpora/$1/html
cp ../data/* ../../corpora/$1/html
./untokenize.pl ../../corpora/$1/$1.token > ../../corpora/$1/$1.untoken
./pickNodes.pl ../../corpora/$1/graph ../../corpora/$1/graph/train $2 $3 > ../../corpora/$1/html/node.type
./makeIndex.pl ../../corpora/$1/graph ../../corpora/$1/html
./htmlNode.pl ../../corpora/$1/graph ../../corpora/$1/graph/train ../../corpora/$1/html ../../corpora/$1/$1.untoken $4
./htmlImage.pl ../../corpora/$1/$1.untoken ../../corpora/$1/graph ../../corpora/$1/html $4
./htmlPMI.pl ../../corpora/$1/graph/train ../../corpora/$1/html

popd > /dev/null
