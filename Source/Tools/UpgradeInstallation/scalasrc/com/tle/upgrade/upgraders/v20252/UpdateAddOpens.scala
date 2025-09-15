package com.tle.upgrade.upgraders.v20252

import com.dytech.edge.common.Constants.MANAGER_FOLDER
import com.tle.common.util.ExecUtils
import com.tle.upgrade.ApplicationFiles.{EQUELLA_SERVER_CONFIG_LINUX, EQUELLA_SERVER_CONFIG_WINDOWS}
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.{LineFileModifier, UpgradeResult}

import java.io.File
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Using
import scala.util.matching.Regex

private case class ConfigModifications(
    javaAddOpens: List[String],
    javaOptsRegex: Regex,
    javaOptsModifier: String => String,
    javaOptsSplitterRegex: String,
    javaOptsDelimiter: String
) {
  def removeExistingAddOpens(opts: String): String =
    opts
      .split(javaOptsSplitterRegex)
      .filterNot(_.startsWith("--add-opens="))
      .mkString(javaOptsDelimiter)
}

/** This upgrade adds necessary --add-opens JVM options to the oEQ server configuration files
  * (equella-server.config for Linux and equella-server.bat for Windows) to allow access to certain
  * Java internal APIs that may be restricted by default in newer Java versions. This is essential
  * for compatibility with libraries like XStream and JNDI that require access to these internal
  * APIs.
  *
  * The upgrade checks if the JAVA_ADDOPENS variable is already present in the configuration files.
  * If not, it adds the JAVA_ADDOPENS variable with the required --add-opens options and modifies
  * the existing JAVA_ARGS variable to include JAVA_ADDOPENS.
  *
  * This upgrade is specifically designed for installations upgrading to version 2025.2, as later
  * versions will already have these settings in place. Therefore, this upgrader can be removed in
  * future releases after 2025.2, assuming users follow the recommended practice of upgrading one
  * major version at a time.
  */
class UpdateAddOpens extends AbstractUpgrader {

  override def getId: String = "UpdateAddOpens20252"

  // Versions after 2025.2 will never match this upgrader as JAVA_ADDOPENS will already be in place.
  // So it can be removed after 2025.2. (On the assumption that people follow the advice of upgrading
  // one major version at a time.)
  override def canBeRemoved: Boolean = true

  override def upgrade(result: UpgradeResult, installDir: File): Unit = {
    val managerDir                         = new File(installDir, MANAGER_FOLDER)
    def configFile(filename: String): File = new File(managerDir, filename)
    def windowsConfigFile: File            = configFile(EQUELLA_SERVER_CONFIG_WINDOWS)
    def linuxConfigFile: File              = configFile(EQUELLA_SERVER_CONFIG_LINUX)

    ExecUtils.determinePlatform() match {
      case ExecUtils.PLATFORM_WIN64 if requiresModification(windowsConfigFile) =>
        updateWindowsConfig(windowsConfigFile, result)
      case ExecUtils.PLATFORM_LINUX64 if requiresModification(linuxConfigFile) =>
        updateLinuxConfig(linuxConfigFile, result)
      case ExecUtils.PLATFORM_WIN64 | ExecUtils.PLATFORM_LINUX64 =>
        result.addLogMessage(
          "No JVM '--add-opens' modification required for oEQ server configuration files."
        )
      case unknownPlatform =>
        result.info(s"Unknown platform: $unknownPlatform")

    }
  }

  private def requiresModification(configFile: File): Boolean = {
    val alreadyUpdated = configFile.exists() && Using(Source.fromFile(configFile)) { src =>
      src.getLines().exists(_.contains("JAVA_ADDOPENS"))
    }.getOrElse(false)

    !alreadyUpdated
  }

  private def updateWindowsConfig(configFile: File, result: UpgradeResult): Unit = {
    val modifications = ConfigModifications(
      javaAddOpens = List(
        "set JAVA_ADDOPENS=^",
        "--add-opens=java.base/java.io=ALL-UNNAMED;^",
        "--add-opens=java.base/java.lang.ref=ALL-UNNAMED;^",
        "--add-opens=java.base/java.lang=ALL-UNNAMED;^",
        "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED;^",
        "--add-opens=java.base/java.util=ALL-UNNAMED;^",
        "--add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED;^",
        "--add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED;^",
        "--add-opens=java.naming/javax.naming.directory=ALL-UNNAMED;^",
        "--add-opens=java.naming/javax.naming.ldap=ALL-UNNAMED;^",
        "--add-opens=java.naming/javax.naming=ALL-UNNAMED"
      ),
      javaOptsRegex = "set JAVA_ARGS=(.*)".r,
      javaOptsModifier = (opts: String) => s"set JAVA_ARGS=$opts" + ";%JAVA_ADDOPENS%",
      javaOptsSplitterRegex = ";",
      javaOptsDelimiter = ";"
    )

    updateConfigFile(configFile, modifications, result)
  }

  private def updateLinuxConfig(configFile: File, result: UpgradeResult): Unit = {
    val modifications = ConfigModifications(
      javaAddOpens = List(
        "export JAVA_ADDOPENS=\"",
        "  --add-opens=java.base/java.io=ALL-UNNAMED",
        "  --add-opens=java.base/java.lang.ref=ALL-UNNAMED",
        "  --add-opens=java.base/java.lang=ALL-UNNAMED",
        "  --add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
        "  --add-opens=java.base/java.util=ALL-UNNAMED",
        "  --add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED",
        "  --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED",
        "  --add-opens=java.naming/javax.naming.directory=ALL-UNNAMED",
        "  --add-opens=java.naming/javax.naming.ldap=ALL-UNNAMED",
        "  --add-opens=java.naming/javax.naming=ALL-UNNAMED\""
      ),
      javaOptsRegex = "export JAVA_OPTS=\"(.*)\"".r,
      javaOptsModifier = (opts: String) => s"export JAVA_OPTS=\"$opts" + " $JAVA_ADDOPENS\"",
      javaOptsSplitterRegex = "\\s+",
      javaOptsDelimiter = " "
    )

    updateConfigFile(configFile, modifications, result)
  }

  private def updateConfigFile(
      configFile: File,
      modifications: ConfigModifications,
      result: UpgradeResult
  ): Unit = {
    val configFileUpdater = new LineFileModifier(configFile, result) {
      override protected def processLineMulti(line: String): java.util.List[String] = {
        val javaOptsPattern: Regex = modifications.javaOptsRegex
        val updates = line match {
          case javaOptsPattern(opts) =>
            val cleanedOpts = modifications.removeExistingAddOpens(opts)
            modifications.javaAddOpens ++ List("", modifications.javaOptsModifier(cleanedOpts))
          case _ => List(line)
        }
        updates.asJava
      }

      // This method is not used because we override processLineMulti
      override protected def processLine(line: String): String = ???
    }

    configFileUpdater.update()
  }
}
