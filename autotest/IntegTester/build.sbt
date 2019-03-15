import sbt.IO
import Path.rebase

import scala.sys.process.Process

name := "IntegTester"

version := "1.0"

val Http4sVersion = "0.18.14"
val CirceVersion  = "0.9.3"

scalaVersion := "2.12.6"
scalacOptions += "-Ypartial-unification"

excludeDependencies ++= Seq("org.typelevel" % "scala-library")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % CirceVersion)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  "org.slf4j"  % "slf4j-simple"         % "1.7.25"
)

resourceGenerators in Compile += Def.task {
  val baseJs = baseDirectory.value / "ps"
  val cached = FileFunction.cached(target.value / "pscache") { files =>
    Common.nodeInstall(baseJs)
    Common.nodeScript("build", baseJs)
    val outDir       = (resourceManaged in Compile).value
    val baseJsTarget = baseJs / "dist"
    IO.copy((baseJsTarget ** ("*.js" | "*.css" | "*.json")).pair(rebase(baseJsTarget, outDir)))
  }
  cached((baseJs / "src" ** "*").get.toSet).toSeq
}.taskValue
