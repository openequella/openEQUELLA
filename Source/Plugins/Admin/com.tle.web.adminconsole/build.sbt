lazy val adminConsoleJar = (project in file("jarsrc")).settings(
  libraryDependencies ++= Seq(
    "jpf" % "jpf" % "1.0.7",
    "com.google.guava" % "guava" % "18.0",
    "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "org.springframework" % "spring-web" % "2.5.5",
    "org.springframework" % "spring-aop" % "2.5.5",
    "com.fifesoft" % "rsyntaxtextarea" % "1.5.2",
    "com.miglayout" % "miglayout-swing" % "4.2",
    xstreamDep
  ),
  excludeDependencies += "commons-logging" % "commons-logging",
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
).dependsOn(platformCommon, platformSwing, platformEquella, LocalProject("com_tle_webstart_admin"))

resourceGenerators in Compile += Def.task {
  val outJar = (resourceManaged in Compile).value / "web/adminconsole.jar"
  val jarFile = (assembly in adminConsoleJar).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue
