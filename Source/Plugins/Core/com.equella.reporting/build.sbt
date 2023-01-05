import sbt.io.Path.flat

lazy val Birt          = config("birt")
lazy val CustomCompile = config("compile") extend Birt

libraryDependencies ++= Seq(
  "com.github.openequella" % "birt-report-framework" % "4.9.1" artifacts Artifact(
    "birt-report-framework",
    Artifact.DefaultType,
    "zip"),
  "com.github.equella.reporting" % "reporting-common"         % "6.5",
  "com.github.equella.reporting" % "reporting-oda"            % "6.5",
  "com.github.equella.reporting" % "reporting-oda-connectors" % "6.5",
  "rhino"                        % "js"                       % "1.7R2"
).map(_ % Birt)

/**
  * Previously, we copied `birt-api.jar` to directory `jpflibs` by using `jpfLibraryJars`.
  * Now because we use the BIRT Report Framework which is a ZIP file of Jars, we need to
  * extract all the Jars to `jpflibs`. We also need to add these Jars to classpath by
  * executing task `unmanagedJars`.
  */
(Compile / unmanagedJars) ++= {
  val baseDir      = (Compile / target).value
  val jpf          = baseDir / "jpflibs"
  val updateReport = update.value
  val managedJars =
    Classpaths.managedJars(Birt, Set("jar"), updateReport).files.filter(_.getName.endsWith(".jar"))

  // Unzip 'birt-report-framework' to the JPF dir.
  IO.unzip(updateReport.select(artifact = artifactFilter(name = "birt-report-framework")).head, jpf)
  // Copy other managed JARs to the JPF dir.
  IO.copy(managedJars.pair(flat(jpf), errorIfNone = false))

  (jpf ** "*.jar").classpath
}

ivyConfigurations := overrideConfigs(Birt, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := (Compile / unmanagedJars).value
