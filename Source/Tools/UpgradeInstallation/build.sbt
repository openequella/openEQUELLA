val jacksonVersion = "2.11.3"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava"          % "31.0.1-jre",
  "org.slf4j"        % "jcl-over-slf4j" % "1.7.33",
  log4j,
  log4jSlf4jImpl,
  "org.typelevel"            %% "cats-core"       % "2.7.0",
  xstreamDep,
  "commons-configuration" % "commons-configuration" % "1.10",
  "commons-io"            % "commons-io"            % "2.11.0",
  "commons-lang"          % "commons-lang"          % "2.6",
  // Need these two jackson deps to allow processing log4j yaml config files.
  "com.fasterxml.jackson.core"       % "jackson-databind"        % jacksonVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion,
  "com.fasterxml.jackson.module"     %% "jackson-module-scala"   % jacksonVersion
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging"
)

(assembly / mainClass) := Some("com.tle.upgrade.UpgradeMain")
(assembly / assemblyOption) := (assembly / assemblyOption).value.withIncludeScala(true)

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
