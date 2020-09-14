val springVersion = "3.2.18.RELEASE"

libraryDependencies ++= Seq(
  "net.java.dev.jna"    % "platform"     % "3.5.2",
  "org.rococoa"         % "rococoa-core" % "0.5",
  "com.google.guava"    % "guava"        % "18.0",
  "org.springframework" % "spring-web"   % springVersion,
  "org.springframework" % "spring-aop"   % springVersion
)

packageOptions in assembly += Package.ManifestAttributes(
  "Application-Name"                       -> "EQUELLA In-place File Editor",
  "Permissions"                            -> "all-permissions",
  "Codebase"                               -> "*",
  "Application-Library-Allowable-Codebase" -> "*",
  "Caller-Allowable-Codebase"              -> "*"
)
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// Added due to a [deduplicate: different file contents found in the following] error against:
// org.springframework/spring-context/jars/spring-context-3.2.18.RELEASE.jar:overview.html
// org.springframework/spring-web/jars/spring-web-3.2.18.RELEASE.jar:overview.html
assemblyMergeStrategy in assembly := {
  case x if x.contains("overview.html") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

dependsOn(platformSwing,
          LocalProject("com_equella_base"),
          LocalProject("com_tle_common_applet"),
          LocalProject("com_tle_common_inplaceeditor"))
