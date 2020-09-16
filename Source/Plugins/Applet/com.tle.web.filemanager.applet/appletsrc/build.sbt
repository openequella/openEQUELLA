val springVersion = "5.2.9.RELEASE"

libraryDependencies ++= Seq(
  "com.google.guava"         % "guava"           % "18.0",
  "com.github.insubstantial" % "flamingo"        % "7.3",
  "com.miglayout"            % "miglayout-swing" % "4.2",
  "org.springframework"      % "spring-web"      % springVersion,
  "org.springframework"      % "spring-aop"      % springVersion
)

dependsOn(platformSwing, LocalProject("com_tle_common_applet"))

packageOptions in assembly += Package.ManifestAttributes(
  "Application-Name"                       -> "EQUELLA File Manager",
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
