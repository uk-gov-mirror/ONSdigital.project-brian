#!/bin/bash

export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8008,server=y,suspend=n"
export PORT="8083"

# Restolino configuration
export RESTOLINO_STATIC="src/main/resources/files"
export RESTOLINO_CLASSES="target/classes"
export PACKAGE_PREFIX=com.github.onsdigital.brian.api

#Development: reloadable
mvn test dependency:copy-dependencies  && \

mvn package && \
java $JAVA_OPTS \
 -Drestolino.packageprefix=$PACKAGE_PREFIX \
 -jar target/*-jar-with-dependencies.jar
