#!/bin/sh
/home/albrecht/java/jrmc-3.0.3-1.6.0/bin/java \
 -Xms1024m \
 -Xmx2048m \
 -server \
 -XXaggressive:opt \
 -XXtlaSize:min=4096k \
 -Dcom.sun.management.jmxremote \
 -classpath genoogle.jar:lib/* \
  bio.pih.interfaces.WebServer \
  www.pih.bio.br \
  /home/albrecht/genoogle/webapps/ 8080
