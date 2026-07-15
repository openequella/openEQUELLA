val guiceVersion = "5.1.0"
val slf4jVersion = "2.0.18"
val sttpVersion  = "3.11.0"

libraryDependencies ++= Seq(
  // Logging dependencies
  "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
  "org.slf4j" % "slf4j-api"      % slf4jVersion,
  log4jSlf4jImpl,
  // (support YAML based logging configuration)
  jacksonDataBind,
  jacksonDataFormatYaml,
  // General dependencies
  "com.google.guava"       % "guava"           % "33.6.0-jre",
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.fifesoft"           % "rsyntaxtextarea" % "3.6.3",
  "com.miglayout"          % "miglayout-swing" % "11.4.3",
  springWeb,
  springAop,
  springContext,
  "io.github.openequella" %% "graphql-client" % "0.13.0-SNAPSHOT",
  "com.google.inject"      % "guice"          % guiceVersion excludeAll (
    // Due to deduplicates with aopalliance via Spring AOP.
    // Maybe it can be removed when all HTTP Invoker code is gone
    ExclusionRule(
      organization = "aopalliance",
      name = "aopalliance"
    )
  ),
  // STTP for REST calls, ideally match the version with the transitive from graphql-client
  "com.softwaremill.sttp.client3" %% "core"  % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
  // Circe generic for decoding REST JSON responses
  "io.circe" %% "circe-generic" % "0.14.15"
)

(run / fork) := true

(Compile / run / mainClass) := Some("com.tle.client.harness.ClientLauncher")
