import Path.rebase
import sbt.IO

name := "IntegTester"

version := "1.0"

val CirceVersion  = "0.12.1"
val Http4sVersion = "0.21.8"
val jsoupVersion  = "1.16.2"

scalaVersion := "2.13.12"

excludeDependencies ++= Seq("org.typelevel" % "scala-library")

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % CirceVersion)

libraryDependencies ++= Seq(
  "org.http4s"       %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"       %% "http4s-dsl"          % Http4sVersion,
  "org.http4s"       %% "http4s-circe"        % Http4sVersion,
  "org.slf4j"        % "slf4j-simple"         % "2.0.9",
  "org.jsoup"        % "jsoup"                % jsoupVersion,
  "com.nulab-inc"    %% "scala-oauth2-core"   % "1.6.0",
  "javax.servlet"    % "javax.servlet-api"    % "4.0.1",
  "com.google.guava" % "guava"                % "32.1.3-jre",
  jacksonDataBind,
  jacksonModuleScala
)

(Compile / resourceGenerators) += Def.task {
  val baseJs = baseDirectory.value / "front-end"
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
