import sbt.IO
import Path.rebase

import scala.sys.process.Process

name := "IntegTester"

version := "1.0"

val CirceVersion  = "0.12.1"
val Http4sVersion = "0.21.8"
val jsoupVersion  = "1.14.1"

scalaVersion := "2.12.14"
scalacOptions += "-Ypartial-unification"

excludeDependencies ++= Seq("org.typelevel" % "scala-library")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % CirceVersion)

libraryDependencies ++= Seq(
  "org.http4s"    %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"    %% "http4s-dsl"          % Http4sVersion,
  "org.http4s"    %% "http4s-circe"        % Http4sVersion,
  "org.slf4j"     % "slf4j-simple"         % "1.7.32",
  "org.jsoup"     % "jsoup"                % jsoupVersion,
  "com.nulab-inc" %% "scala-oauth2-core"   % "1.5.0"
)

(Compile / resourceGenerators) += Def.task {
  val baseJs = baseDirectory.value / "ps"
  val cached = FileFunction.cached(target.value / "pscache") { files =>
    Common.nodeInstall(baseJs)
    Common.nodeScript("build", baseJs)
    val outDir       = (Compile / resourceManaged).value / "www"
    val baseJsTarget = baseJs / "target/www"
    IO.copy((baseJsTarget ** "*").pair(rebase(baseJsTarget, outDir)))
  }
  cached(
    ((baseJs / "src" ** "*").get ++ (baseJs / "tsrc" ** "*").get ++ (baseJs / "www" ** "*").get).toSet).toSeq
}.taskValue
