addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.2.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.1.1")

addSbtPlugin("com.etsy" % "sbt-checkstyle-plugin" % "3.1.1")
// Old version used because something else depends on an old JAWN
val circeVersion = "0.7.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "com.typesafe"           % "config"                % "1.3.4",
  "org.jacoco"             % "org.jacoco.report"     % "0.7.9",
  "org.jdom"               % "jdom2"                 % "2.0.6",
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
  "commons-beanutils"      % "commons-beanutils"     % "1.9.3",
  "commons-codec"          % "commons-codec"         % "1.12",
  "org.slf4j"              % "slf4j-nop"             % "1.7.28",
  "com.yahoo.platform.yui" % "yuicompressor"         % "2.4.8"
)
dependencyOverrides += "com.puppycrawl.tools" % "checkstyle" % "8.22"
