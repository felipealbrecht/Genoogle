#!/bin/sh
/home/albrecht/jrmc-3.0.3-1.6.0/bin/java \
 -Xms1024m \
 -Xmx2048m \
 -server \
 -XX:+UseFastAccessorMethods \
 -XX:+AggressiveOpts \
 -XX:+UseTLAB \
 -XXaggressive \
 -XXtlaSize:min=4096k \
 -Dcom.sun.management.jmxremote \
 -classpath genoogle.jar:lib/* \
  bio.pih.web.WebServer \
  www.pih.bio.br \
  /home/albrecht/genoogle/webapps/ 8080
# -Dcom.sun.management.jmxremote.password.file=jmxremote.password \
# -Dcom.sun.management.jmxremote.ssl=false \
# -Dcom.sun.management.jmxremote.authenticate=false \
# -Dcom.sun.management.jmxremote.port=8090 \
# -verbose:gc \
# -XX:+PrintTLAB \
# -Xloggc:gc_details -XX:+PrintGCDetails \
# -Xss128k \
# java \
# -XX:TLABSize=1024k \
