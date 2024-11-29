val guiceVersion = "5.1.0"
val slf4jVersion = "2.0.16"

libraryDependencies ++= Seq(
  // Logging dependencies
  "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
  "org.slf4j" % "slf4j-api"      % slf4jVersion,
  log4jSlf4jImpl,
  // (support YAML based logging configuration)
  jacksonDataBind,
  jacksonDataFormatYaml,
  // General dependencies
  "com.google.guava"       % "guava"           % "32.1.3-jre",
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.fifesoft"           % "rsyntaxtextarea" % "1.5.2",
  "com.miglayout"          % "miglayout-swing" % "4.2",
  springWeb,
  springAop,
  springContext,
  "io.github.openequella" %% "graphql-client" % "0.1.0-SNAPSHOT",
  "com.google.inject"     % "guice"           % guiceVersion
)

(run / fork) := true

(Compile / run / mainClass) := Some("com.tle.client.harness.ClientLauncher")
