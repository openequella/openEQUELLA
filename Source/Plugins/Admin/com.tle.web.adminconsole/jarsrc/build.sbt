libraryDependencies ++= Seq(
  "com.github.equella.jpf" % "jpf"            % "1.0.7",
  "com.google.guava"       % "guava"          % "32.1.3-jre",
  "org.slf4j"              % "jcl-over-slf4j" % "2.0.17",
  "org.slf4j"              % "slf4j-simple"   % "2.0.17",
  springWeb,
  springAop,
  springContext,
  "com.fifesoft"  % "rsyntaxtextarea" % "1.5.2",
  "com.miglayout" % "miglayout-swing" % "4.2",
  xstreamDep
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging",
  // Spring 5 added a default logging bridge.  In oEQ, this results in
  // a [deduplicate: different file contents found in the following] error
  // ...org.slf4j/jcl-over-slf4j/jars/jcl-over-slf4j-1.7.30.jar:org/apache/commons/logging/Log.class
  // ...org.springframework/spring-jcl/jars/spring-jcl-5.3.23.jar:org/apache/commons/logging/Log.class
  // As per https://github.com/spring-projects/spring-framework/issues/20611 ,
  // since we already have logging in place, we can safely exclude the dep from spring.
  "org.springframework" % "spring-jcl"
)

(assembly / packageOptions) += Package.ManifestAttributes("Permissions" -> "all-permissions")
(assembly / assemblyOption) := (assembly / assemblyOption).value
(assembly / assemblyMergeStrategy) := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  // The following three were added when the hibernate-types was added the the hibernate module
  case PathList("javax", "activation", _*)       => MergeStrategy.first
  case PathList("javax", "xml", _*)              => MergeStrategy.first
  case PathList("META-INF", "versions", "9", _*) => MergeStrategy.first
  // Added due to a [deduplicate: different file contents found in the following] error against:
  // org.springframework/spring-context/jars/spring-context-3.2.18.RELEASE.jar:overview.html
  // org.springframework/spring-web/jars/spring-web-3.2.18.RELEASE.jar:overview.html
  case x if x.contains("overview.html") => MergeStrategy.first
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
dependsOn(platformCommon, platformSwing, platformEquella, LocalProject("com_tle_webstart_admin"))
