libraryDependencies ++= Seq(
  "com.google.guava" % "guava"          % "32.1.3-jre",
  "org.slf4j"        % "jcl-over-slf4j" % "2.0.17",
  log4j,
  log4jCore,
  log4jSlf4jImpl,
  "org.typelevel" %% "cats-core" % "2.10.0",
  xstreamDep,
  "commons-configuration" % "commons-configuration" % "1.10",
  "commons-io"            % "commons-io"            % "2.16.1",
  "commons-lang"          % "commons-lang"          % "2.6",
  // Need these two jackson deps to allow processing log4j yaml config files.
  jacksonDataBind,
  jacksonDataFormatYaml,
  jacksonModuleScala
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging"
)

(assembly / mainClass)      := Some("com.tle.upgrade.UpgradeMain")
(assembly / assemblyOption) := (assembly / assemblyOption).value.withIncludeScala(true)

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
