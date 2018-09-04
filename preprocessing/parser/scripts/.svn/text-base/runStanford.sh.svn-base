#!/bin/bash

BASEDIR=$(dirname $0)

java -cp $BASEDIR/../stanford/stanford-parser.jar -mx1024m edu.stanford.nlp.parser.lexparser.LexicalizedParser -sentences newline -outputFormat "wordsAndTags,typedDependencies" $BASEDIR/../stanford/englishPCFG.ser.gz $1 > $2
