#!/bin/bash

# ./run.sh <corpus name> <corpus root dir>

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null
mkdir -p $2/$1/tmp/token
tmp_dir=$2/$1/tmp/token

awk -F'\t' '{print $1}' $2/$1/$1.spell > $tmp_dir/url.txt
awk -F'\t' '{print $2}' $2/$1/$1.spell > $tmp_dir/sent.txt
./token.sh $tmp_dir/sent.txt > $tmp_dir/token.txt
../../misc/splice.pl $tmp_dir/url.txt $tmp_dir/token.txt > $2/$1/$1.pretoken

popd > /dev/null
