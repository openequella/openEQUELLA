import com.typesafe.config.{Config, ConfigFactory}
import de.johoop.testngplugin.TestNGPlugin.testNGSuites
import scala.collection.JavaConversions._

name := "tests"

version := "1.0"

scalaVersion := "2.12.2"

lazy val installDir = settingKey[File]("Dir to install EQUELLA into")
lazy val installEquella = taskKey[File]("Install EQUELLA locally")
lazy val startEquella = taskKey[Unit]("Start the EQUELLA service")
lazy val stopEquella = taskKey[Unit]("Stop the EQUELLA service")
lazy val installOptions = settingKey[InstallOptions]("EQUELLA installer options")
lazy val installerZip = settingKey[Option[File]]("The installer zip")
lazy val buildConfig = settingKey[Config]("The build config options")

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
  testNGSuites := {
    val tc = buildConfig.value.getConfig("tests")
    tc.getStringList("suitenames").map(n => s"Tests/$n")
  }
).dependsOn(platform)

val IntegTester = project in file("IntegTester")

buildConfig in ThisBuild := {
  val defaultConfig = ConfigFactory.parseFile(file("project/build-defaults.conf"))
  ConfigFactory.parseFile(file("build.conf")).withFallback(defaultConfig)
}

installDir := target.value / "equella-install"

installOptions := {
  val ic = buildConfig.value.getConfig("install")
  InstallOptions(target.value / "equella-installer-6.4",
    installDir.value, file(sys.props("java.home")),
    url = ic.getString("url"), hostname = ic.getString("hostname"), port = ic.getInt("port"))
}

installerZip := {
  if (buildConfig.value.hasPath("install.zip")) Some(file(buildConfig.value.getString("install.zip"))) else None
}

installEquella := {
  val opts = installOptions.value
  val zipFile = installerZip.value
  val installSettings = target.value / "installsettings.xml"
  zipFile.fold(sys.error("Must have install.zip set")) { z =>
    IO.unzip(z, target.value)
    val baseInstaller = opts.baseInstall
    val installerJar = baseInstaller / "enterprise-install.jar"
    opts.writeXML(installSettings)
    val o = ForkOptions(runJVMOptions = Seq(
      "-jar", installerJar.absolutePath
    ))
    val args = Seq("--unsupported", installSettings.absolutePath)
    Fork.java(o, args)
    baseInstaller
  }
}

def serviceCommand(opts: InstallOptions, cmd: String): Unit = {
  val serverScript = opts.baseDir / "manager/equellaserver"
  List(serverScript.absolutePath, cmd).!
}

startEquella := serviceCommand(installOptions.value, "start")

stopEquella := serviceCommand(installOptions.value, "stop")
