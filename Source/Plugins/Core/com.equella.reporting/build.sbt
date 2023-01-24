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

ivyConfigurations := overrideConfigs(Birt, CustomCompile)(ivyConfigurations.value)

// This setting should include all the managed JARs and unmanaged JARs extracted from zip file
// birt-report-framework.
jpfLibraryJars := {
  val unmanagedJarBase = baseDirectory.value / "lib"

  // Copy managed Jars to unmanaged Jar base.
  val updateReport = update.value
  val managedJars: Seq[File] =
    Classpaths.managedJars(Birt, Set("jar"), updateReport).files.filter(_.getName.endsWith(".jar"))
  IO.copy(managedJars.pair(flat(unmanagedJarBase), errorIfNone = false))

  IO.unzip(updateReport.select(artifact = artifactFilter(name = "birt-report-framework")).head,
           unmanagedJarBase)

  (unmanagedJarBase ** "*.jar").classpath
}
