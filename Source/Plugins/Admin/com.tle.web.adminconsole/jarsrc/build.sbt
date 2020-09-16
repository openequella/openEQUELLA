val springVersion = "5.2.9.RELEASE"

libraryDependencies ++= Seq(
  "com.github.equella.jpf" % "jpf"             % "1.0.7",
  "com.google.guava"       % "guava"           % "18.0",
  "org.slf4j"              % "jcl-over-slf4j"  % "1.7.30",
  "org.slf4j"              % "slf4j-simple"    % "1.7.30",
  "org.springframework"    % "spring-web"      % springVersion,
  "org.springframework"    % "spring-aop"      % springVersion,
  "com.fifesoft"           % "rsyntaxtextarea" % "1.5.2",
  "com.miglayout"          % "miglayout-swing" % "4.2",
  xstreamDep
)

excludeDependencies += "commons-logging" % "commons-logging"
packageOptions in assembly += Package.ManifestAttributes("Permissions" -> "all-permissions")
assemblyOption in assembly := (assemblyOption in assembly).value
assemblyMergeStrategy in assembly := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  // Added due to a [deduplicate: different file contents found in the following] error against:
  // org.springframework/spring-context/jars/spring-context-3.2.18.RELEASE.jar:overview.html
  // org.springframework/spring-web/jars/spring-web-3.2.18.RELEASE.jar:overview.html
  case x if x.contains("overview.html") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
dependsOn(platformCommon, platformSwing, platformEquella, LocalProject("com_tle_webstart_admin"))
