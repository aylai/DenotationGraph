#!/bin/bash

graph_name="results_20151006"
corpus_dir="/shared/projects/DenotationGraphCorpora"
preprocess_dir="/shared/projects/DenotationGraph"
graph_gen_dir="/shared/projects/DenotationGraphGeneration"
num_cores_parser=12

# must run first: source setenvSh.sh
if [ -z "$PERLLIB" ]; then export PERLLIB=`pwd`; else export PERLLIB=$PERLLIB:`pwd`; fi
if [ -z "$PERL5LIB" ]; then export PERL5LIB=`pwd`; else export PERL5LIB=$PERL5LIB:`pwd`; fi
export WNSEARCHDIR=`pwd`/WordNet

echo "redo NP"
cd $preprocess_dir/entity/scripts
./getNPs.pl ../../corpora/$graph_name/$graph_name.coref > ../tmp/$graph_name/NP.np
cp ../tmp/$graph_name/NP.np $corpus_dir/corpus/$graph_name/$graph_name.np 
cd $preprocess_dir
echo "parser"
parser/scripts/run.sh $graph_name $num_cores_parser
echo "event"
event/scripts/run.sh $graph_name
echo "graph pre"
graph/scripts/runPre0.sh $graph_name $graph_den_dir $corpus_dir # normalizes girl/girl_child and fixes "wear"
html/scripts/untokenize.pl corpora/$graph_name/$graph_name.token > corpora/$graph_name/$graph_name.untoken

echo "generate graph"
cd $graph_gen_dir
mvn clean compile
mvn package -DskipTests
java -Xmx80g -jar $graph_gen_dir/target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar $graph_gen_dir/data/ $graph_name $corpus_dir start
java -Xmx80g -jar $graph_gen_dir/target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar $graph_gen_dir/data/ $graph_name $corpus_dir finish

echo "generate graph DB"
java -Xmx50g -cp /home/aylai2/apps/sqlite4java-282/sqlite4java.jar:$graph_gen_dir/target/DenotationGraphGeneration-1.0-SNAPSHOT-jar-with-dependencies.jar:. structures.DenotationDB $graph_gen_dir/data/ $corpus_dir/corpora/$graph_name/
