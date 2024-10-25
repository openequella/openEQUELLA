name := "graphql-client"

ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.openequella"
ThisBuild / organizationName := "openEQUELLA GraphQL Client"

lazy val root = (project in file("."))
  .settings(
    name := "graphql-client"
  )

libraryDependencies ++= Seq(
  "com.github.ghostdogpr"         %% "caliban-client" % "2.5.1",
  "com.softwaremill.sttp.client3" %% "zio"            % "3.9.7",
  // Add Scala Test
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  "org.scalatest"  %% "scalatest"      % "3.2.19" % Test,
)

enablePlugins(CalibanPlugin)
