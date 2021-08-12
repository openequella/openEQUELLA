import Path.relativeTo

libraryDependencies ++= Seq(
  "com.google.guava" % "guava"         % "18.0",
  "org.slf4j"        % "slf4j-simple"  % "1.7.32",
  "commons-codec"    % "commons-codec" % "1.15",
  postgresDep,
  sqlServerDep
)

libraryDependencies ++= {
  if (bundleOracleDriver.value) {
    oracleDriverMavenCoordinate.value
  } else {
    Seq.empty
  }
}

excludeDependencies ++= Seq(
  "log4j"           % "log4j",
  "org.slf4j"       % "slf4j-log4j12",
  "commons-logging" % "commons-logging",
  "stax"            % "stax-api"
)

(assembly / assemblyOption) := (assembly / assemblyOption).value.copy(includeScala = false)

(assembly / mainClass) := Some("com.dytech.edge.installer.application.Launch")

lazy val equellaserver  = LocalProject("equellaserver")
lazy val upgradeManager = LocalProject("UpgradeManager")

installerZip := {
  val log            = streams.value.log
  val ver            = equellaVersion.value
  val dirname        = s"equella-installer-${ver.semanticVersion}"
  val outZip         = target.value / s"$dirname.zip"
  val serverData     = baseDirectory.value / "data/server"
  val allServerFiles = serverData ** "*" pair (relativeTo(serverData), false)
  val upZip          = (equellaserver / upgradeZip).value
  val allFiles = Seq(
    assembly.value -> "enterprise-install.jar",
    upZip          -> s"manager/updates/${upZip.getName}"
  ) ++ allServerFiles
  log.info(s"Creating installer ${outZip.absolutePath}")
  IO.zip(allFiles.map(t => (t._1, s"$dirname/${t._2}")),
         outZip,
         Option((ThisBuild / buildTimestamp).value))
  outZip
}
