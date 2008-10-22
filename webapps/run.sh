#!/bin/sh
java \
 -Dcom.sun.management.jmxremote \
 -Dcom.sun.management.jmxremote.password.file=jmxremote.password \
 -Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.port=8090 \
 -server \
 -Xmx300m \
 -classpath genoogle.jar:lib/* \
  bio.pih.web.WebServer \
  www.pih.bio.br \
  /home/albrecht/web-genoogle/webapps/ 8080
