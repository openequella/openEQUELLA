#export RUN_USER=equella
# echo "Running manager config from directory ${PWD}, as it happens...( or `pwd`)"

export JAVA_HOME="/usr/lib/jvm/java-6-sun-1.6.0.26"
export CLASSPATH="./manager-rd6cf621.jar"
export JAVA_OPTS="-XX:MaxPermSize=256m -XX:MaxGCPauseMillis=500 -XX:NewRatio=3 -XX:GCTimeRatio=16 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=70"

# echo "exporting classpath as $CLASSPATH, it seems"
