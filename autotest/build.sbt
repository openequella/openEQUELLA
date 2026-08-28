import com.typesafe.config.{Config, ConfigFactory}
import de.johoop.testngplugin.TestNGPlugin
import org.jacoco.core.tools.ExecFileLoader
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt.complete.DefaultParsers.spaceDelimited

import scala.sys.process._
import Path.rebase
import cats.instances.uuid

import scala.jdk.CollectionConverters._

// application.conf has to be on the test classpath, because TestConfig reads it with
// ConfigFactory.load(). AUTOTEST_CONFIG still wins over it, via the config.file system property set
// below.
Test / unmanagedResourceDirectories += baseDirectory.value / "config" / "resources"

// The supplementary services some tests need: a mock LMS integration and an HTTP echo service.
// A separate project because it has main sources and its own front-end build, rather than tests.
lazy val IntegTester = project in file("IntegTester")

val circeVersion  = "0.14.12"
val http4sVersion = "0.23.36"
val catsVersion   = "2.13.0"

libraryDependencies += "org.jacoco"  % "org.jacoco.agent" % "0.8.15" classifier "runtime"
libraryDependencies += "com.opencsv" % "opencsv"          % "5.12.0"

libraryDependencies ++= Seq(
  "org.testng" % "testng" % "7.12.0" % Test,
  // The older Log4j is required by dependency "oclc-harvester2" at runtime.
  "log4j"              % "log4j"              % "1.2.17" % Test,
  "commons-httpclient" % "commons-httpclient" % "3.1"    % Test,
  // Drives the property-based suites under `equellatests`. ScalaTest itself comes from
  // CommonSettings; scalacheck-1-19 is the ScalaTest 3.2.20 <-> ScalaCheck 1.19 bridge.
  "org.scalacheck"    %% "scalacheck"      % "1.19.0"   % Test,
  "org.scalatestplus" %% "scalacheck-1-19" % "3.2.20.0" % Test
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

// The Selenium framework and page-object library these suites are built on.
libraryDependencies ++= Seq(
  "javax.jws"                 % "javax.jws-api"        % "1.1",
  "org.apache.commons"        % "commons-lang3"        % "3.20.0",
  "org.apache.commons"        % "commons-collections4" % "4.6.0",
  "org.seleniumhq.selenium"   % "selenium-java"        % "4.46.0",
  "com.codeborne"             % "selenide"             % "7.17.0",
  "xalan"                     % "xalan"                % "2.7.3",
  "xalan"                     % "serializer"           % "2.7.3",
  "org.apache.httpcomponents" % "httpclient"           % "4.5.14",
  "com.jcraft"                % "jsch"                 % "0.1.55",
  "org.jacoco"                % "org.jacoco.report"    % "0.8.15",
  "org.dspace"                % "oclc-harvester2"      % "1.0.0",
  "com.typesafe"              % "config"               % "1.4.9",
  "org.apache.logging.log4j"  % "log4j"                % log4jVersion,
  "org.apache.logging.log4j"  % "log4j-core"           % log4jVersion,
  "org.apache.logging.log4j"  % "log4j-slf4j2-impl"    % log4jVersion,
  "org.http4s" %% "http4s-blaze-client" % "0.23.18", // The latest version of blzae client is still 0.23.17 by 13/05/2025.
  "org.http4s"    %% "http4s-circe"      % http4sVersion,
  "org.typelevel" %% "cats-free"         % catsVersion,
  "com.unboundid"  % "unboundid-ldapsdk" % "7.0.5",
  jacksonDataBind,
  jacksonDataFormatYaml,
  "com.auth0" % "jwks-rsa" % "0.24.1"
)

enablePlugins(TestNGPlugin)

// This project holds two kinds of end-to-end test: the long-standing TestNG suites, and newer suites
// written with ScalaTest (currently the property-based ones under `equellatests`, but ScalaTest is
// where all new tests should go). They need separate tasks rather than one merged `test`, because CI
// shards the TestNG suites into several parallel jobs — anything sharing the `Test` config would be
// re-run by every shard.
//
// The split is by framework rather than by suite name, so a new ScalaTest suite is picked up wherever
// it lives:
//
//   test            -> TestNG only
//   ScalaTest/test  -> ScalaTest only
lazy val ScalaTest = config("scalatest") extend Test

configs(ScalaTest)

inConfig(ScalaTest)(Defaults.testTasks)

// sbt auto-generates a `configuration` setting key for every custom Configuration and flags it as
// unused by lintUnused, even though the ScalaTest config itself is actively used.
Global / excludeLintKeys += ScalaTest / configuration

// sbt-testng-plugin appends to testFrameworks, so without these both configs would otherwise
// discover both frameworks.
val testNGFramework = TestFramework("de.johoop.testnginterface.TestNGFramework")

Test / testFrameworks      := Seq(testNGFramework)
ScalaTest / testFrameworks := Seq(TestFrameworks.ScalaTest)

testNGOutputDirectory := (target.value / "testng").absolutePath

testNGParameters ++= Seq("-log", autotestBuildConfig.value.getInt("tests.verbose").toString)

// Suite names are configured bare (e.g. "all.yaml") and resolved here, so that the
// `suites` folder is this build's business rather than something every config file has to know.
testNGSuites := {
  val tc = autotestBuildConfig.value.getConfig("tests")
  tc.getStringList("suitenames").asScala.map(n => (baseDirectory.value / "suites" / n).absolutePath)
}

Test / testOptions += {
  val outDir = testNGOutputDirectory.value
  Tests.Setup(() => System.setProperty("testng.output.dir", outDir))
}
Test / testOptions += Tests.Cleanup(() => System.clearProperty("testng.output.dir"))

// Mirror TestAnnotationTransformer's UI filtering for ScalaTest suites. That class reads the same
// environment variable to disable @NewUIOnly/@OldUIOnly TestNG tests; both annotations carry
// ScalaTest's @TagAnnotation so the tag name is the annotation's fully qualified class name.
val NewUiOnlyTag = "testng.annotation.NewUIOnly"
val OldUiOnlyTag = "testng.annotation.OldUIOnly"

ScalaTest / testOptions += {
  // Boolean.parseBoolean, as used by TestAnnotationTransformer, accepts only "true" ignoring case.
  val newUiEnabled = sys.env.get("OLD_TEST_NEWUI").exists(_.equalsIgnoreCase("true"))
  Tests.Argument(TestFrameworks.ScalaTest, "-l", if (newUiEnabled) OldUiOnlyTag else NewUiOnlyTag)
}

// JUnit XML, so results reach CI's test report alongside TestNG's own junitreports. Absolute
// because sbt's working directory is the build root rather than this project.
ScalaTest / testOptions += Tests.Argument(
  TestFrameworks.ScalaTest,
  "-u",
  (target.value / "scalatest-junitreports").absolutePath
)

// The TestNG suites have long been run strictly sequentially (see `threadCount: 1` in
// suites/all.yaml), and every config that is actually used sets `tests.parallel = false`.
// Honouring the same key for both keeps the ScalaTest suites in step, which is what isolates the ones
// that mutate institution-wide state from the TestNG suites sharing that institution.
Test / parallelExecution      := autotestBuildConfig.value.getBoolean("tests.parallel")
ScalaTest / parallelExecution := autotestBuildConfig.value.getBoolean("tests.parallel")

(ThisBuild / autotestBuildConfig) := {
  val defaultConfig = ConfigFactory.parseFile(file("autotest/config/defaults.conf"))
  val configFile    = file(
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
  val f            =
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
    val ind           = h.indexOf(':')
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
(coverageReport / target)          := {
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

setupForTests := {
  val run = (Test / runner).value
  val log = sLog.value
  run
    .run(
      "equellatests.SetupForTests",
      (Test / fullClasspath).value.files,
      spaceDelimited("<arg>").parsed,
      log
    )
    .get
}

configureInstall := {
  val run = (Test / runner).value
  run
    .run(
      "equellatests.InstallFirstTime",
      (Test / fullClasspath).value.files,
      Seq(),
      sLog.value
    )
    .get
}

collectArtifacts := {
  val results                                                 = target.value / "test-artifacts.zip"
  def allFiles(files: Seq[File]): Traversable[(File, String)] = {
    files.flatMap(f => (f ** "*").pair(rebase(f, f.getName)))
  }
  val logsDir = installDir.value / "logs"
  // Where TestConfig.getResultsFolder writes screenshots and failed-test artefacts.
  val scReportDir  = target.value / "test-reports"
  val oldReportDir = file(testNGOutputDirectory.value)

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
