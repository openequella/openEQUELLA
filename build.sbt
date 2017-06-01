import java.util.Properties

import com.typesafe.sbt.license.LicenseReport

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

lazy val equellaserver = (project in file("Source/Server/equellaserver")).settings(legacyPaths).enablePlugins(JPFRunnerPlugin)

lazy val adminTool = (project in file("Source/Server/adminTool")).settings(legacyPaths).dependsOn(
  platformSwing,
  platformEquella,
  LocalProject("com_tle_webstart_admin"),
  LocalProject("adminConsoleJar")
)

lazy val conversion = (project in file("Source/Server/conversion")).settings(legacyPaths).dependsOn(
  platformCommon
)

lazy val UpgradeInstallation = (project in file("Source/Tools/UpgradeInstallation")).settings(legacyPaths).dependsOn(
  platformCommon,
  platformEquella,
  log4jCustom
)

lazy val UpgradeManager = (project in file("Source/Tools/UpgradeManager")).settings(legacyPaths).dependsOn(platformCommon, platformEquella, log4jCustom)

lazy val Installer = (project in file("Installer")).settings(legacyPaths).dependsOn(platformCommon, platformSwing, platformEquella, UpgradeManager)

lazy val equella = (project in file(".")).enablePlugins(JPFScanPlugin, JarSignerPlugin, GitVersioning)
  .aggregate(equellaserver, allPlugins, adminTool, Installer,
  UpgradeManager, conversion, UpgradeInstallation, learningedge_config)

buildConfig in ThisBuild := Common.buildConfig

oracleDriverJar in ThisBuild := {
  val c = buildConfig.value
  if (c.hasPath("build.oraclejar")) {
    Some(file(c.getString("build.oraclejar")))
  } else None
}

name := "Equella"

git.useGitDescribe := true

val TagRegex = """(.*)-(.*)-(\d*)-(.*)""".r
git.gitTagToVersionNumber := {
  case TagRegex(m, t, v, sha) => Some(EquellaVersion(m, t, v.toInt, sha).fullVersion)
  case _ => None
}

equellaVersion in ThisBuild := EquellaVersion(version.value)

versionProperties in ThisBuild := {
  val eqVersion = equellaVersion.value
  val props = new Properties
  props.putAll(
    Map("version.mm" -> eqVersion.majorMinor,
        "version.mmr" -> s"${eqVersion.majorMinor}.r${eqVersion.commits}",
        "version.display" -> s"${eqVersion.majorMinor}-${eqVersion.releaseType}",
        "version.commit" -> eqVersion.sha).asJava)
  val f = target.value / "version.properties"
  IO.write(props, "version", f)
  f
}

updateLicenses := {
  val serverReport = (updateLicenses in equellaserver).value
  val plugsinReports = updateLicenses.all(ScopeFilter(inAggregates(allPlugins))).value
  val allLicenses = (plugsinReports.flatMap(_.licenses) ++ serverReport.licenses)
    .groupBy(_.module).values.map(_.head).filterNot(_.module.organization == "org.apereo.equella")
  LicenseReport(allLicenses.toSeq, serverReport.orig)
}

aggregate in dumpLicenseReport := false

cancelable in Global := true
