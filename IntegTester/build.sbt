name := "IntegTester"

version := "1.0"

scalaVersion := "2.12.2"

val http4sVersion = "0.17.0-M3"

val circeVersion = "0.8.0"

excludeDependencies ++= Seq("org.typelevel" % "scala-library")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)

unmanagedResourceDirectories in Compile += baseDirectory.value / "ps/dist"