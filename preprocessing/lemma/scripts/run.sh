#!/bin/bash

# run.sh <corpus name>  <corpus root dir>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null

#./lemmatizer.pl ../../corpora/$1/$1.pos ../../corpora/$1/$1
./lemmatizer.pl $2/$1/$1.pos $2/$1/$1

popd > /dev/null
