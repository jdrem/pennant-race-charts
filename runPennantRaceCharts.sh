#!/bin/sh

CLASSPATH=./build/libs/"*":./build/dependencies/"*"

$JAVA_HOME/bin/java -cp $CLASSPATH net.remgant.charts.Pennant $*
