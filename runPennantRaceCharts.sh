#!/bin/sh

CLASSPATH=./target/"*":./target/dependency/"*"

$JAVA_HOME/bin/java -cp $CLASSPATH net.remgant.charts.Pennant $*
