libraryDependencies ++= Seq(
  "org.slf4j"       % "slf4j-api"    % "1.7.28",
  "org.slf4j"       % "slf4j-simple" % "1.7.28",
  "org.apache.tika" % "tika-core"    % "1.14",
  "org.apache.tika" % "tika-parsers" % "1.14"
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
