rem Java options
set JAVA_ARGS=-Djava.endorsed.dirs=../common/endorsed;-Dcatalina.base=../tomcat;-Dcatalina.home=../tomcat;-Djava.io.tmpdir=../tomcat/temp;-Xrs;-Xms96m;-Xmx512m;-Djava.net.preferIPv4Stack=true;-Djava.net.preferIPv6Addresses=false;-Djava.awt.headless=true;-Demma.rt.control=false
rem Class path
set CLASS_PATH=../tomcat/bin/bootstrap.jar;../tomcat/bin/tomcat-juli.jar;../learningedge-config
rem Display name for the windows service
set DISPLAY_NAME=Equella App Server
rem Service name cannot contain spaces
set SERVICE_NAME=EquellaAppServer
rem Start type can me auto or manual
set START_TYPE=auto
rem Path to Java installation
set JAVA_HOME=c:/Program Files/Java/jdk1.6.0_25
rem Run this server as a specific user
rem remove "rem" from the following lines to enable
rem set SERVICE_USER=.\equella
rem set SERVICE_PASSWORD=password
