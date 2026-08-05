dependsOn(LocalProject("IntegTester"), LocalProject("config"))

val circeVersion  = "0.14.12"
val http4sVersion = "0.23.36"
val catsVersion   = "2.13.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "javax.jws"                 % "javax.jws-api"     % "1.1",
  "org.apache.commons"        % "commons-lang3"     % "3.20.0",
  "org.seleniumhq.selenium"   % "selenium-java"     % "4.45.0",
  "com.codeborne"             % "selenide"          % "7.17.0",
  "org.easytesting"           % "fest-util"         % "1.2.5",
  "org.easytesting"           % "fest-swing"        % "1.2.1",
  "xalan"                     % "xalan"             % "2.7.3",
  "xalan"                     % "serializer"        % "2.7.3",
  "org.apache.httpcomponents" % "httpclient"        % "4.5.14",
  "com.jcraft"                % "jsch"              % "0.1.55",
  "org.jacoco"                % "org.jacoco.report" % "0.8.15",
  "org.dspace"                % "oclc-harvester2"   % "1.0.0",
  "com.typesafe"              % "config"            % "1.4.9",
  "org.apache.logging.log4j"  % "log4j"             % log4jVersion,
  "org.apache.logging.log4j"  % "log4j-core"        % log4jVersion,
  "org.apache.logging.log4j"  % "log4j-slf4j2-impl" % log4jVersion,
  "org.http4s" %% "http4s-blaze-client" % "0.23.17", // The latest version of blzae client is still 0.23.17 by 13/05/2025.
  "org.http4s"    %% "http4s-circe"      % http4sVersion,
  "org.typelevel" %% "cats-free"         % catsVersion,
  "com.unboundid"  % "unboundid-ldapsdk" % "7.0.5",
  jacksonDataBind,
  jacksonDataFormatYaml,
  "com.auth0" % "jwks-rsa" % "0.24.1"
)

(Compile / unmanagedBase) := baseDirectory.value / "lib/adminjars"
