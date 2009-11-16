#!/bin/sh
$JAVA_HOME/bin/java \
 -Xms1024m \
 -Xmx2048m \
 -server \
 -classpath genoogle.jar:lib/*:lib/jetty/* \
  bio.pih.interfaces.WebServer \
  webapps/genoogle-web 8080
