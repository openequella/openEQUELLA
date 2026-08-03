addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.10.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.1.0")
// Update JGit so as to support git worktrees. The bundled version in sbt-git is limited to
// their desire to support Java 8 still. But we're on JDK 21 so we can just force a newer JGit.
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "7.7.0.202606012155-r"

addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.1.1")

addSbtPlugin("com.github.sbt.junit" % "sbt-jupiter-interface" % "0.19.0")

// Provides access to the OWASP Dependency Check to search for
// vulnerabilities in our dependencies. Most useful:
// - ./sbt dependencyCheckAnyProject
// Recommended: run with an NVD API key (via the NVD_API_KEY env var, read in the root build.sbt)
// to avoid rate limiting, and allow more retries since Maven Central
// can be flaky to access:
// - NVD_API_KEY="your API key" ./sbt -Danalyzer.central.retry.count=7 dependencyCheck
//
// NOTE: Uses a lot of temporary file storage, you may need to:
//   export JVM_OPTS="-Djava.io.tmpdir=/var/tmp"
addSbtPlugin("net.nmoncho" % "sbt-dependency-check" % "1.8.5")

// Provides support for all the tasks found at:
// https://github.com/sbt/sbt-dependency-graph#main-tasks
// Especially
// - dependencyTree
// - whatDependsOn <organization> <module> <revision>
//    - revision is optional
addDependencyTreePlugin

val circeVersion = "0.14.16"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion
)

val axis2Version = "2.0.1"
libraryDependencies ++= Seq(
  "org.apache.axis2" % "axis2-kernel"      % axis2Version,
  "org.apache.axis2" % "axis2-java2wsdl"   % axis2Version,
  "org.apache.axis2" % "axis2-adb"         % axis2Version,
  "org.apache.axis2" % "axis2-adb-codegen" % axis2Version,
  "org.apache.axis2" % "axis2-codegen"     % axis2Version,
  "org.apache.axis2" % "axis2-xmlbeans"    % axis2Version
)

libraryDependencies ++= Seq(
  "com.typesafe"           % "config"                % "1.4.9",
  "org.jacoco"             % "org.jacoco.report"     % "0.8.15",
  "org.jdom"               % "jdom2"                 % "2.0.6.1",
  "commons-logging"        % "commons-logging"       % "1.4.0",
  "commons-discovery"      % "commons-discovery"     % "0.5",
  "commons-configuration"  % "commons-configuration" % "1.10",
  "commons-beanutils"      % "commons-beanutils"     % "1.11.0",
  "commons-codec"          % "commons-codec"         % "1.22.1",
  "org.slf4j"              % "slf4j-nop"             % "2.0.18",
  "com.yahoo.platform.yui" % "yuicompressor"         % "2.4.8"
)
