libraryDependencies ++= Seq(
  "com.google.guava" % "guava"          % "18.0",
  "org.slf4j"        % "jcl-over-slf4j" % "1.7.30",
  "org.slf4j"        % "slf4j-log4j12"  % "1.7.30",
  "log4j"            % "log4j"          % "1.2.17",
  xstreamDep,
  "commons-configuration" % "commons-configuration" % "1.10",
  "commons-io"            % "commons-io"            % "2.8.0",
  "commons-lang"          % "commons-lang"          % "2.6"
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging"
)

(assembly / mainClass) := Some("com.tle.upgrade.UpgradeMain")
(assembly / assemblyOption) := (assembly / assemblyOption).value.copy(includeScala = false)

(assembly / assemblyMergeStrategy) := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
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
