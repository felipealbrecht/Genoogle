#!/bin/sh

# Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
# Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
#
# For further information check the LICENSE file.
#

# ----------------------------------------------------------------------------------------
# Script to start Genoogle with standalone web server which uses another Genoogle 
#  instance by wEBsERVICES
#
# Environment Variables:
#
#   GENOOGLE_HOME            Point at your Genoogle base directory.
#
#   JAVA_HOME                Must point at your Java Development Kit installation.
#                            Required to run the with the "debug" or "javac" argument.
#
#   WEB_STANDALONE_PORT      Port where the stand alone service will run.
#
#   WEB_STANDALONE_APP_DIR   Point where is Genoogle webservices web app directory.
#
# ----------------------------------------------------------------------------------------

if [ -n "${GENOOGLE_HOME+x}" ]; then
	echo "\$GENOOGLE_HOME=$GENOOGLE_HOME"
else
	echo "\$GENOOGLE_HOME is not set. Using `pwd`"
	GENOOGLE_HOME=`pwd`
	echo "\$GENOOGLE_HOME=$GENOOGLE_HOME"
fi

if [ -d ${GENOOGLE_HOME}/conf ]; then
	echo "Configuration files is $GENOOGLE_HOME/conf/conf.xml"
else
	echo "Configuration directory not found. Please configure correctly \$GENOOGLE_HOME."
	return
fi

if [ -n "${WEB_STANDALONE_APP_DIR+x}" ]; then
	echo "\$WEB_STANDALONE_APP_DIR=$WEB_STANDALONE_APP_DIR"
else
	echo "\$WEB_STANDALONE_APP_DIR is not set. Trying ${GENOOGLE_HOME}/webapps/genoogle-standalone."
	if [ -d ${GENOOGLE_HOME}/webapps/genoogle-standalone ]; then
		WEB_STANDALONE_APP_DIR=${GENOOGLE_HOME}/webapps/genoogle-standalone
		echo "Web Service App found at $WEB_STANDALONE_APP_DIR".
	else
		echo "Web Service App not found. Please configure it at WEB_STANDALONE_APP_DIR variable."
	fi
fi

if [ -d ${WEB_STANDALONE_APP_DIR}/WEB-INF ]; then
	echo "Web services Web application found at ${WEB_STANDALONE_APP_DIR}"
else
	echo "${WEB_STANDALONE_APP_DIR} does not appears to be the Web Service App directory. Please, configure WEB_STANDALONE_APP_DIR variable correctly."
	return;
fi

DEFAULT_PORT=8080
if [ -n "${WEB_STANDALONE_PORT+x}" ]; then
	echo "\$WEB_STANDALONE_PORT=$WEB_STANDALONE_PORT"
else
	echo "\$WEB_STANDALONE_PORT is not set. Using $DEFAULT_PORT."
	WEB_STANDALONE_PORT=$DEFAULT_PORT
	echo "\$WEB_STANDALONE_PORT=$WEB_STANDALONE_PORT"
fi



$JAVA_HOME/bin/java \
 -Xms256m \
 -Xmx512m \
 -server \
 -classpath ${GENOOGLE_HOME}/genoogle.jar:${GENOOGLE_HOME}/lib/*:${GENOOGLE_HOME}/lib/jetty/* \
  bio.pih.genoogle.interfaces.WebServer \
  ${WEB_STANDALONE_APP_DIR} ${WEB_STANDALONE_PORT} true
