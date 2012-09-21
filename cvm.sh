#!/bin/sh
if [ ! -d "oopsvm/bin" ]; then
    mkdir "oopsvm/bin"
fi
javac -d oopsvm/bin -sourcepath oopsvm/src oopsvm/src/OOPSVM.java
