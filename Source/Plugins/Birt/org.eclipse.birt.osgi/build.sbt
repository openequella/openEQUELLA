import Path.flat

lazy val BirtOsgi      = config("birtosgi")
lazy val CustomCompile = config("compile") extend BirtOsgi

libraryDependencies := Seq(
  "com.github.openequella" % "birt-osgi" % "4.9.1" artifacts Artifact("birt-osgi",
                                                                      Artifact.DefaultType,
                                                                      "zip"),
  "com.github.equella.reporting" % "reporting-common"                               % "6.5",
  "com.github.equella.reporting" % "reporting-oda"                                  % "6.5",
  "com.github.equella.reporting" % "reporting-oda-connectors"                       % "6.5",
  "org.apache.commons"           % "com.springsource.org.apache.commons.httpclient" % "3.1.0",
  "org.apache.commons"           % "com.springsource.org.apache.commons.logging"    % "1.1.1",
  "org.apache.commons"           % "com.springsource.org.apache.commons.codec"      % "1.6.0",
  xstreamDep,
  "javax.xml.stream" % "com.springsource.javax.xml.stream" % "1.0.1"
).map(_ % BirtOsgi)

resolvers += Resolver.url("my-test-repo",
                          url("https://repository.springsource.com/ivy/bundles/external/"))(
  Patterns(false, "[organisation]/[module]/[revision]/[artifact]-[revision].[ext]"))

ivyConfigurations := overrideConfigs(BirtOsgi, CustomCompile)(ivyConfigurations.value)

(Compile / resourceGenerators) += Def.task {
  val baseDir  = (Compile / resourceManaged).value
  val baseBirt = baseDir / "ReportEngine"
  IO.delete(baseBirt)
  val outPlugins = baseBirt / "lib"
  val ur         = update.value
  val pluginJars =
    Classpaths.managedJars(BirtOsgi, Set("jar"), ur).files.filter(_.getName.endsWith(".jar"))
  val unzipped       = IO.unzip(ur.select(artifact = artifactFilter(extension = "zip")).head, baseBirt)
  val copied         = IO.copy(pluginJars.pair(flat(outPlugins), errorIfNone = false))
  val birtManifest   = baseDirectory.value / "birt-MANIFEST.MF"
  val manifestPlugin = outPlugins / "org.eclipse.birt.api.jar"
  IO.zip(Seq(birtManifest -> "META-INF/MANIFEST.MF"),
         manifestPlugin,
         Option((ThisBuild / buildTimestamp).value))
  unzipped.toSeq ++ copied :+ manifestPlugin
}.taskValue
