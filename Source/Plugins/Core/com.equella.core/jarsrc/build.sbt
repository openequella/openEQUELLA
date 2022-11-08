libraryDependencies ++= Seq(
  "net.java.dev.jna" % "platform"     % "3.5.2",
  "org.rococoa"      % "rococoa-core" % "0.5",
  "com.google.guava" % "guava"        % "31.1-jre",
  springWeb,
  springAop
)

(assembly / packageOptions) += Package.ManifestAttributes(
  "Application-Name"                       -> "EQUELLA In-place File Editor",
  "Permissions"                            -> "all-permissions",
  "Codebase"                               -> "*",
  "Application-Library-Allowable-Codebase" -> "*",
  "Caller-Allowable-Codebase"              -> "*"
)
(assembly / assemblyOption) := (assembly / assemblyOption).value.withIncludeScala(false)

(assembly / assemblyMergeStrategy) := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  // The following three were added when the hibernate-types was added the the hibernate module
  case PathList("javax", "activation", _*)       => MergeStrategy.first
  case PathList("javax", "xml", "bind", _*)      => MergeStrategy.first
  case PathList("META-INF", "versions", "9", _*) => MergeStrategy.first
  // Added due to a [deduplicate: different file contents found in the following] error against:
  // org.springframework/spring-context/jars/spring-context-3.2.18.RELEASE.jar:overview.html
  // org.springframework/spring-web/jars/spring-web-3.2.18.RELEASE.jar:overview.html
  case x if x.contains("overview.html") => MergeStrategy.first
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

dependsOn(platformSwing,
          LocalProject("com_equella_base"),
          LocalProject("com_tle_common_applet"),
          LocalProject("com_tle_common_inplaceeditor"))
