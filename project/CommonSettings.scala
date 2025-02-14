import com.typesafe.config.Config
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object CommonSettings extends AutoPlugin {
  object autoImport {
    lazy val versionProperties = taskKey[File]("Version property file")
    lazy val upgradeZip        = taskKey[File]("Create upgrade zip")
    lazy val installerZip      = taskKey[File]("Create the installer zip")
    lazy val equellaMajor      = settingKey[Int]("The major equella version")
    lazy val equellaMinor      = settingKey[Int]("The minor equella version")
    lazy val equellaPatch      = settingKey[Int]("The patch equella version")
    lazy val equellaStream     = settingKey[String]("The equella stream name")
    lazy val equellaBuild      = settingKey[String]("The equella build version")
    lazy val equellaVersion    = settingKey[EquellaVersion]("The full equella version")
    lazy val bundleOracleDriver =
      settingKey[Boolean]("The flag used to indicate if oracle driver is needed or not")
    lazy val oracleDriverMavenCoordinate =
      settingKey[Seq[ModuleID]]("The Maven coordinate of Oracle JDBC")
    lazy val buildConfig       = settingKey[Config]("The build configuration settings")
    lazy val buildTimestamp    = settingKey[Long]("Timestamp - in seconds - to use for this build")
    lazy val prepareDevConfig  = taskKey[Unit]("Prepare the dev learningedge-config folder")
    lazy val writeSourceZip    = taskKey[File]("Write out a zip containing all sources")
    lazy val langStrings       = taskKey[Seq[LangStrings]]("Fully qualified language strings")
    lazy val writeLanguagePack = taskKey[File]("Write the default language pack")
    lazy val writeScriptingJavadoc = taskKey[File]("Write the scripting javadoc")
    lazy val mergeJPF              = inputKey[Unit]("Merge all")
    lazy val buildReactFrontEnd    = taskKey[File]("Build the ReactJS based front-end")
    lazy val oeqTsRestApiDir =
      settingKey[File]("The base directory of the oEQ Typescript REST module project")
    lazy val reactFrontEndDir = settingKey[File]("The base directory of the ReactJS project")
    lazy val reactFrontEndOutputDir =
      settingKey[File]("The output/target directory of the ReactJS project")
    lazy val reactFrontEndLanguageBundle =
      settingKey[File]("The language bundle file for the ReactJS front-end project")

    lazy val platformCommon  = LocalProject("com_tle_platform_common")
    lazy val platformSwing   = LocalProject("com_tle_platform_swing")
    lazy val platformEquella = LocalProject("com_tle_platform_equella")
    lazy val postgresDep     = "org.postgresql"          % "postgresql" % "42.7.3"
    lazy val sqlServerDep    = "com.microsoft.sqlserver" % "mssql-jdbc" % "11.2.1.jre8"

    lazy val log4jVersion   = "2.23.1"
    lazy val log4j          = "org.apache.logging.log4j" % "log4j"             % log4jVersion
    lazy val log4jCore      = "org.apache.logging.log4j" % "log4j-core"        % log4jVersion
    lazy val log4jSlf4jImpl = "org.apache.logging.log4j" % "log4j-slf4j2-impl" % log4jVersion

    lazy val springVersion = "5.3.34"
    lazy val springWeb     = "org.springframework" % "spring-web"     % springVersion
    lazy val springAop     = "org.springframework" % "spring-aop"     % springVersion
    lazy val springContext = "org.springframework" % "spring-context" % springVersion

    lazy val xstreamVersion = "1.4.21"
    lazy val xstreamDep     = "com.thoughtworks.xstream" % "xstream" % xstreamVersion

    lazy val jacksonVersion  = "2.15.3"
    lazy val jacksonDataBind = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
    lazy val jacksonDataFormatYaml =
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion
    lazy val jacksonModuleScala =
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  }

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = HeaderPlugin && JvmPlugin
  override def projectSettings = Seq(
    organization := "com.github.equella",
    scalaVersion := "2.13.13",
    scalacOptions ++= Seq("-Vimplicits"),
    javacOptions ++= Seq("--release", "21"),
    compileOrder := CompileOrder.Mixed,
    headerLicense := Some(
      HeaderLicense.Custom(
        """|Licensed to The Apereo Foundation under one or more contributor license
           |agreements. See the NOTICE file distributed with this work for additional
           |information regarding copyright ownership.
           |
           |The Apereo Foundation licenses this file to you under the Apache License,
           |Version 2.0, (the "License"); you may not use this file except in compliance
           |with the License. You may obtain a copy of the License at:
           |
           |    http://www.apache.org/licenses/LICENSE-2.0
           |
           |Unless required by applicable law or agreed to in writing, software
           |distributed under the License is distributed on an "AS IS" BASIS,
           |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
           |See the License for the specific language governing permissions and
           |limitations under the License.
           |""".stripMargin
      )
    ),
    resolvers ++= Seq(
      Resolver.bintrayRepo("omegat-org", "maven")
    ),
    libraryDependencies ++= Seq(
      "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
      "org.scalatest" %% "scalatest"       % "3.2.18" % Test
    )
  )
}
