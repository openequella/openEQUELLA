import java.io.FileNotFoundException
import java.util.Properties
import Path.rebase
import com.typesafe.sbt.license.LicenseReport
import sbt.io.Using

import java.time.Instant
import scala.collection.JavaConverters._

lazy val learningedge_config = project in file("Dev/learningedge-config")

lazy val allPlugins      = LocalProject("allPlugins")
lazy val allPluginsScope = ScopeFilter(inAggregates(allPlugins, includeRoot = false))
val legacyPaths = Seq(
  (Compile / javaSource) := baseDirectory.value / "src",
  (Test / javaSource) := baseDirectory.value / "test",
  (Compile / unmanagedResourceDirectories) := (baseDirectory.value / "resources") :: Nil,
  (Compile / unmanagedSourceDirectories) := (Compile / javaSource).value :: Nil,
  (Test / unmanagedSourceDirectories) := (Test / javaSource).value :: Nil
)

lazy val autotest = project in file("autotest")
lazy val equellaserver =
  (project in file("Source/Server/equellaserver")).enablePlugins(JPFRunnerPlugin)

lazy val adminTool = (project in file("Source/Server/adminTool"))
  .settings(legacyPaths)
  .dependsOn(
    platformSwing,
    platformEquella,
    LocalProject("com_tle_webstart_admin"),
    LocalProject("adminConsoleJar")
  )

lazy val conversion = (project in file("Source/Server/conversion"))
  .settings(legacyPaths)
  .dependsOn(
    platformCommon
  )

lazy val UpgradeInstallation = (project in file("Source/Tools/UpgradeInstallation"))
  .settings(legacyPaths)
  .dependsOn(
    platformCommon,
    platformEquella,
    log4jCustom
  )

lazy val UpgradeManager = (project in file("Source/Tools/UpgradeManager"))
  .settings(legacyPaths)
  .dependsOn(platformCommon, platformEquella, log4jCustom)

lazy val Installer = (project in file("Installer"))
  .settings(legacyPaths)
  .dependsOn(platformCommon, platformSwing, platformEquella, UpgradeManager)

lazy val equella = (project in file("."))
  .enablePlugins(JPFScanPlugin, JarSignerPlugin, GitVersioning)
  .aggregate(equellaserver,
             allPlugins,
             adminTool,
             Installer,
             UpgradeManager,
             conversion,
             UpgradeInstallation,
             learningedge_config)

checkJavaCodeStyle := {
  import com.etsy.sbt.checkstyle._
  val rootDirectory       = (LocalProject("equella") / baseDirectory).value
  val rootTargetDirectory = (LocalProject("equella") / target).value
  def countErrorNumber: Int = {
    val outputFile = new File("target/checkstyle-report.xml")
    if (outputFile.exists()) {
      val report = scala.xml.XML.loadFile(outputFile)
      (report \\ "file" \ "error").length
    } else {
      throw new FileNotFoundException("checkstyle report is missing")
    }
  }
  // As we will specify where the config file is, the resource file can be null
  // We don't want to exit SBT if checkstyle finds any issue, so severityLevel should be None
  Checkstyle.checkstyle(
    javaSource = rootDirectory,
    resources = null,
    outputFile = rootTargetDirectory / "checkstyle-report.xml",
    configLocation = CheckstyleConfigLocation.File("checkstyle-config.xml"),
    xsltTransformations = Some(
      Set(CheckstyleXSLTSettings(rootDirectory / "checkstyle-report-template.xml",
                                 rootTargetDirectory / "checkstyle-report.html"))),
    severityLevel = None,
    streams = streams.value
  )
  val errorNumber     = countErrorNumber
  val thresholdNumber = 569
  if (errorNumber > thresholdNumber) {
    throw new MessageOnlyException(
      "Checkstyle error threshold (" + thresholdNumber + ") exceeded with error count of " + errorNumber)
  }
}

(ThisBuild / bundleOracleDriver) := {
  val path = "build.bundleOracleDriver"
  if (buildConfig.value.hasPath(path)) {
    buildConfig.value.getBoolean(path)
  } else {
    false
  }
}
(ThisBuild / oracleDriverMavenCoordinate) := Seq("com.oracle.ojdbc" % "ojdbc8" % "19.3.0.0")

(ThisBuild / buildConfig) := Common.buildConfig

name := "Equella"

(ThisBuild / equellaMajor) := 2021
(ThisBuild / equellaMinor) := 2
(ThisBuild / equellaPatch) := 0
(ThisBuild / equellaStream) := "Alpha"
(ThisBuild / equellaBuild) := buildConfig.value.getString("build.buildname")
(ThisBuild / buildTimestamp) := Instant.now().getEpochSecond

version := {
  val shortCommit = git.gitHeadCommit.value.map { sha =>
    "g" + sha.take(7)
  }.get

  EquellaVersion(equellaMajor.value,
                 equellaMinor.value,
                 equellaPatch.value,
                 s"${equellaStream.value}.${equellaBuild.value}",
                 shortCommit).fullVersion
}

(ThisBuild / equellaVersion) := EquellaVersion(version.value)

