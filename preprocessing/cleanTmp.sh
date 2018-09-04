#!/bin/bash

BASEDIR=$(dirname $0)
pushd $BASEDIR > /dev/null
rm -rf */tmp/*
popd > /dev/null
