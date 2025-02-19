lazy val Serial = config("serial") extend Test

configs(Serial)

dependsOn(LocalProject("IntegTester"), LocalProject("config"))

inConfig(Serial)(Defaults.testTasks)

val circeVersion  = "0.12.1"
val http4sVersion = "0.21.8"
val catsVersion   = "2.10.0"
val cxfVersion    = "3.6.5"

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.scala-lang"            % "scala-reflect"            % scalaVersion.value,
  "javax.jws"                 % "javax.jws-api"            % "1.1",
  "org.apache.commons"        % "commons-lang3"            % "3.14.0",
  "org.seleniumhq.selenium"   % "selenium-java"            % "4.26.0",
  "org.easytesting"           % "fest-util"                % "1.2.5",
  "org.easytesting"           % "fest-swing"               % "1.2.1",
  "xalan"                     % "xalan"                    % "2.7.3",
  "xalan"                     % "serializer"               % "2.7.3",
  "org.apache.cxf"            % "cxf-rt-frontend-simple"   % cxfVersion,
  "org.apache.cxf"            % "cxf-rt-databinding-aegis" % cxfVersion,
  "org.apache.cxf"            % "cxf-rt-transports-http"   % cxfVersion,
  "org.apache.httpcomponents" % "httpclient"               % "4.5.14",
  "com.jcraft"                % "jsch"                     % "0.1.55",
  "org.jacoco"                % "org.jacoco.report"        % "0.8.12",
  "org.dspace"                % "oclc-harvester2"          % "1.0.0",
  "com.typesafe"              % "config"                   % "1.4.3",
  "org.slf4j"                 % "slf4j-simple"             % "2.0.13",
  "org.scalacheck"           %% "scalacheck"               % "1.17.0" % "test,serial",
  "org.http4s"               %% "http4s-async-http-client" % http4sVersion,
  "org.http4s"               %% "http4s-blaze-client"      % http4sVersion,
  "org.http4s"               %% "http4s-circe"             % http4sVersion,
  "org.typelevel"            %% "cats-free"                % catsVersion,
  "com.unboundid"             % "unboundid-ldapsdk"        % "6.0.11",
  jacksonDataBind,
  "com.auth0" % "jwks-rsa" % "0.22.1"
)

(Compile / unmanagedBase) := baseDirectory.value / "lib/adminjars"

def serialFilter(name: String): Boolean = {
  name endsWith "PropertiesSerial"
}
def stdFilter(name: String): Boolean = {
  (name endsWith "Properties") && !serialFilter(name)
}

val commonOptions = Seq(
  sbt.Tests.Argument(TestFrameworks.ScalaCheck, "-s", "1")
)
(Serial / testOptions) := commonOptions

(Test / testOptions) := commonOptions

(Test / testOptions) += sbt.Tests.Filter(stdFilter)

(Serial / testOptions) += sbt.Tests.Filter(serialFilter)

(Serial / parallelExecution) := false

(Test / parallelExecution) := autotestBuildConfig.value.getBoolean("tests.parallel")
