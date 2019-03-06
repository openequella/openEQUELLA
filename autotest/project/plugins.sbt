addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.1.1")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "org.jacoco" % "org.jacoco.report" % "0.7.9",
  "org.jdom" % "jdom2" % "2.0.6"
)
