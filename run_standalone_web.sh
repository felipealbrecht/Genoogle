#!/bin/sh
$JAVA_HOME/bin/java \
 -Xms256m \
 -Xmx512m \
 -server \
 -classpath genoogle.jar:lib/*:lib/jetty/* \
  bio.pih.genoogle.interfaces.WebServer \
  webapps/genoogle-standalone 8080 true
