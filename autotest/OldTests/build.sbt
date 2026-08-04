import de.johoop.testngplugin.TestNGPlugin
import scala.jdk.CollectionConverters._

val circeVersion  = "0.14.12"
val http4sVersion = "0.23.36"
val catsVersion   = "2.13.0"

libraryDependencies += "com.opencsv" % "opencsv" % "5.12.0"
libraryDependencies ++= Seq(
  "org.testng" % "testng" % "7.12.0" % Test,
  // The older Log4j is required by dependency "oclc-harvester2" at runtime.
  "log4j"                    % "log4j"              % "1.2.17" % Test,
  "commons-httpclient"       % "commons-httpclient" % "3.1"    % Test,
  "com.thoughtworks.xstream" % "xstream"            % "1.4.21" % Test,
  // Drives the property-based suites under `equellatests`. ScalaTest itself comes from
  // CommonSettings; scalacheck-1-19 is the ScalaTest 3.2.20 <-> ScalaCheck 1.19 bridge.
  "org.scalacheck"    %% "scalacheck"      % "1.19.0"   % Test,
  "org.scalatestplus" %% "scalacheck-1-19" % "3.2.20.0" % Test
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

// The Selenium framework and page-object library these suites are built on.
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

// Prebuilt admin-console jars, needed by the fest-swing admin console tests. Left unscoped because
// unmanagedBase is a project-level setting; the jars reach the test classpath from here.
unmanagedBase := baseDirectory.value / "lib/adminjars"

enablePlugins(TestNGPlugin)

// This project holds two kinds of end-to-end test: the long-standing TestNG suites, and newer suites
// written with ScalaTest (currently the property-based ones under `equellatests`, but ScalaTest is
// where all new tests should go). They need separate tasks rather than one merged `test`, because CI
// shards the TestNG suites into several parallel jobs — anything sharing the `Test` config would be
// re-run by every shard.
//
// The split is by framework rather than by suite name, so a new ScalaTest suite is picked up wherever
// it lives:
//
//   OldTests/test            -> TestNG only
//   OldTests/ScalaTest/test  -> ScalaTest only
lazy val ScalaTest = config("scalatest") extend Test

configs(ScalaTest)

inConfig(ScalaTest)(Defaults.testTasks)

// sbt auto-generates a `configuration` setting key for every custom Configuration and flags it as
// unused by lintUnused, even though the ScalaTest config itself is actively used.
Global / excludeLintKeys += ScalaTest / configuration

// sbt-testng-plugin appends to testFrameworks, so without these both configs would otherwise
// discover both frameworks.
val testNGFramework = TestFramework("de.johoop.testnginterface.TestNGFramework")

Test / testFrameworks      := Seq(testNGFramework)
ScalaTest / testFrameworks := Seq(TestFrameworks.ScalaTest)

testNGOutputDirectory := (target.value / "testng").absolutePath

testNGParameters ++= Seq("-log", autotestBuildConfig.value.getInt("tests.verbose").toString)

testNGSuites := {
  val tc = autotestBuildConfig.value.getConfig("tests")
  tc.getStringList("suitenames").asScala.map(n => (baseDirectory.value / n).absolutePath)
}

Test / testOptions += {
  val outDir = testNGOutputDirectory.value
  Tests.Setup(() => System.setProperty("testng.output.dir", outDir))
}
Test / testOptions += Tests.Cleanup(() => System.clearProperty("testng.output.dir"))

// Mirror TestAnnotationTransformer's UI filtering for ScalaTest suites. That class reads the same
// environment variable to disable @NewUIOnly/@OldUIOnly TestNG tests; both annotations carry
// ScalaTest's @TagAnnotation so the tag name is the annotation's fully qualified class name.
val NewUiOnlyTag = "testng.annotation.NewUIOnly"
val OldUiOnlyTag = "testng.annotation.OldUIOnly"

ScalaTest / testOptions += {
  // Boolean.parseBoolean, as used by TestAnnotationTransformer, accepts only "true" ignoring case.
  val newUiEnabled = sys.env.get("OLD_TEST_NEWUI").exists(_.equalsIgnoreCase("true"))
  Tests.Argument(TestFrameworks.ScalaTest, "-l", if (newUiEnabled) OldUiOnlyTag else NewUiOnlyTag)
}

// JUnit XML, so results reach GitLab's test report alongside TestNG's own junitreports. Absolute
// because sbt's working directory is the build root rather than this project.
ScalaTest / testOptions += Tests.Argument(
  TestFrameworks.ScalaTest,
  "-u",
  (target.value / "scalatest-junitreports").absolutePath
)

// The TestNG suites have long been run strictly sequentially (see `threadCount: 1` in
// testng-codebuild.yaml), and every config that is actually used sets `tests.parallel = false`.
// Honouring the same key for both keeps the ScalaTest suites in step, which is what isolates the ones
// that mutate institution-wide state from the TestNG suites sharing that institution.
Test / parallelExecution      := autotestBuildConfig.value.getBoolean("tests.parallel")
ScalaTest / parallelExecution := autotestBuildConfig.value.getBoolean("tests.parallel")
