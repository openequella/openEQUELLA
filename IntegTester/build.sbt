name := "IntegTester"

version := "1.0"

scalaVersion := "2.12.2"

val http4sVersion = "0.17.0-M1"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)