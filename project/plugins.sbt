addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.0.3")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "org.jacoco" % "org.jacoco.report" % "0.7.9"
)
