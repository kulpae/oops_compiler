#!/bin/sh

ARGS=$@
OOPS=$@
#make asm name of OOPS
ASM=$( echo $OOPS | sed -e 's/\.oops/.asm/g' | sed -e 's/.*\///g' )

java -cp oopsc/bin OOPSC $OOPS Kompilate/$ASM
