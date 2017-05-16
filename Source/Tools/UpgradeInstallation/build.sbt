libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.jvnet.hudson" % "xstream" % "1.3.1-hudson-8",
  "commons-configuration" % "commons-configuration" % "1.9",
  "commons-io" % "commons-io" % "2.4",
  "commons-lang" % "commons-lang" % "2.6"
)

excludeDependencies ++= Seq(
  "commons-logging" % "commons-logging"
)

mainClass in assembly := Some("com.tle.upgrade.UpgradeMain")

