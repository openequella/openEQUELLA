libraryDependencies ++= Seq(
  "org.slf4j"              % "jcl-over-slf4j"  % "2.0.9",
  "org.slf4j"              % "slf4j-api"       % "2.0.9",
  "org.slf4j"              % "slf4j-simple"    % "2.0.9",
  "com.google.guava"       % "guava"           % "32.1.2-jre",
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.fifesoft"           % "rsyntaxtextarea" % "1.5.2",
  "com.miglayout"          % "miglayout-swing" % "4.2",
  springWeb,
  springAop,
  springContext
)

(run / fork) := true

(Compile / run / mainClass) := Some("com.tle.client.harness.ClientLauncher")
