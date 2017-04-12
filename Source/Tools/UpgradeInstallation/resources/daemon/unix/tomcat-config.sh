#export RUN_USER=equella

# echo "Running tomcat config from directory ${PWD}, it seems ...(or `pwd`)"

export JAVA_HOME="/usr/lib/jvm/java-6-sun-1.6.0.26"
export CLASSPATH="../tomcat/bin/bootstrap.jar:../learningedge-config"

export JAVA_OPTS="-Djava.endorsed.dirs=../common/endorsed -Dcatalina.base=../tomcat -Dcatalina.home=../tomcat -Djava.io.tmpdir=../tomcat/temp -Xrs -Xms96m -Xmx512m -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Djava.awt.headless=true -XX:MaxPermSize=256m -XX:MaxGCPauseMillis=500 -XX:NewRatio=3 -XX:GCTimeRatio=16 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=70"

# echo "exporting javaopts as $JAVA_OPTS"
# echo "export classpath as $CLASSPATH"

