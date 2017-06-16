import scala.collection.JavaConversions._

libraryDependencies ++= Seq(
  "org.testng" % "testng" % "6.11" % Test,
  "org.easytesting" % "fest-swing-testng" % "1.2.1" % Test
)

testNGSettings

testNGOutputDirectory := (target.value / "testng").absolutePath

testNGParameters ++= Seq("-log", buildConfig.value.getInt("tests.verbose").toString)

testNGSuites := {
  val tc = buildConfig.value.getConfig("tests")
  tc.getStringList("suitenames").map(n => (baseDirectory.value / n).absolutePath)
}

