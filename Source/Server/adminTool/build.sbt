val springVersion = "5.3.20"

libraryDependencies ++= Seq(
  "org.slf4j"              % "jcl-over-slf4j"  % "1.7.36",
  "org.slf4j"              % "slf4j-api"       % "1.7.36",
  "org.slf4j"              % "slf4j-simple"    % "1.7.36",
  "com.google.guava"       % "guava"           % "31.1-jre",
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.fifesoft"           % "rsyntaxtextarea" % "1.5.2",
  "com.miglayout"          % "miglayout-swing" % "4.2",
  "org.springframework"    % "spring-web"      % springVersion,
  "org.springframework"    % "spring-aop"      % springVersion,
  "org.springframework"    % "spring-context"  % springVersion
)

(run / fork) := true

(Compile / run / mainClass) := Some("com.tle.client.harness.ClientLauncher")
