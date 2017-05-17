import java.util.Properties
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

lazy val platformCommon = LocalProject("com_tle_platform_common")
lazy val platformSwing = LocalProject("com_tle_platform_swing")
lazy val platformEquella = LocalProject("com_tle_platform_equella")
lazy val log4jCustom = LocalProject("com_tle_log4j")

lazy val adminTool = (project in file("Source/Server/adminTool")).settings(legacyPaths).dependsOn(
  platformSwing,
  platformEquella,
  LocalProject("com_tle_webstart_admin")
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

lazy val equella = (project in file(".")).enablePlugins(JPFScanPlugin).aggregate(equellaserver, allPlugins, adminTool, Installer,
  UpgradeManager, conversion, UpgradeInstallation)

buildConfig in ThisBuild := Common.buildConfig

oracleDriverJar in ThisBuild := {
  val c = buildConfig.value
  if (c.hasPath("build.oraclejar")) {
    Some(file(c.getString("build.oraclejar")))
  } else None
}

name := "Equella"

versionProperties in ThisBuild := {
  val props = new Properties
  props.putAll(
    Map("version.mm" -> majorVersion.value,
        "version.mmr" -> version.value,
        "version.display" -> "6.4-Alpha",
        "version.commit" -> "3a75a23").asJava)
  val f = target.value / "version.properties"
  IO.write(props, "version", f)
  f
}