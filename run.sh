#!/bin/sh
java \
 -Xmx14196M \
 -Xms14196M \
 -Xmn3g \
 -Xss128k \
 -verbose:gc \
 -server \
 -classpath genoogle.jar:lib/* \
  bio.pih.web.WebServer \
  www.pih.bio.br \
  /home/albrecht/genoogle/webapps/ 8090
 #-Dcom.sun.management.jmxremote.password.file=jmxremote.password \
 #-Dcom.sun.management.jmxremote \
 #-Dcom.sun.management.jmxremote.ssl=false \
 #-Dcom.sun.management.jmxremote.authenticate=false \
 #-Dcom.sun.management.jmxremote.port=8090 \
 #-XX:+UseFastAccessorMethods \
 #-XX:ParallelGCThreads=20 \
 #-XX:+UseConcMarkSweepGC \
 #-XX:+UseParNewGC \
 #-XX:SurvivorRatio=8 \
 #-XX:TargetSurvivorRatio=90  \
 #-XX:MaxTenuringThreshold=31 \
 #-XX:+AggressiveOpts \
