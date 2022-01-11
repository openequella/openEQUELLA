import de.johoop.testngplugin.TestNGPlugin
import scala.collection.JavaConverters._

libraryDependencies += "com.opencsv" % "opencsv" % "5.5.2"
libraryDependencies ++= Seq(
  "org.testng"               % "testng"             % "6.14.3" % Test,
  "org.apache.logging.log4j" % "log4j"              % "2.17.1" % Test,
  "commons-httpclient"       % "commons-httpclient" % "3.1"    % Test
)

enablePlugins(TestNGPlugin)

testNGOutputDirectory := (target.value / "testng").absolutePath

testNGParameters ++= Seq("-log", autotestBuildConfig.value.getInt("tests.verbose").toString)

testNGSuites := {
  val tc = autotestBuildConfig.value.getConfig("tests")
  tc.getStringList("suitenames").asScala.map(n => (baseDirectory.value / n).absolutePath)
}
