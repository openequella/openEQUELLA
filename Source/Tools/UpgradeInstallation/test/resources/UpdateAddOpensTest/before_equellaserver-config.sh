#export RUN_USER=equella
export JAVA_HOME="/home/ian/.sdkman/candidates/java/21.0.7-tem"
#export JMX_CONFIG="-Dcom.sun.management.jmxremote.port=8855 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#export HEAP_CONFIG="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=../"
export CLASSPATH="../learningedge-config:../server/equella-server.jar"
export JAVA_OPTS=" -Xrs -Xms96m -Xmx512m -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Djava.awt.headless=true -Djava.io.tmpdir=../server/temp -XX:MaxGCPauseMillis=500 -XX:NewRatio=3 -XX:GCTimeRatio=16 -XX:+DisableExplicitGC -XX:+UseG1GC -Dcom.sun.jndi.ldap.connect.pool.timeout=3000000 -Dcom.sun.jndi.ldap.connect.pool.maxsize=200 -Dcom.sun.jndi.ldap.connect.pool.prefsize=20 --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED --add-opens=java.naming/javax.naming.directory=ALL-UNNAMED --add-opens=java.naming/javax.naming.ldap=ALL-UNNAMED --add-opens=java.naming/javax.naming=ALL-UNNAMED"
