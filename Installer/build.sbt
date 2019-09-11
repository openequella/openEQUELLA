import Path.relativeTo

libraryDependencies ++= Seq(
  "com.google.guava" % "guava"         % "18.0",
  "org.slf4j"        % "slf4j-simple"  % "1.7.28",
  "commons-codec"    % "commons-codec" % "1.12",
  postgresDep,
  sqlServerDep
)

excludeDependencies ++= Seq(
  "log4j"           % "log4j",
  "org.slf4j"       % "slf4j-log4j12",
  "commons-logging" % "commons-logging",
  "stax"            % "stax-api"
)

unmanagedJars in Compile ++= oracleDriverJar.value.toSeq.classpath

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

mainClass in assembly := Some("com.dytech.edge.installer.application.Launch")

lazy val equellaserver  = LocalProject("equellaserver")
lazy val upgradeManager = LocalProject("UpgradeManager")

installerZip := {
  val log            = streams.value.log
  val ver            = equellaVersion.value
  val dirname        = s"equella-installer-${ver.majorMinor}"
  val outZip         = target.value / s"$dirname.zip"
  val serverData     = baseDirectory.value / "data/server"
  val allServerFiles = serverData ** "*" pair (relativeTo(serverData), false)
  val upZip          = (upgradeZip in equellaserver).value
  val allFiles = Seq(
    assembly.value -> "enterprise-install.jar",
    upZip          -> s"manager/updates/${upZip.getName}"
  ) ++ allServerFiles
  log.info(s"Creating installer ${outZip.absolutePath}")
  IO.zip(allFiles.map(t => (t._1, s"$dirname/${t._2}")), outZip)
  outZip
}
