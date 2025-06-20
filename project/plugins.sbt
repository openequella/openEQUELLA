addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.1.1")

// Provides access to the OWASP Dependency Check to search for
// vulnerabilities in our dependencies. Most useful:
// - ./sbt dependencyCheckAnyProject
//
// NOTE: Uses a lot of temporary file storage, you may need to:
//   export JVM_OPTS="-Djava.io.tmpdir=/var/tmp"
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "5.1.0")

// Provides support for all the tasks found at:
// https://github.com/sbt/sbt-dependency-graph#main-tasks
// Especially
// - dependencyTree
// - whatDependsOn <organization> <module> <revision>
//    - revision is optional
addDependencyTreePlugin

val circeVersion = "0.14.14"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion
)

val axis2Version = "2.0.0"
libraryDependencies ++= Seq(
  "org.apache.axis2" % "axis2-kernel"      % axis2Version,
  "org.apache.axis2" % "axis2-java2wsdl"   % axis2Version,
  "org.apache.axis2" % "axis2-adb"         % axis2Version,
  "org.apache.axis2" % "axis2-adb-codegen" % axis2Version,
  "org.apache.axis2" % "axis2-codegen"     % axis2Version,
  "org.apache.axis2" % "axis2-xmlbeans"    % axis2Version
)

libraryDependencies ++= Seq(
  "com.typesafe"           % "config"                % "1.4.3",
  "org.jacoco"             % "org.jacoco.report"     % "0.8.13",
  "org.jdom"               % "jdom2"                 % "2.0.6.1",
  "commons-logging"        % "commons-logging"       % "1.3.5",
  "commons-discovery"      % "commons-discovery"     % "0.5",
  "commons-configuration"  % "commons-configuration" % "1.10",
  "commons-beanutils"      % "commons-beanutils"     % "1.11.0",
  "commons-codec"          % "commons-codec"         % "1.18.0",
  "org.slf4j"              % "slf4j-nop"             % "2.0.17",
  "com.yahoo.platform.yui" % "yuicompressor"         % "2.4.8"
)
