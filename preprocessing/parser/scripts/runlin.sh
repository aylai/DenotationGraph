#!/bin/bash

BASEDIR=$(dirname $0)

java -Xmx2048m -jar $BASEDIR/../malt/malt.jar -c engmalt.linear -w $BASEDIR/../malt -i $1 -o $2 -m parse
