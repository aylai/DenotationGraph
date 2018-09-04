if [ -z "$PERLLIB" ]; then export PERLLIB=`pwd`; else export PERLLIB=$PERLLIB:`pwd`; fi
if [ -z "$PERL5LIB" ]; then export PERL5LIB=`pwd`; else export PERL5LIB=$PERL5LIB:`pwd`; fi
export WNSEARCHDIR=`pwd`/WordNet
