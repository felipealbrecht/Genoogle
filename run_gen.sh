#!/bin/sh
$JAVA_HOME/bin/java \
 -Xms1024m \
 -Xmx2048m \
 -server \
 -classpath genoogle.jar:lib/* \
 bio.pih.SOIS -g
