libraryDependencies ++= Seq(
  "com.google.guava" % "guava"          % "31.0.1-jre",
  "org.slf4j"        % "jcl-over-slf4j" % "1.7.33",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.1",
  "org.apache.logging.log4j" % "log4j"            % "2.17.1",
  xstreamDep,
  "commons-configuration" % "commons-configuration" % "1.10",
  "commons-io"            % "commons-io"            % "2.11.0",
  "commons-lang"          % "commons-lang"          % "2.6",
  // Need these two jackson deps to allow processing log4j yaml config files.
  "com.fasterxml.jackson.core"       % "jackson-databind"        % "2.11.3",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.11.3"
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging"
)

(assembly / mainClass) := Some("com.tle.upgrade.UpgradeMain")
(assembly / assemblyOption) := (assembly / assemblyOption).value.copy(includeScala = false)

(assembly / assemblyMergeStrategy) := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  case "module-info.class"                  => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

val upgradeManager = LocalProject("UpgradeManager")

(Compile / resourceGenerators) += Def.task {
  val base = (Compile / resourceManaged).value
  val files = Seq(
    (upgradeManager / assembly).value -> base / "manager/manager.jar",
    versionProperties.value           -> base / "version.properties"
  )
  IO.copy(files)
  files.map(_._2)
}.taskValue
