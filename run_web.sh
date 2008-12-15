#!/bin/sh
java \
 -Xms2048m \
 -Xmx2048m \
 -Xmn1g \
 -Xss128k \
 -XX:ParallelGCThreads=5 \
 -XX:+UseConcMarkSweepGC \
 -XX:+UseParNewGC \
 -XX:SurvivorRatio=8 \
 -XX:TargetSurvivorRatio=90  \
 -XX:MaxTenuringThreshold=31 \
 -XX:+UseFastAccessorMethods \
 -XX:+AggressiveOpts \
 -verbose:gc \
 -server \
 -classpath genoogle.jar:lib/* \
  bio.pih.web.WebServer \
  www.pih.bio.br \
  /home/albrecht/genoogle/webapps/ 8080
# -Dcom.sun.management.jmxremote \
#-Dcom.sun.management.jmxremote.password.file=jmxremote.password \
#-Dcom.sun.management.jmxremote.ssl=false \
#-Dcom.sun.management.jmxremote.authenticate=false \
#-Dcom.sun.management.jmxremote.port=8090 \
