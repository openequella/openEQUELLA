libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "3.4.0",
  "org.easytesting" % "fest-util" % "1.2.5",
  "org.easytesting" % "fest-swing" % "1.2.1",
  "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13",
  "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.13",
  "org.dspace.oclc" % "oclc-srw" % "1.0.20080328",
  "org.apache.cxf" % "cxf-bundle" % "2.7.6",
  "axis" % "axis" % "1.4",
  "com.jcraft" % "jsch" % "0.1.54",
  "jpf" % "jpf-tools" % "1.0.5",
  "org.jacoco" % "org.jacoco.report" % "0.7.9",
  "org.dspace" % "oclc-harvester2" % "0.1.12",
  "org.jvnet.hudson" % "xstream" % "1.3.1-hudson-8",
  "com.typesafe" % "config" % "1.3.1",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
)

unmanagedBase in Compile := baseDirectory.value / "lib/adminjars"

unmanagedClasspath in Test += baseDirectory.value / buildConfig.value.getString("tests.configdir")

testOptions in Test := Seq(
  Tests.Argument(TestFrameworks.ScalaCheck, "-s", "1"),
  Tests.Setup( () => setupForTests.value )
)

parallelExecution in Test := buildConfig.value.getBoolean("tests.parallel")

setupForTests := (runMain in Test).toTask(" equellatests.SetupForTests").value
