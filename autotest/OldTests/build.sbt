import de.johoop.testngplugin.TestNGPlugin
import scala.collection.JavaConverters._

libraryDependencies ++= Seq(
  "org.testng"         % "testng"             % "6.11"   % Test,
  "org.easytesting"    % "fest-swing-testng"  % "1.2.1"  % Test,
  "log4j"              % "log4j"              % "1.2.17" % Test,
  "commons-httpclient" % "commons-httpclient" % "3.1"    % Test
)

enablePlugins(TestNGPlugin)

testNGOutputDirectory := (target.value / "testng").absolutePath

testNGParameters ++= Seq("-log", autotestBuildConfig.value.getInt("tests.verbose").toString)

testNGSuites := {
  val tc = autotestBuildConfig.value.getConfig("tests")
  tc.getStringList("suitenames").asScala.map(n => (baseDirectory.value / n).absolutePath)
}
