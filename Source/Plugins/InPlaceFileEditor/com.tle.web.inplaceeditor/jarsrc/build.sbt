libraryDependencies ++= Seq(
  "net.java.dev.jna" % "platform" % "3.5.1",
  "org.rococoa" % "rococoa-core" % "0.5",
  "com.google.guava" % "guava" % "18.0",
  "org.springframework" % "spring-web" % "2.5.5",
  "org.springframework" % "spring-aop" % "2.5.5"
)

packageOptions in assembly += Package.ManifestAttributes(
  "Application-Name" -> "EQUELLA In-place File Editor",
  "Permissions" -> "all-permissions",
  "Codebase" -> "*",
  "Application-Library-Allowable-Codebase" -> "*",
  "Caller-Allowable-Codebase" -> "*"
)
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

dependsOn(platformSwing, LocalProject("com_tle_common_entity"),
  LocalProject("com_tle_common_applet"), LocalProject("com_tle_common_inplaceeditor"))