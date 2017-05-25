libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0",
  "flamingo" % "flamingo" % "1.0",
  "com.miglayout" % "miglayout-swing" % "4.2"
)
dependsOn(platformSwing, LocalProject("com_tle_common_applet"), LocalProject("com_tle_applet_filemanager"))

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
