libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "dhfjava" % "dhfjava" % "1"
)

mainClass in assembly := Some("com.tle.conversion.Main")
