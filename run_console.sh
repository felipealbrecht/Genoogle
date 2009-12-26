#!/bin/sh

# Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
# Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
#
# For further information check the LICENSE file.
#

# ----------------------------------------------------------------------------------------
# Script to start Genoogle with the console interface.
#
# Environment Variables:
#
#   GENOOGLE_HOME   Point at your Genoogle base directory.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#                   Required to run the with the "debug" or "javac" argument.
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

$JAVA_HOME/bin/java \
 -server \
 -Xms1024m \
 -Xmx3072m \
 -classpath ${GENOOGLE_HOME}/genoogle.jar:${GENOOGLE_HOME}/lib/* \
  bio.pih.genoogle.Genoogle $1 $2
