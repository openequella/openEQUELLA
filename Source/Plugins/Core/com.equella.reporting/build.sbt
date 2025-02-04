import sbt.io.Path.flat

lazy val Birt          = config("birt")
lazy val CustomCompile = config("compile") extend Birt

libraryDependencies ++= Seq(
  "com.github.openequella" % "birt-report-framework" % "4.9.2" artifacts Artifact(
    "birt-report-framework",
    Artifact.DefaultType,
    "zip"
  ),
  "com.github.openequella" % "birt-osgi" % "4.9.2" artifacts Artifact(
    "birt-osgi",
    Artifact.DefaultType,
    "zip"
  ),
  "com.github.equella.reporting" % "reporting-common"         % "6.5",
  "com.github.equella.reporting" % "reporting-oda"            % "6.5",
  "com.github.equella.reporting" % "reporting-oda-connectors" % "6.5",
  xstreamDep
).map(_ % Birt)

ivyConfigurations := overrideConfigs(Birt, CustomCompile)(ivyConfigurations.value)

// This setting should include all the managed JARs and unmanaged JARs extracted from zip file
// birt-report-framework and birt-osgi.
jpfLibraryJars := {
  val updateReport     = update.value
  val unmanagedJarBase = baseDirectory.value / "lib"
  val osgiBaseDir      = (Compile / resourceManaged).value / "ReportEngine"

  // Copy managed Jars to the unmanaged Jar base.
  def copyManagedJars: Set[File] = {
    val managedJars: Seq[File] =
      Classpaths
        .managedJars(Birt, Set("jar"), updateReport)
        .files
        .filter(_.getName.endsWith(".jar"))
    IO.copy(managedJars.pair(flat(unmanagedJarBase), errorIfNone = false))
  }

  // Unzip framework to the unmanaged Jar base.
  def unzipFramework: Set[File] = {
    val framework =
      updateReport.select(artifact = artifactFilter(name = "birt-report-framework")).head
    IO.unzip(framework, unmanagedJarBase)
  }

  // Unzip osgi to the managed resources directory and classes directory, which will achieve
  // the same result as `Compile / resourceGenerators`.
  def unzipOSGI(): Unit = {
    val osgi = updateReport.select(artifact = artifactFilter(name = "birt-osgi")).head
    Array(osgiBaseDir, (Compile / classDirectory).value / "ReportEngine").foreach(IO.unzip(osgi, _))
  }

  // Copy OSGI Jars to unmanaged Jar base, excluding Jars that cause dependency conflicts.
  def copyOSGIToUnmanagedJars: Set[File] = {
    def exclusionRule(file: File) =
      List(
        "javax.xml_1.3.4.v201005080400.jar",
        "js.jar",
        "BirtSample.jar",
        "sampledb.jar",
        "guava-r09.jar"
      ).contains(file.getName)

    val fileFilter = "*.jar" -- new SimpleFileFilter(exclusionRule)
    val jars       = osgiBaseDir / "platform" / "plugins" ** fileFilter

    IO.copy(jars.pair(flat(unmanagedJarBase), errorIfNone = false))
  }

  copyManagedJars
  unzipFramework
  unzipOSGI()
  copyOSGIToUnmanagedJars

  // Return all the Jars in the unmanaged Jar base.
  (unmanagedJarBase ** "*.jar").classpath
}
