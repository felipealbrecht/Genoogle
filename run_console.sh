#!/bin/sh
$JAVA_HOME/bin/java \
 -server \
 -Xms3096m \
 -Xmx3096m \
 -classpath genoogle.jar:lib/* \
  bio.pih.Genoogle $1 $2
