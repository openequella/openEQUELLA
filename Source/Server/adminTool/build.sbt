libraryDependencies ++= Seq(
  "org.slf4j"              % "jcl-over-slf4j"  % "2.0.17",
  "org.slf4j"              % "slf4j-api"       % "2.0.17",
  "org.slf4j"              % "slf4j-simple"    % "2.0.17",
  "com.google.guava"       % "guava"           % "33.4.8-jre",
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.fifesoft"           % "rsyntaxtextarea" % "3.6.0",
  "com.miglayout"          % "miglayout-swing" % "11.4.2",
  springWeb,
  springAop,
  springContext
)

(run / fork) := true

(Compile / run / mainClass) := Some("com.tle.client.harness.ClientLauncher")
