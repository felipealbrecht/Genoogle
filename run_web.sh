#!/bin/sh

# Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
# Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
#
# For further information check the LICENSE file.
#

# ----------------------------------------------------------------------------------------
# Script to start Genoogle with web server, web services, and console interfaces.
#
# Environment Variables:
#
#   GENOOGLE_HOME         Point at your Genoogle base directory.
#
#   JAVA_HOME             Must point at your Java Development Kit installation.
#                         Required to run the with the "debug" or "javac" argument.
#
#   WEB_SERVICE_APP_DIR   Point where is Genoogle webservices web app directory.
#
#   WEB_SERVICE_PORT      Port where the Web Services will run.
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

if [ -n "${WEB_SERVICE_APP_DIR+x}" ]; then
	echo "\$WEB_SERVICE_APP_DIR=$WEB_SERVICE_APP_DIR"
else
	echo "\$WEB_SERVICE_APP_DIR is not set. Trying ${GENOOGLE_HOME}/webapps/genoogle-webservices."
	if [ -d ${GENOOGLE_HOME}/webapps/genoogle-webservices ]; then
		WEB_SERVICE_APP_DIR=${GENOOGLE_HOME}/webapps/genoogle-webservices
		echo "Web Service App found at $WEB_SERVICE_APP_DIR".
	else
		echo "Web Service App not found. Please configure it at WEB_SERVICE_APP_DIR variable."
	fi
fi

if [ -d ${WEB_SERVICE_APP_DIR}/WEB-INF ]; then
	echo "Web services Web application found at ${WEB_SERVICE_APP_DIR}"
else
	echo "${WEB_SERVICE_APP_DIR} does not appears to be the Web Service App directory. Please, configure WEB_SERVICE_APP_DIR variable correctly."
	return;
fi

DEFAULT_PORT=8090
if [ -n "${WEB_SERVICE_PORT+x}" ]; then
	echo "\$WEB_SERVICE_PORT=$WEB_SERVICE_PORT"
else
	echo "\$WEB_SERVICE_PORT is not set. Using $DEFAULT_PORT."
	WEB_SERVICE_PORT=$DEFAULT_PORT
	echo "\$WEB_SERVICE_PORT=$WEB_SERVICE_PORT"
fi

$JAVA_HOME/bin/java \
 -Xms2048m \
 -Xmx3600m \
 -server \
 -classpath ${GENOOGLE_HOME}/genoogle.jar:${GENOOGLE_HOME}/lib/*:${GENOOGLE_HOME}/lib/jetty/*:${GENOOGLE_HOME}/lib/jetty/ext/* \
  bio.pih.genoogle.interfaces.WebServer \
  ${WEB_SERVICE_APP_DIR} ${WEB_SERVICE_PORT}
