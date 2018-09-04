#!/usr/bin/perl

open(file, $ARGV[0]);
@lista = <file>;
chomp(@lista);
close(file);

open(file, $ARGV[1]);
@listb = <file>;
chomp(@listb);
close(file);

for ($i = 0; $i <= $#lista; $i++) {
    print "$lista[$i]\t$listb[$i]\n";
}
