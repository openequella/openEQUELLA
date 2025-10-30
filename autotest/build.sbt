import com.typesafe.config.{Config, ConfigFactory}
import org.jacoco.core.tools.ExecFileLoader
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt.complete.DefaultParsers.spaceDelimited

import scala.sys.process._
import Path.rebase
import cats.instances.uuid

import scala.jdk.CollectionConverters._

name := "equella-autotests"

libraryDependencies += "org.jacoco" % "org.jacoco.agent" % "0.8.14" classifier "runtime"

lazy val config = (project in file("config"))
  .settings((Compile / resourceDirectory) := baseDirectory.value / "resources")

lazy val IntegTester = project in file("IntegTester")

lazy val Tests = project in file("Tests")

lazy val OldTests = (project in file("OldTests")).dependsOn(Tests, config)

(ThisBuild / autotestBuildConfig) := {
  val defaultConfig = ConfigFactory.parseFile(file("autotest/autotest-defaults.conf"))
  val configFile = file(
    sys.props.getOrElse(
      "config.file", {
        val envConfig = sys.env.get("AUTOTEST_CONFIG")
        envConfig.foreach { cf =>
          sys.props.update("config.file", cf); ConfigFactory.invalidateCaches()
        }
        envConfig.getOrElse("autotest/config/resources/application.conf")
      }
    )
  )
  sLog.value.info(s"Loading config from: ${configFile.absolutePath}")
  ConfigFactory.load(ConfigFactory.parseFile(configFile).withFallback(defaultConfig))
}

lazy val installConfig = Def.setting[Config] {
  autotestBuildConfig.value.getConfig("install")
}

installDir := optPath(installConfig.value, "basedir")
  .getOrElse(baseDirectory.value / "equella-install")

installOptions := {
  val ic        = installConfig.value
  val jacocoJar = coverageJar.value
  val jacoco = Option(ic.getString("jacoco")).filter(_.nonEmpty).map(o => JacocoAgent(jacocoJar, o))
  val db     = ic.getConfig("db")
  InstallOptions(
    installDir.value,
    file(sys.props("java.home")),
    url = ic.getString("url"),
    hostname = ic.getString("hostname"),
    port = ic.getInt("port"),
    jacoco = jacoco,
    dbtype = db.getString("type"),
    dbname = db.getString("name"),
    dbport = db.getInt("port"),
    dbhost = db.getString("host"),
    dbuser = db.getString("user"),
    dbpassword = db.getString("password"),
    auditLevel = ic.getString("auditLevel")
  )
}

def optPath(bc: Config, p: String) = if (bc.hasPath(p)) Some(file(bc.getString(p))) else None

autotestInstallerZip := {
  val bc                    = autotestBuildConfig.value
  val equellaFullVersion    = equellaVersion.value
  val installerFileName     = s"equella-installer-${equellaFullVersion.semanticVersion}.zip"
  val installerDirectory    = (LocalProject("Installer") / target).value
  val installerAbsolutePath = installerDirectory / installerFileName
  // If the Installer named as installerFileName exists then return it, otherwise returns the default Installer
  if (installerAbsolutePath.exists) {
    Some(installerAbsolutePath)
  } else {
    optPath(bc, "install.zip").orElse(
      optPath(bc, "install.dir").map(d => (d * "equella-installer-*.zip").get.head)
    )
  }
}

sourceZip := optPath(autotestBuildConfig.value, "install.sourcezip")

lazy val relevantClasses: Seq[String] => Boolean = {
  case Seq("com", "tle", "admin", _*)                                          => false
  case Seq("com", "dytech", "edge", "admin", _*)                               => false
  case Seq("com", "dytech", "gui", _*)                                         => false
  case Seq("com", "blackboard", _*)                                            => false
  case Seq("com", "tle", "core", "connectors", "blackboard", "webservice", _*) => false
  case _                                                                       => true
}

coverageJar := {
  update.value
    .select(
      configurationFilter(AllPassFilter),
      moduleFilter("org.jacoco", "org.jacoco.agent"),
      artifactFilter(classifier = "runtime")
    )
    .head
}

/** Dumps coverage data to a single file in the target directory, unless the configuration file has
  * a directory specified at the path of `coverage.file`.
  */
dumpCoverage := {
  val cc           = autotestBuildConfig.value.getConfig("coverage")
  val dumpFilename = "jacoco.exec"
  val f =
    optPath(cc, "file")
      .filter(f => f.isDirectory && f.canWrite)
      // When dumping into a directory, make sure each file is unique
      .map(_ / s"$dumpFilename-${uuid.hashCode()}")
      .getOrElse(target.value / dumpFilename)
  sLog.value.info(s"Dumping coverage data to ${f.absolutePath}")
  coverageLoader.value.save(f, false)
  f
}