(ThisBuild / versionProperties) := {
  val eqVersion = equellaVersion.value
  val props     = new Properties
  props.putAll(
    Map(
      "version.display" -> s"${eqVersion.semanticVersion}-${eqVersion.releaseType}",
      "version.commit"  -> eqVersion.sha
    ).asJava)
  val f = target.value / "version.properties"
  IO.write(props, "version", f)
  f
}

updateLicenses := {
  val ourOrg         = organization.value
  val serverReport   = (equellaserver / updateLicenses).value
  val plugsinReports = updateLicenses.all(allPluginsScope).value
  val allLicenses = (plugsinReports.flatMap(_.licenses) ++ serverReport.licenses)
    .groupBy(_.module)
    .values
    .map(_.head)
    .filterNot(_.module.organization == ourOrg)
  LicenseReport(allLicenses.toSeq, serverReport.orig)
}

writeLanguagePack := {
  IO.withTemporaryDirectory { dir =>
    val allProps = langStrings
      .all(allPluginsScope)
      .value
      .flatten
      .groupBy(ls => (ls.group, ls.xml))
      .map {
        case ((g, xml), lss) =>
          val fname = g + (if (xml) ".xml" else ".properties")
          val f     = dir / fname
          val p     = new SortedProperties()
          lss.foreach(ls => p.putAll(ls.strings.asJava))
          Using.fileOutputStream()(f) { os =>
            if (xml) p.storeToXML(os, "") else p.store(os, "")
          }
          (f, fname)
      }
    val outZip = target.value / "reference-language-pack.zip"
    sLog.value.info(s"Writing ${outZip.absolutePath}")
    IO.zip(allProps, outZip, Option((ThisBuild / buildTimestamp).value))
    outZip
  }
}

(dumpLicenseReport / aggregate) := false

(Global / cancelable) := true

val pluginAndLibs = Def.task {
  val bd      = baseDirectory.value
  val jpfLibs = jpfLibraryJars.value
  (bd, jpfLibs)
}

mergeJPF := {

  import complete.DefaultParsers._

  val adminConsole = false
  val args         = spaceDelimited("<arg>").parsed
  val _allPluginDirs =
    pluginAndLibs.all(allPluginsScope).value
  val extensionsOnly =
    (baseDirectory.value / "Source/Plugins/Extensions" * "*" / "plugin-jpf.xml").get
  val allPluginDirs = _allPluginDirs ++ extensionsOnly.map(f => (f.getParentFile, Seq.empty))
  if (args.isEmpty) {
    val plugins = PluginRefactor.findPluginsToMerge(allPluginDirs, adminConsole = adminConsole)
    println(s"mergeJPF <ID> ${plugins.mkString(" ")}")
  } else {
    val newPlugin  = args.head
    val basePlugin = baseDirectory.value / "Source/Plugins"
    PluginRefactor.mergePlugins(allPluginDirs,
                                basePlugin,
                                newPlugin,
                                args.tail,
                                adminConsole = adminConsole)
  }
}

writeScriptingJavadoc := {
  val javadocDir = (Compile / doc).value
  val ver        = version.value
  val outZip     = target.value / s"scriptingapi-javadoc-$ver.zip"
  sLog.value.info(s"Writing ${outZip.absolutePath}")
  IO.zip((javadocDir ** "*").pair(rebase(javadocDir, "")),
         outZip,
         Option((ThisBuild / buildTimestamp).value))
  outZip
}

ThisBuild / reactFrontEndDir := baseDirectory.value / "react-front-end"
ThisBuild / reactFrontEndOutputDir := reactFrontEndDir.value / "target/resources"
ThisBuild / buildReactFrontEnd := {
  val dir = reactFrontEndDir.value
  Common.nodeInstall(dir)
  Common.nodeScript("build", dir)

  // return the location of the resulting artefacts
  reactFrontEndOutputDir.value
}
ThisBuild / reactFrontEndLanguageBundle := reactFrontEndOutputDir.value / "lang/jsbundle.json"

// Add to the clean to ensure we clean out the react-front-end
clean := {
  clean.value
  Common.nodeScript("clean", reactFrontEndDir.value)
}

val userBeans: FileFilter = ("GroupBean.java" || "UserBean.java" || "RoleBean.java") &&
  new SimpleFileFilter(_.getParentFile.getName == "valuebean")

// Globs came from the original ant scripts

def javadocSources(base: File): PathFinder = {
  (base / "src") ** ("package-info.java" || "*ScriptType.java"
  || "*ScriptObject.java" || userBeans)
}

(Compile / doc / aggregate) := false
(Compile / doc / sources) := {
  (javadocSources((LocalProject("com_equella_base") / baseDirectory).value)
    +++ javadocSources((LocalProject("com_equella_core") / baseDirectory).value)).get
}
(Compile / doc / javacOptions) := Seq()

lazy val allEquella = ScopeFilter(inAggregates(equella))

lazy val devrebuild = taskKey[Unit]("clean and build all code - targeting a local dev run")

devrebuild := {
  Def
    .sequential(
      clean.all(allEquella),
      jpfWriteDevJars.all(allPluginsScope),
      (Compile / fullClasspath).all(allEquella),
    )
    .value
}

lazy val cleanrun = taskKey[Unit]("clean, build and run a dev server")

cleanrun := {
  Def
    .sequential(
      devrebuild,
      (equellaserver / run).toTask("")
    )
    .value
}
