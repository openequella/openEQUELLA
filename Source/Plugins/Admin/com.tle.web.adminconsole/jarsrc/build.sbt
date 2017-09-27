libraryDependencies ++= Seq(
  "com.github.equella.jpf" % "jpf" % "1.0.7",
  "com.google.guava" % "guava" % "18.0",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.springframework" % "spring-web" % "2.5.5",
  "org.springframework" % "spring-aop" % "2.5.5",
  "com.fifesoft" % "rsyntaxtextarea" % "1.5.2",
  "com.miglayout" % "miglayout-swing" % "4.2",
  xstreamDep
)

scalaVersion := "2.11.7"

excludeDependencies += "commons-logging" % "commons-logging"
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyMergeStrategy in assembly := {
  case PathList("org", "xmlpull", "v1", _*) => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
dependsOn(platformCommon, platformSwing, platformEquella, LocalProject("com_tle_webstart_admin"))