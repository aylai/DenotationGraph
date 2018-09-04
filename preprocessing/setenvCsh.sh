if $?PERLLIB then
	setenv PERLLIB ${PERLLIB}:`pwd`
else
	setenv PERLLIB `pwd`
endif
if $?PERL5LIB then
	setenv PERL5LIB ${PERL5LIB}:`pwd`
else
	setenv PERL5LIB `pwd`
endif
setenv WNSEARCHDIR `pwd`/WordNet
