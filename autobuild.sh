#!/bin/bash
ant -Djava.compilerargs=-Xlint:deprecation -q debug
cp bin/SmsMail-debug-unaligned.apk .