coverageLoader := {
  val log = sLog.value
  val cc  = autotestBuildConfig.value.getConfig("coverage")
  val l   = new ExecFileLoader()
  optPath(cc, "file").filter(_.canRead).foreach { f =>
    log.info(s"Loading coverage data from ${f.absolutePath}")
    if (f.isDirectory)
      f.listFiles()
        .foreach(ef => {
          log.info(s"--> ${ef.name}")
          l.load(ef)
        })
    else
      l.load(f)
  }
  cc.getStringList("hosts").asScala.foreach { h =>
    val ind = h.indexOf(':')
    val (hname, port) =
      if (ind == -1) (h, 6300) else (h.substring(0, ind), h.substring(ind + 1).toInt)
    log.info(s"Collecting coverage from $h")
    try {
      CoverageReporter.dumpCoverage(l, hname, port)
    } catch {
      case ex: Exception =>
        log.warn(s"Failed to retrieve coverage from $h. Message: ${ex.getMessage}")
    }
  }
  l
}

val saxBuilder = {
  val sb = new SAXBuilder(XMLReaders.NONVALIDATING)
  sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
  sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
  sb
}

(coverageReport / sourceDirectory) := target.value / "all_srcs"
(coverageReport / target) := {
  val cc = autotestBuildConfig.value.getConfig("coverage")
  optPath(cc, "reportdir").getOrElse(target.value / "coverage-report")
}

coverageReport := {
  val log        = sLog.value
  val io         = installOptions.value
  val execLoader = coverageLoader.value
  val allClasses = target.value / "all_classes"
  IO.delete(allClasses)
  val classFilter = new NameFilter {
    def accept(name: String): Boolean =
      if (name.startsWith("classes/"))
        relevantClasses.apply(name.substring("classes/".length).split("/"))
      else name == "plugin-jpf.xml"
  }
  val allPlugins = (io.installDir / "plugins" ** "*.jar").get.flatMap { jar =>
    val clzDir   = allClasses / jar.getName
    val files    = IO.unzip(jar, clzDir, filter = classFilter)
    val jpf      = saxBuilder.build(clzDir / "plugin-jpf.xml")
    val pluginId = jpf.getRootElement.getAttributeValue("id")
    if (files.size > 2)
      Some((jar.getParentFile.getName, CoveragePlugin(clzDir, pluginId)))
    else None
  }

  val srcZip  = sourceZip.value
  val allSrcs = (coverageReport / sourceDirectory).value
  srcZip.foreach(z => {
    log.info(s"Using source zip file: $z")
    log.info(s"Extracting to: ${allSrcs.absolutePath}")
    IO.unzip(z, allSrcs)
  })
  val coverageDir = (coverageReport / target).value
  log.info(s"Creating coverage report at ${coverageDir.absolutePath}")
  CoverageReporter.createReport(
    execLoader,
    allPlugins.groupBy(_._1).mapValues(_.map(_._2)).toSeq,
    coverageDir,
    allSrcs
  )
}

installEquella := {
  val opts            = installOptions.value
  val zipFile         = autotestInstallerZip.value
  val log             = sLog.value
  val installSettings = target.value / "installsettings.xml"
  zipFile.fold(sys.error("Must have install.zip set")) { z =>
    val installFiles = target.value / "installer_files"
    log.info(s"Unzipping $z")
    IO.delete(installFiles)
    IO.unzip(z, installFiles)
    val baseInstaller = (installFiles * "*").get.head
    val installerJar  = baseInstaller / "enterprise-install.jar"
    opts.writeXML(installSettings, baseInstaller)
    val o    = ForkOptions().withRunJVMOptions(Vector("-jar", installerJar.absolutePath))
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
  val run = (TestPrj / Test / runner).value
  val log = sLog.value
  run
    .run(
      "equellatests.SetupForTests",
      (TestPrj / Test / fullClasspath).value.files,
      spaceDelimited("<arg>").parsed,
      log
    )
    .get
}

configureInstall := {
  val run = (TestPrj / Test / runner).value
  run
    .run(
      "equellatests.InstallFirstTime",
      (TestPrj / Test / fullClasspath).value.files,
      Seq(),
      sLog.value
    )
    .get
}

(test / aggregate) := false

collectArtifacts := {
  val results = target.value / "test-artifacts.zip"
  def allFiles(files: Seq[File]): Traversable[(File, String)] = {
    files.flatMap(f => (f ** "*").pair(rebase(f, f.getName)))
  }
  val logsDir      = installDir.value / "logs"
  val scReportDir  = (LocalProject("Tests") / target).value / "test-reports"
  val oldReportDir = file((OldTests / testNGOutputDirectory).value)

  sLog.value.info(s"Collecting test artifacts into ${results.absolutePath}")
  IO.zip(
    allFiles(Seq(logsDir, scReportDir, oldReportDir, (coverageReport / target).value)),
    results,
    Option((ThisBuild / buildTimestamp).value)
  )
  results
}

(Global / concurrentRestrictions) := {
  val testConfig = autotestBuildConfig.value.getConfig("tests")
  if (testConfig.hasPath("maxthreads")) {
    Seq(
      Tags.limit(Tags.Test, testConfig.getInt("maxthreads"))
    )
  } else {
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
