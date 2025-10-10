rem Java options
rem set HEAP_CONFIG=-XX:+HeapDumpOnOutOfMemoryError;-XX:HeapDumpPath=../;
rem set JMX_CONFIG=-Dcom.sun.management.jmxremote.port=8855;-Dcom.sun.management.jmxremote.authenticate=false;-Dcom.sun.management.jmxremote.ssl=false;
set JAVA_ADDOPENS=^
--add-opens=java.base/java.io=ALL-UNNAMED;^
--add-opens=java.base/java.lang.ref=ALL-UNNAMED;^
--add-opens=java.base/java.lang=ALL-UNNAMED;^
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED;^
--add-opens=java.base/java.util=ALL-UNNAMED;^
--add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED;^
--add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED;^
--add-opens=java.naming/javax.naming.directory=ALL-UNNAMED;^
--add-opens=java.naming/javax.naming.ldap=ALL-UNNAMED;^
--add-opens=java.naming/javax.naming=ALL-UNNAMED

set JAVA_ARGS=-Xms96m;-Xmx512m;-Djava.net.preferIPv4Stack=true;-Djava.net.preferIPv6Addresses=false;-Djava.awt.headless=true;-Djava.io.tmpdir=../server/temp;-XX:MaxGCPauseMillis=500;-XX:NewRatio=3;-XX:GCTimeRatio=16;-XX:+DisableExplicitGC;-XX:+UseG1GC;-Dcom.sun.jndi.ldap.connect.pool.timeout=3000000;-Dcom.sun.jndi.ldap.connect.pool.maxsize=200;-Dcom.sun.jndi.ldap.connect.pool.prefsize=20;%JAVA_ADDOPENS%
rem Class path
set CLASS_PATH=../learningedge-config;../server/equella-server.jar
rem Display name for the Windows service
set DISPLAY_NAME=EQUELLA App Server
rem Service name cannot contain spaces
set SERVICE_NAME=EQUELLA-AppServer
rem Start type can me auto or manual
set START_TYPE=auto
rem Path to Java installation
set JAVA_HOME=C:/Program Files/Eclipse Adoptium/jdk-21.0.8.9-hotspot/
rem Run this server as a specific user
rem remove "rem" from the following lines to enable
rem set SERVICE_USER=.\equella
rem set SERVICE_PASSWORD=password
