#!/bin/sh
java \
 -Xmx1024M \
 -Xms1024M \
 -Xmn256M \
 -Xss128k \
 -verbose:gc \
 -server \
 -classpath genoogle.jar:lib/* \
 bio.pih.SOIS \
 -XX:+UseFastAccessorMethods \
 -XX:ParallelGCThreads=20 \
 -XX:+UseConcMarkSweepGC \
 -XX:+UseParNewGC \
 -XX:SurvivorRatio=8 \
 -XX:TargetSurvivorRatio=90  \
 -XX:MaxTenuringThreshold=31 \
 -Dcom.sun.management.jmxremote.password.file=jmxremote.password \
 -Dcom.sun.management.jmxremote \
 -Dcom.sun.management.jmxremote.ssl=false \
 -Dcom.sun.management.jmxremote.authenticate=false \
 -Dcom.sun.management.jmxremote.port=8090 \
 -XX:+AggressiveOpts \
 -s "${1}" "${2}"
