addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.2")

addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.1.1")

addSbtPlugin("com.etsy" % "sbt-checkstyle-plugin" % "3.1.1")

// Provides access to the OWASP Dependency Check to search for
// vulnerabilities in our dependencies. Most useful:
// - ./sbt dependencyCheckAnyProject
//
// NOTE: Uses a lot of temporary file storage, you may need to:
//   export JVM_OPTS="-Djava.io.tmpdir=/var/tmp"
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "3.3.0")

// Provides support for all the tasks found at:
// https://github.com/sbt/sbt-dependency-graph#main-tasks
// Especially
// - dependencyTree
// - whatDependsOn <organization> <module> <revision>
//    - revision is optional
addDependencyTreePlugin

// Old version used because something else depends on an old JAWN
val circeVersion = "0.7.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "com.typesafe"           % "config"                % "1.4.1",
  "org.jacoco"             % "org.jacoco.report"     % "0.8.7",
  "org.jdom"               % "jdom2"                 % "2.0.6.1",
  "org.apache.axis2"       % "axis2-kernel"          % "1.6.2",
  "org.apache.axis2"       % "axis2-java2wsdl"       % "1.6.2",
  "org.apache.axis2"       % "axis2-adb"             % "1.6.2",
  "org.apache.axis2"       % "axis2-jaxbri"          % "1.6.2",
  "org.apache.axis2"       % "axis2-adb-codegen"     % "1.6.2",
  "org.apache.axis2"       % "axis2-codegen"         % "1.6.2",
  "org.apache.axis2"       % "axis2-xmlbeans"        % "1.6.2",
  "commons-logging"        % "commons-logging"       % "1.2",
  "commons-discovery"      % "commons-discovery"     % "0.5",
  "commons-configuration"  % "commons-configuration" % "1.10",
  "commons-beanutils"      % "commons-beanutils"     % "1.9.4",
  "commons-codec"          % "commons-codec"         % "1.15",
  "org.slf4j"              % "slf4j-nop"             % "1.7.33",
  "com.yahoo.platform.yui" % "yuicompressor"         % "2.4.8"
)
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "9.2.1"
