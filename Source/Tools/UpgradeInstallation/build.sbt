libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5",
  "log4j" % "log4j" % "1.2.17",
  xstreamDep,
  "commons-configuration" % "commons-configuration" % "1.9",
  "commons-io" % "commons-io" % "2.4",
  "commons-lang" % "commons-lang" % "2.6"
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging"
)

mainClass in assembly := Some("com.tle.upgrade.UpgradeMain")
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

val upgradeManager = LocalProject("UpgradeManager")

resourceGenerators in Compile += Def.task {
  val base = (resourceManaged in Compile).value
  val files = Seq(
    (assembly in upgradeManager).value -> base / "manager/manager.jar",
    versionProperties.value -> base / "version.properties"
  )
  IO.copy(files)
  files.map(_._2)
}.taskValue