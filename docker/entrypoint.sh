#! /bin/bash

STANDARD_JVM_ARGS=$(grep "JAVA_OPTS" manager/equellaserver-config.sh  | awk -F '"' '{print $2}')
JVM_ARGS="$STANDARD_JVM_ARGS $JVM_ARGS"
echo $JVM_ARGS

java $JVM_ARGS -cp learningedge-config:server/equella-server.jar com.tle.core.equella.runner.EQUELLAServer

