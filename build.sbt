import de.johoop.testngplugin.TestNGPlugin.testNGSuites

name := "tests"

version := "1.0"

scalaVersion := "2.12.2"

lazy val common = Seq(
  resolvers += "Local EQUELLA deps" at IO.toURI(file(Path.userHome.absolutePath) / "/equella-deps").toString
)

lazy val platform = (project in file("Platform/Plugins/com.tle.platform.common")).settings(common).settings(
  javaSource in Compile := baseDirectory.value / "src",
  javaSource in Test := baseDirectory.value / "test",
  libraryDependencies ++= Seq(
    "org.apache.commons" % "commons-compress" % "1.1",
    "jpf" % "jpf" % "1.0.7",
    "com.google.guava" % "guava" % "18.0",
    "commons-beanutils" % "commons-beanutils-equella" % "1.8.2.1",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "commons-codec" % "commons-codec" % "1.7",
    "junit" % "junit" % "4.12" % Test
  )
)

lazy val selenium_tests = (project in file("Tests")).settings(common).settings(
  javaSource in Test := baseDirectory.value / "src",
  libraryDependencies ++= Seq(
    "org.seleniumhq.selenium" % "selenium-java" % "3.4.0",
    "org.testng" % "testng" % "6.11",
    "org.easytesting" % "fest-util" % "1.2.5",
    "org.easytesting" % "fest-swing-testng" % "1.2.1",
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
    "org.slf4j" % "slf4j-simple" % "1.7.5"
  ),
  unmanagedBase in Compile := baseDirectory.value / "lib/adminjars",
  unmanagedClasspath in Test += baseDirectory.value / "config",
  testNGSettings,
  testNGOutputDirectory := (target.value / "testng").absolutePath,
  testNGSuites := Seq("Tests/testng.xml")
).dependsOn(platform)

