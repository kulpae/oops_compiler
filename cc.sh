#!/bin/sh

mkdir -p oopsc/bin 2>&1 > /dev/null
javac -d oopsc/bin -sourcepath oopsc/src oopsc/src/OOPSC.java
