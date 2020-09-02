val tikaVersion = "1.24.1"

libraryDependencies ++= Seq(
  "org.slf4j"       % "slf4j-api"    % "1.7.30",
  "org.slf4j"       % "slf4j-simple" % "1.7.30",
  "org.apache.tika" % "tika-core"    % tikaVersion,
  "org.apache.tika" % "tika-parsers" % tikaVersion
)

excludeDependencies += "commons-logging" % "commons-logging"
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "cxf", "bus-extensions.txt") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.tle.conversion.Main")
