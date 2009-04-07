#!/bin/sh
/home/albrecht/jrmc-3.0.3-1.6.0/bin/java \
 -Xms2048m \
 -Xmx2048m \
 -Xss128k \
 -XX:+UseFastAccessorMethods \
 -XX:+AggressiveOpts \
 -verbose:gc \
 -server \
 -classpath genoogle.jar:lib/* \
 -Dcom.sun.management.jmxremote \
  bio.pih.web.WebServer \
  www.pih.bio.br \
  /home/albrecht/genoogle/webapps/ 8080
#-Dcom.sun.management.jmxremote.password.file=jmxremote.password \
#-Dcom.sun.management.jmxremote.ssl=false \
#-Dcom.sun.management.jmxremote.authenticate=false \
#-Dcom.sun.management.jmxremote.port=8090 \
