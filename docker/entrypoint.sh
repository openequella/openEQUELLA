#! /bin/bash

# Copy across the JVM arguments we use for standard installations, and then merge in custom ones
# provided in the JVM_ARGS environment variable - exposed in Dockerfile. There is another variable MEM
# in Dockerfile for easily controlling -Xmx on Dev cluster.
STANDARD_JVM_ARGS=$(grep "JAVA_OPTS" equellaserver-config.sh  | awk -F '"' '{print $2}')
JVM_ARGS="$STANDARD_JVM_ARGS $JVM_ARGS -Xmx${MEM}m"

echo Starting openEQUELLA with following JVM arguments:
echo $JVM_ARGS

java $JVM_ARGS -cp learningedge-config:server/equella-server.jar com.tle.core.equella.runner.EQUELLAServer
