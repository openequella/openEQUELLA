import de.johoop.testngplugin.TestNGPlugin
import scala.jdk.CollectionConverters._

libraryDependencies += "com.opencsv" % "opencsv" % "5.9"
libraryDependencies ++= Seq(
  "org.testng" % "testng" % "6.14.3" % Test,
  // The older Log4j is required by dependency "oclc-harvester2" at runtime.
  "log4j"                    % "log4j"              % "1.2.17" % Test,
  "commons-httpclient"       % "commons-httpclient" % "3.1"    % Test,
  "com.thoughtworks.xstream" % "xstream"            % "1.4.21" % Test
)

/*
Although very old and has vulns, axis 1.4 is required for the SRW tests (SRWTest etc) and is needed
when using the very old (and unsure where the code is) oclc-srw.
 */
libraryDependencies ++= Seq(
  "axis"            % "axis"     % "1.4"          % Test,
  "org.dspace.oclc" % "oclc-srw" % "1.0.20080328" % Test
)

enablePlugins(TestNGPlugin)

testNGOutputDirectory := (target.value / "testng").absolutePath

testNGParameters ++= Seq("-log", autotestBuildConfig.value.getInt("tests.verbose").toString)

testNGSuites := {
  val tc = autotestBuildConfig.value.getConfig("tests")
  tc.getStringList("suitenames").asScala.map(n => (baseDirectory.value / n).absolutePath)
}
