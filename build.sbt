import com.typesafe.config.{Config, ConfigFactory}
import org.jacoco.core.tools.{ExecDumpClient, ExecFileLoader}
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt.Keys.{scalaVersion, version}

import scala.collection.JavaConversions._

name := "equella-autotests"

libraryDependencies += "org.jacoco" % "org.jacoco.agent" % "0.7.9" classifier "runtime"

lazy val installerDir = "equella-installer-6.5"

lazy val common = Seq(
  resolvers += "Local EQUELLA deps" at IO.toURI(file(Path.userHome.absolutePath) / "/equella-deps").toString,
  scalaVersion := "2.12.3",
  version := "1.0"
)

common

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

lazy val config = (project in file("config")).settings(resourceDirectory in Compile := baseDirectory.value / "resources").settings(common)

lazy val Tests = (project in file("Tests")).settings(common).dependsOn(platform, config)

lazy val OldTests = (project in file("OldTests")).settings(common).dependsOn(platform, Tests, config)

val IntegTester = project in file("IntegTester")

buildConfig in ThisBuild := {
  val defaultConfig = ConfigFactory.parseFile(file("project/build-defaults.conf"))
  val configFile = sys.props.get("config.file").getOrElse("config/resources/application.conf")
  ConfigFactory.load(ConfigFactory.parseFile(file(configFile)).withFallback(defaultConfig))
}

lazy val installConfig = Def.setting[Config] {
  buildConfig.value.getConfig("install")
}

installDir := optPath(installConfig.value, "basedir").getOrElse(baseDirectory.value / "equella-install")

installOptions := {
  val ic = installConfig.value
  val jacoco = Option(ic.getString("jacoco")).filter(_.nonEmpty).map(o => JacocoAgent(coverageJar.value, o))
  val db = ic.getConfig("db")
  InstallOptions(target.value / installerDir,
    installDir.value, file(sys.props("java.home")),
    url = ic.getString("url"), hostname = ic.getString("hostname"), port = ic.getInt("port"),
    jacoco = jacoco, dbtype = db.getString("type"), dbname = db.getString("name"),
    dbport = db.getInt("port"), dbhost = db.getString("host"), dbuser = db.getString("user"),
    dbpassword = db.getString("password"))
}

def optPath(bc: Config, p: String) = if (bc.hasPath(p)) Some(file(bc.getString(p))) else None

installerZip := optPath(buildConfig.value, "install.zip")

sourceZip := optPath(buildConfig.value, "install.sourcezip")

lazy val relevantClasses: Seq[String] => Boolean = {
  case Seq("com", "tle", "admin", _*) => false
  case Seq("com", "dytech", "edge", "admin", _*) => false
  case Seq("com", "dytech", "gui", _*) => false
  case Seq("com", "blackboard", _*) => false
  case Seq("com", "tle", "core", "connectors", "blackboard", "webservice", _*) => false
  case _ => true
}

coverageJar := {
  update.value.select(module = moduleFilter("org.jacoco", "org.jacoco.agent"),
    artifact = artifactFilter(classifier = "runtime")).head
}

dumpCoverage := {
  val f = target.value / "jacoco.exec"
  sLog.value.info(s"Dumping coverage data to ${f.absolutePath}")
  coverageLoader.value.save(f, false)
  f
}

coverageLoader := {
  val log = sLog.value
  val cc = buildConfig.value.getConfig("coverage")
  val l = new ExecFileLoader()
  optPath(cc, "file").filter(_.canRead).foreach { f =>
    log.info(s"Loading coverage data from ${f.absolutePath}")
    l.load(f)
  }
  cc.getStringList("hosts").foreach { h =>
    val ind = h.indexOf(':')
    val (hname, port) = if (ind == -1) (h, 6300) else (h.substring(0, ind), h.substring(ind + 1).toInt)
    log.info(s"Collecting coverage from $h")
    CoverageReporter.dumpCoverage(l, hname, port)
  }
  l
}

val saxBuilder = {
  val sb = new SAXBuilder(XMLReaders.NONVALIDATING)
  sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
  sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
  sb
}

coverageReport := {
  val log = sLog.value
  val io = installOptions.value
  val cc = buildConfig.value.getConfig("coverage")
  val execLoader = coverageLoader.value
  val allClasses = target.value / "all_classes"
  IO.delete(allClasses)
  val classFilter = new NameFilter {
    def accept(name: String): Boolean = if (name.startsWith("classes/"))
      relevantClasses.apply(name.substring("classes/".length).split("/"))
    else name == "plugin-jpf.xml"
  }
  val allPlugins = (io.installDir / "plugins" ** "*.jar").get.flatMap { jar =>
    val clzDir = allClasses / jar.getName
    val files = IO.unzip(jar, clzDir, filter = classFilter)
    val jpf = saxBuilder.build(clzDir / "plugin-jpf.xml")
    val pluginId = jpf.getRootElement.getAttributeValue("id")
    if (files.size > 2)
      Some((jar.getParentFile.getName, CoveragePlugin(clzDir, pluginId)))
    else None
  }

  val srcZip = sourceZip.value
  val allSrcs = target.value / "all_srcs"
  srcZip.foreach(z => IO.unzip(z, allSrcs))
  val coverageDir = optPath(cc, "reportdir").getOrElse(target.value / "coverage-report")
  log.info(s"Creating coverage report at ${coverageDir.absolutePath}")
  CoverageReporter.createReport(execLoader, allPlugins.groupBy(_._1).mapValues(_.map(_._2)).toSeq, coverageDir, allSrcs)
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

/*
Steps to clusterize install
Change freetext path to local dir - mandatory config
Change logs path in learningedge-log4j.properties
Set LOGS_HOME in equellaserver-config.sh
Set EQUELLASERVER_HOME in equellaserver-config.sh
Remove java.io.tmpdir property from equellaserver-config.sh
Make sure LOGS_HOME dir exists
 */
