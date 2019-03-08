import com.typesafe.config.{Config, ConfigFactory}
import org.jacoco.core.tools.{ExecDumpClient, ExecFileLoader}
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt.Keys.{scalaVersion, version}
import sbt.complete.DefaultParsers.spaceDelimited

import scala.sys.process._
import Path.rebase

import scala.collection.JavaConverters._

name := "equella-autotests"

libraryDependencies += "org.jacoco" % "org.jacoco.agent" % "0.7.9" classifier "runtime"


lazy val config = (project in file("config")).settings(resourceDirectory in Compile := baseDirectory.value / "resources")

lazy val IntegTester = project in file("IntegTester")

lazy val Tests = project in file("Tests")

lazy val OldTests = (project in file("OldTests")).dependsOn(Tests, config)


autotestBuildConfig in ThisBuild := {
  val defaultConfig = ConfigFactory.parseFile(file("autotest/autotest-defaults.conf"))
  val configFile = file(sys.props.getOrElse("config.file", {
    val envConfig = sys.env.get("AUTOTEST_CONFIG")
    envConfig.foreach { cf => sys.props.update("config.file", cf); ConfigFactory.invalidateCaches() }
    envConfig.getOrElse("autotest/config/resources/application.conf")
  }))
  sLog.value.info(s"Loading config from: ${configFile.absolutePath}")
  ConfigFactory.load(ConfigFactory.parseFile(configFile).withFallback(defaultConfig))
}

lazy val installConfig = Def.setting[Config] {
  autotestBuildConfig.value.getConfig("install")
}

installDir := optPath(installConfig.value, "basedir").getOrElse(baseDirectory.value / "equella-install")

installOptions := {
  val ic = installConfig.value
  val jacocoJar = coverageJar.value
  val jacoco = Option(ic.getString("jacoco")).filter(_.nonEmpty).map(o => JacocoAgent(jacocoJar, o))
  val db = ic.getConfig("db")
  InstallOptions(
    installDir.value, file(sys.props("java.home")),
    url = ic.getString("url"), hostname = ic.getString("hostname"), port = ic.getInt("port"),
    jacoco = jacoco, dbtype = db.getString("type"), dbname = db.getString("name"),
    dbport = db.getInt("port"), dbhost = db.getString("host"), dbuser = db.getString("user"),
    dbpassword = db.getString("password"))
}

def optPath(bc: Config, p: String) = if (bc.hasPath(p)) Some(file(bc.getString(p))) else None

autotestInstallerZip := {
  val bc = autotestBuildConfig.value
  optPath(bc, "install.zip").orElse(optPath(bc, "install.dir").map(d => (d * "equella-installer-*.zip").get.head))
}

sourceZip := optPath(autotestBuildConfig.value, "install.sourcezip")

lazy val relevantClasses: Seq[String] => Boolean = {
  case Seq("com", "tle", "admin", _*) => false
  case Seq("com", "dytech", "edge", "admin", _*) => false
  case Seq("com", "dytech", "gui", _*) => false
  case Seq("com", "blackboard", _*) => false
  case Seq("com", "tle", "core", "connectors", "blackboard", "webservice", _*) => false
  case _ => true
}

coverageJar := {
  update.value.select(
    configurationFilter(AllPassFilter),
    moduleFilter("org.jacoco", "org.jacoco.agent"),
    artifactFilter(classifier = "runtime")
  ).head
}

dumpCoverage := {
  val f = target.value / "jacoco.exec"
  sLog.value.info(s"Dumping coverage data to ${f.absolutePath}")
  coverageLoader.value.save(f, false)
  f
}

coverageLoader := {
  val log = sLog.value
  val cc = autotestBuildConfig.value.getConfig("coverage")
  val l = new ExecFileLoader()
  optPath(cc, "file").filter(_.canRead).foreach { f =>
    log.info(s"Loading coverage data from ${f.absolutePath}")
    l.load(f)
  }
  cc.getStringList("hosts").asScala.foreach { h =>
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

sourceDirectory in coverageReport := target.value / "all_srcs"
target in coverageReport := {
  val cc = autotestBuildConfig.value.getConfig("coverage")
  optPath(cc, "reportdir").getOrElse(target.value / "coverage-report")
}

coverageReport := {
  val log = sLog.value
  val io = installOptions.value
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
  val allSrcs = (sourceDirectory in coverageReport).value
  srcZip.foreach(z => IO.unzip(z, allSrcs))
  val coverageDir = (target in coverageReport).value
  log.info(s"Creating coverage report at ${coverageDir.absolutePath}")
  CoverageReporter.createReport(execLoader, allPlugins.groupBy(_._1).mapValues(_.map(_._2)).toSeq, coverageDir, allSrcs)
}

installEquella := {
  val opts = installOptions.value
  val zipFile = autotestInstallerZip.value
  val log = sLog.value
  val installSettings = target.value / "installsettings.xml"
  zipFile.fold(sys.error("Must have install.zip set")) { z =>
    val installFiles = target.value / "installer_files"
    log.info(s"Unzipping $z")
    IO.delete(installFiles)
    IO.unzip(z, installFiles)
    val baseInstaller = (installFiles * "*").get.head
    val installerJar = baseInstaller / "enterprise-install.jar"
    opts.writeXML(installSettings, baseInstaller)
    val o = ForkOptions().withRunJVMOptions(Vector("-jar", installerJar.absolutePath))
    val args = Seq("--unsupported", installSettings.absolutePath)
    Fork.java(o, args)
    baseInstaller
  }
}

def serviceCommand(opts: InstallOptions, cmd: String): Unit = {
  val serverScript = opts.installDir / "manager/equellaserver"
  List(serverScript.absolutePath, cmd) !
}

startEquella := serviceCommand(installOptions.value, "start")

stopEquella := serviceCommand(installOptions.value, "stop")

val TestPrj = LocalProject("Tests")

setupForTests := {
  val run = (runner in (TestPrj,Test)).value
  val log = sLog.value
  run.run("equellatests.SetupForTests", (fullClasspath in (TestPrj, Test)).value.files, spaceDelimited("<arg>").parsed, log)
}

configureInstall := {
  val run = (runner in (TestPrj, Test)).value
  run.run("equellatests.InstallFirstTime", (fullClasspath in (TestPrj, Test)).value.files, Seq(), sLog.value)
}

aggregate in test := false

collectArtifacts := {
  val results = target.value / "test-artifacts.zip"
  def allFiles(files: Seq[File]): Traversable[(File, String)] = {
    files.flatMap(f => (f ** "*").pair(rebase(f, f.getName)))
  }
  val logsDir = installDir.value / "logs"
  val scReportDir = (target in LocalProject("Tests")).value / "test-reports"
  val oldReportDir = file((testNGOutputDirectory in OldTests).value)

  sLog.value.info(s"Collecting test artifacts into ${results.absolutePath}")
  IO.zip(allFiles(Seq(logsDir, scReportDir, oldReportDir, (target in coverageReport).value)), results)
  results
}

concurrentRestrictions in Global := {
  val testConfig = autotestBuildConfig.value.getConfig("tests")
  if (testConfig.hasPath("maxthreads"))
  {
    Seq(
      Tags.limit(Tags.Test, testConfig.getInt("maxthreads"))
    )
  }
  else {
    Seq()
  }
}


/*
Steps to clusterize install
Change freetext path to local dir - mandatory config
Change logs path in learningedge-log4j.properties
Set LOGS_HOME in equellaserver-config.sh
Set EQUELLASERVER_HOME in equellaserver-config.sh
Remove java.io.tmpdir property from equellaserver-config.sh
Make sure LOGS_HOME dir exists
 */
