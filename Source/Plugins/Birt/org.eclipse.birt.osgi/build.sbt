import Path.flat

lazy val BirtOsgi      = config("birtosgi")
lazy val CustomCompile = config("compile") extend BirtOsgi

libraryDependencies := Seq(
  "com.github.openequella" % "birt-framework" % "4.6.0.20160607212201" artifacts Artifact(
    "birt-framework",
    "zip"),
  "com.github.openequella"   % "reporting-common"                               % "2020.2.0.2020052905",
  "com.github.openequella"   % "reporting-oda"                                  % "2020.2.0.2020052905",
  "com.github.openequella"   % "reporting-oda-connectors"                       % "2020.2.0.2020052905",
  "org.apache.commons"       % "com.springsource.org.apache.commons.httpclient" % "3.1.0",
  "org.apache.commons"       % "com.springsource.org.apache.commons.logging"    % "1.1.1",
  "org.apache.commons"       % "com.springsource.org.apache.commons.codec"      % "1.6.0",
  "com.thoughtworks.xstream" % "com.springsource.com.thoughtworks.xstream"      % "1.3.1",
  "org.xmlpull"              % "com.springsource.org.xmlpull"                   % "1.1.4.c",
  "javax.xml.stream"         % "com.springsource.javax.xml.stream"              % "1.0.1"
).map(_ % BirtOsgi)

ivyConfigurations := overrideConfigs(BirtOsgi, CustomCompile)(ivyConfigurations.value)

resourceGenerators in Compile += Def.task {
  val baseDir  = (resourceManaged in Compile).value
  val baseBirt = baseDir / "birt"
  IO.delete(baseBirt)
  val outPlugins = baseBirt / "plugins"
  val ur         = update.value
  val pluginJars =
    Classpaths
      .managedJars(BirtOsgi, Set("jar"), ur)
      .files
      .filter((file) => file.getName.endsWith(".jar") && !file.getName.endsWith("zip.jar"))
  val unzipped       = IO.unzip(ur.select(artifact = artifactFilter(classifier = "zip")).head, baseBirt)
  val copied         = IO.copy(pluginJars.pair(flat(outPlugins), errorIfNone = false))
  val birtManifest   = baseDirectory.value / "birt-MANIFEST.MF"
  val manifestPlugin = outPlugins / "org.eclipse.birt.api.jar"
  IO.zip(Seq(birtManifest -> "META-INF/MANIFEST.MF"), manifestPlugin)
  unzipped.toSeq ++ copied :+ manifestPlugin
}.taskValue
