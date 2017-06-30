import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConversions._

name := "equella-autotests"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies += "org.jacoco" % "org.jacoco.agent" % "0.7.9" classifier "runtime"

lazy val installerDir = "equella-installer-6.4"

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
    "commons-beanutils" % "commons-beanutils" % "1.9.3",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "commons-codec" % "commons-codec" % "1.7",
    "junit" % "junit" % "4.12" % Test
  )
)

lazy val config = (project in file("config")).settings(resourceDirectory in Compile := baseDirectory.value / "resources")

lazy val Tests = (project in file("Tests")).settings(common).dependsOn(platform, config)

lazy val OldTests = (project in file("OldTests")).settings(common).dependsOn(platform, Tests, config)

val IntegTester = project in file("IntegTester")

buildConfig in ThisBuild := {
  val defaultConfig = ConfigFactory.parseFile(file("project/build-defaults.conf"))
  val configFile = sys.props.get("config.file").getOrElse("config/resources/application.conf")
  ConfigFactory.load(ConfigFactory.parseFile(file(configFile)).withFallback(defaultConfig))
}

installDir := baseDirectory.value / "equella-install"

installOptions := {
  val jacocoJar = update.value.select(module = moduleFilter("org.jacoco", "org.jacoco.agent"),
   artifact = artifactFilter(classifier = "runtime")).head
  val ic = buildConfig.value.getConfig("install")
  InstallOptions(target.value / installerDir,
    installDir.value, file(sys.props("java.home")),
    url = ic.getString("url"), hostname = ic.getString("hostname"), port = ic.getInt("port"),
    jacoco = Some(JacocoAgent(jacocoJar, target.value / "jacoco.exec")))
}

installerZip := {
  if (buildConfig.value.hasPath("install.zip")) Some(file(buildConfig.value.getString("install.zip"))) else None
}

coverageReport := {
  val io = installOptions.value
  val allClasses = target.value / "all_classes"
  (io.installDir / "plugins" ** "*.jar").get.foreach { jar =>
    IO.unzip(jar, allClasses, filter = "classes/*")
  }
  CoverageReporter.createReport(io.jacoco.map(_.outFile).get, allClasses,
    "Coverage", target.value / "coverage-report", target.value)
}

installEquella := {
  val opts = installOptions.value
  val zipFile = installerZip.value
  val installSettings = target.value / "installsettings.xml"
  zipFile.fold(sys.error("Must have install.zip set")) { z =>
    IO.delete(target.value / installerDir)
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
  val serverScript = opts.installDir / "manager/equellaserver"
  List(serverScript.absolutePath, cmd).!
}

startEquella := serviceCommand(installOptions.value, "start")

stopEquella := serviceCommand(installOptions.value, "stop")

aggregate in test := false
