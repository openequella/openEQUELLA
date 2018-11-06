import com.typesafe.config.Config
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object CommonSettings extends AutoPlugin {
  object autoImport {
    lazy val versionProperties = taskKey[File]("Version property file")
    lazy val upgradeZip = taskKey[File]("Create upgrade zip")
    lazy val installerZip = taskKey[File]("Create the installer zip")
    lazy val equellaMajorMinor = settingKey[String]("The major minor equella version")
    lazy val equellaStream = settingKey[String]("The equella stream name")
    lazy val equellaBuild = settingKey[String]("The equella build version")
    lazy val equellaVersion = settingKey[EquellaVersion]("The full equella version")
    lazy val oracleDriverJar = settingKey[Option[File]]("The oracle driver jar")
    lazy val buildConfig = settingKey[Config]("The build configuration settings")
    lazy val prepareDevConfig = taskKey[Unit]("Prepare the dev learningedge-config folder")
    lazy val writeSourceZip = taskKey[File]("Write out a zip containing all sources")
    lazy val langStrings = taskKey[Seq[LangStrings]]("Fully qualified language strings")
    lazy val writeLanguagePack = taskKey[File]("Write the default language pack")
    lazy val mergeJPF = inputKey[Unit]("Merge all")
    lazy val buildJS = taskKey[Seq[File]]("Build JS resources")

    lazy val platformCommon = LocalProject("com_tle_platform_common")
    lazy val platformSwing = LocalProject("com_tle_platform_swing")
    lazy val platformEquella = LocalProject("com_tle_platform_equella")
    lazy val log4jCustom = LocalProject("com_tle_log4j")
    lazy val xstreamDep = "com.thoughtworks.xstream" % "xstream" % "1.4.9"
    lazy val postgresDep = "org.postgresql" % "postgresql" % "42.1.4.jre7"
    lazy val sqlServerDep = "com.microsoft.sqlserver" % "mssql-jdbc" % "6.1.0.jre8"
  }

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = HeaderPlugin && JvmPlugin

  import autoImport._
  override def projectSettings = Seq(
    organization := "com.github.equella",
    scalaVersion := "2.12.6",
    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("io.tryp" % "splain" % "0.3.1" cross CrossVersion.patch),
    scalacOptions ++= Seq("-P:splain:implicits:true", "-P:splain:color:false"),
    javacOptions ++= Seq("-source", "1.8", "-target", "8"),
    compileOrder := CompileOrder.Mixed,
    headerLicense := Some(HeaderLicense.ALv2("2017", "Apereo")),
    resolvers ++= Seq(
      "EBI Nexus" at "http://www.ebi.ac.uk/intact/maven/nexus/content/repositories/ebi-repo/",
      Resolver.bintrayRepo("omegat-org", "maven")
    ),
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
  )
}
