import java.io.FileNotFoundException
import java.util.Properties

import Path.rebase
import com.typesafe.sbt.license.LicenseReport
import sbt.io.Using

import scala.collection.JavaConverters._

lazy val learningedge_config = project in file("Dev/learningedge-config")

lazy val allPlugins = LocalProject("allPlugins")

val legacyPaths = Seq(
  javaSource in Compile := baseDirectory.value / "src",
  javaSource in Test := baseDirectory.value / "test",
  unmanagedResourceDirectories in Compile := (baseDirectory.value / "resources") :: Nil,
  unmanagedSourceDirectories in Compile := (javaSource in Compile).value :: Nil,
  unmanagedSourceDirectories in Test := (javaSource in Test).value :: Nil
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
  val rootDirectory       = (baseDirectory in LocalProject("equella")).value
  val rootTargetDirectory = (target in LocalProject("equella")).value
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
  val thresholdNumber = 870
  if (errorNumber > thresholdNumber) {
    println(
      "Checkstyle error threshold (" + thresholdNumber + ") exceeded with error count of " + errorNumber)
    System.exit(1)
  }
}

buildConfig in ThisBuild := Common.buildConfig

oracleDriverJar in ThisBuild := {
  val c = buildConfig.value
  if (c.hasPath("build.oraclejar")) {
    Some(file(c.getString("build.oraclejar")))
  } else None
}

name := "Equella"

equellaMajorMinor in ThisBuild := "2019.1"
equellaStream in ThisBuild := "Alpha"
equellaBuild in ThisBuild := buildConfig.value.getString("build.buildname")

git.useGitDescribe := true

val TagRegex = """(.*)-(.*)-(\d*)-(.*)""".r
git.gitTagToVersionNumber := {
  val streamName = equellaStream.value
  val majorMinor = equellaMajorMinor.value
  val buildName  = equellaBuild.value

  {
    case TagRegex(_, _, v, sha) =>
      Some(EquellaVersion(majorMinor, s"$streamName.$buildName", v.toInt, sha).fullVersion)
    case _ => None
  }
}

equellaVersion in ThisBuild := EquellaVersion(version.value)

versionProperties in ThisBuild := {
  val eqVersion = equellaVersion.value
  val props     = new Properties
  props.putAll(
    Map(
      "version.mm"      -> eqVersion.majorMinor,
      "version.mmr"     -> s"${eqVersion.majorMinor}.r${eqVersion.commits}",
      "version.display" -> s"${eqVersion.majorMinor}-${eqVersion.releaseType}",
      "version.commit"  -> eqVersion.sha
    ).asJava)
  val f = target.value / "version.properties"
  IO.write(props, "version", f)
  f
}

updateLicenses := {
  val ourOrg         = organization.value
  val serverReport   = (updateLicenses in equellaserver).value
  val plugsinReports = updateLicenses.all(ScopeFilter(inAggregates(allPlugins))).value
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
      .all(ScopeFilter(inAggregates(allPlugins, includeRoot = false)))
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
    IO.zip(allProps, outZip)
    outZip
  }
}

aggregate in dumpLicenseReport := false

cancelable in Global := true

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
    pluginAndLibs.all(ScopeFilter(inAggregates(allPlugins, includeRoot = false))).value
  val extensionsOnly =
    (baseDirectory.value / "Source/Plugins/Extensions" * "*" / "plugin-jpf.xml").get
  val allPluginDirs = _allPluginDirs ++ (extensionsOnly.map(f => (f.getParentFile, Seq.empty)))
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
  val javadocDir = (doc in Compile).value
  val ver        = version.value
  val outZip     = target.value / s"scriptingapi-javadoc-$ver.zip"
  sLog.value.info(s"Writing ${outZip.absolutePath}")
  IO.zip((javadocDir ** "*").pair(rebase(javadocDir, "")), outZip)
  outZip
}

val userBeans: FileFilter = ("GroupBean.java" || "UserBean.java" || "RoleBean.java") &&
  new SimpleFileFilter(_.getParentFile.getName == "valuebean")

// Globs came from the original ant scripts

def javadocSources(base: File): PathFinder = {
  (base / "src") ** ("package-info.java" || "*ScriptType.java"
  || "*ScriptObject.java" || userBeans)
}

aggregate in (Compile, doc) := false
sources in (Compile, doc) := {
  (javadocSources((baseDirectory in LocalProject("com_equella_base")).value)
    +++ javadocSources((baseDirectory in LocalProject("com_equella_core")).value)).get
}
javacOptions in (Compile, doc) := Seq()
