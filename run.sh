#!/bin/bash

graph_name="mpe_test_corpus"
corpus_dir="corpora"
preprocess_dir="preprocessing"
num_cores_parser=12

# must run first: source setenvSh.sh
pwd_dir=`pwd`
cd $preprocess_dir
source setenvSh.sh
cd $pwd_dir
if [ -z "$PERLLIB" ]; then export PERLLIB=`pwd`; else export PERLLIB=$PERLLIB:`pwd`; fi
if [ -z "$PERL5LIB" ]; then export PERL5LIB=`pwd`; else export PERL5LIB=$PERL5LIB:`pwd`; fi
export WNSEARCHDIR=`pwd`/$preprocess_dir/WordNet
mkdir -p $corpus_dir/$graph_name/tmp
mvn clean compile
mvn package

echo "token"
$preprocess_dir/token/scripts/run.sh $graph_name $corpus_dir # starts with .spell file
echo "compound"
$preprocess_dir/compound/scripts/run.sh $graph_name $corpus_dir
echo "pos"
$preprocess_dir/pos/scripts/run.sh $graph_name $corpus_dir # starts with .token file
echo "lemma"
$preprocess_dir/lemma/scripts/run.sh $graph_name $corpus_dir
echo "entity"
$preprocess_dir/entity/scripts/run.sh $graph_name $corpus_dir
# $preprocess_dir/entity/scripts/runNoCoref.sh $graph_name $corpus_dir
echo "parser"
$preprocess_dir/parser/scripts/run.sh $graph_name $num_cores_parser $corpus_dir
echo "event"
$preprocess_dir/event/scripts/run.sh $graph_name $corpus_dir
echo "graph pre"
$preprocess_dir/graph/scripts/runPre0.sh $graph_name $corpus_dir # normalizes girl/girl_child and fixes "wear"

echo "generate graph"
java -Xmx80g -jar target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar $preprocess_dir $graph_name $corpus_dir full false
# if memory issues, try the following two lines instead:
#java -Xmx80g -jar target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar $preprocess_dir $graph_name $corpus_dir start false
#java -Xmx80g -jar target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar $preprocess_dir $graph_name $corpus_dir finish false
