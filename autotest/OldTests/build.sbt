import de.johoop.testngplugin.TestNGPlugin
import scala.jdk.CollectionConverters._

libraryDependencies += "com.opencsv" % "opencsv" % "5.12.0"
libraryDependencies ++= Seq(
  "org.testng" % "testng" % "7.12.0" % Test,
  // The older Log4j is required by dependency "oclc-harvester2" at runtime.
  "log4j"                    % "log4j"              % "1.2.17" % Test,
  "commons-httpclient"       % "commons-httpclient" % "3.1"    % Test,
  "com.thoughtworks.xstream" % "xstream"            % "1.4.21" % Test,
  // Drives the property-based suites under `equellatests`, moved here from the Tests project.
  "org.scalacheck" %% "scalacheck" % "1.19.0" % Test
)

enablePlugins(TestNGPlugin)

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

// The TestNG suites have long been run strictly sequentially (see `threadCount: 1` in
// testng-codebuild.yaml), and every config that is actually used sets `tests.parallel = false`.
// Honouring the same key keeps the moved property-based suites in step, which is what isolates
// the ones that mutate institution-wide state from the TestNG suites sharing that institution.
Test / parallelExecution := autotestBuildConfig.value.getBoolean("tests.parallel")
