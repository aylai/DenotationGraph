#!/bin/bash
BASEDIR=$(dirname $0)
cat $1 | java -cp $BASEDIR/../../opennlp/output/opennlp-tools-1.3.0.jar:$BASEDIR/../../opennlp/lib/maxent-2.4.0.jar:$BASEDIR/../../opennlp/lib/trove.jar opennlp.tools.lang.english.PosTagger $BASEDIR/../../opennlp/models/tag.bin.gz
