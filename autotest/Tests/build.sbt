lazy val Serial = config("serial") extend Test

configs(Serial)

dependsOn(LocalProject("IntegTester"), LocalProject("config"))

inConfig(Serial)(Defaults.testTasks)

val circeVersion  = "0.9.3"
val http4sVersion = "0.20.0-M6"
val catsVersion   = "1.1.0"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "javax.jws"                 % "javax.jws-api"            % "1.1",
  "org.apache.commons"        % "commons-lang3"            % "3.9",
  "org.seleniumhq.selenium"   % "selenium-java"            % "3.141.59",
  "org.easytesting"           % "fest-util"                % "1.2.5",
  "org.easytesting"           % "fest-swing"               % "1.2.1",
  "org.codehaus.jackson"      % "jackson-core-asl"         % "1.9.13",
  "org.codehaus.jackson"      % "jackson-mapper-asl"       % "1.9.13",
  "xalan"                     % "xalan"                    % "2.7.2",
  "org.dspace.oclc"           % "oclc-srw"                 % "1.0.20080328",
  "org.apache.cxf"            % "cxf-rt-frontend-simple"   % "3.3.5",
  "org.apache.cxf"            % "cxf-rt-databinding-aegis" % "3.3.5",
  "org.apache.cxf"            % "cxf-rt-transports-http"   % "3.3.5",
  "org.apache.httpcomponents" % "httpclient"               % "4.5.10",
//  "org.apache.cxf" % "cxf-bundle" % "2.7.6",
  "axis"       % "axis" % "1.4",
  "com.jcraft" % "jsch" % "0.1.55",
//  "jpf" % "jpf-tools" % "1.0.5",
  "org.jacoco"       % "org.jacoco.report"         % "0.7.9",
  "org.dspace"       % "oclc-harvester2"           % "0.1.12",
  "org.jvnet.hudson" % "xstream"                   % "1.3.1-hudson-8",
  "com.typesafe"     % "config"                    % "1.4.0",
  "org.slf4j"        % "slf4j-simple"              % "1.7.30",
  "org.scalacheck"   %% "scalacheck"               % "1.13.5" % "test,serial",
  "org.http4s"       %% "http4s-async-http-client" % http4sVersion,
  "org.http4s"       %% "http4s-blaze-client"      % http4sVersion,
  "org.http4s"       %% "http4s-circe"             % http4sVersion,
  "org.typelevel"    %% "cats-free"                % catsVersion,
  "com.unboundid"    % "unboundid-ldapsdk"         % "4.0.12"
)

unmanagedBase in Compile := baseDirectory.value / "lib/adminjars"

def serialFilter(name: String): Boolean = {
  name endsWith "PropertiesSerial"
}
def stdFilter(name: String): Boolean = {
  (name endsWith "Properties") && !serialFilter(name)
}

val commonOptions = Seq(
  sbt.Tests.Argument(TestFrameworks.ScalaCheck, "-s", "1")
)
testOptions in Serial := commonOptions

testOptions in Test := commonOptions

testOptions in Test += sbt.Tests.Filter(stdFilter)

testOptions in Serial += sbt.Tests.Filter(serialFilter)

parallelExecution in Serial := false

parallelExecution in Test := autotestBuildConfig.value.getBoolean("tests.parallel")
