package com.tle.upgrade.upgraders

import com.dytech.edge.common.Constants
import com.tle.upgrade.{UpgradeResult, Upgrader}
import org.apache.commons.logging.LogFactory
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.InputStream
import java.nio.file.{Files, Path}
import scala.io.Source

/** The directories of a temporary EQUELLA installation created for a test.
  *
  * @param installDir
  *   the root installation directory
  * @param configDir
  *   the `learningedge-config` directory
  * @param managerDir
  *   the `manager` directory
  */
final case class OeqDirs(
    installDir: Path,
    configDir: Path,
    managerDir: Path
)

/** Common scaffolding for upgrader tests that exercise an upgrader against a temporary installation
  * directory.
  */
abstract class UpgraderTest(resourceFolder: String)
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen {

  protected def runUpgrader(upgrader: Upgrader, installDir: Path): Unit = {
    val result = new UpgradeResult(LogFactory.getLog(getClass))
    upgrader.upgrade(result, installDir.toFile)
  }

  /** Builds the temporary installation directory this test's upgrader runs against. */
  def setupOeqDirs(): OeqDirs = {
    val installDir = Files.createTempDirectory("installDir")
    val configDir  = Files.createDirectory(installDir.resolve(Constants.LEARNINGEDGE_CONFIG_FOLDER))
    val managerDir = Files.createDirectory(installDir.resolve(Constants.MANAGER_FOLDER))

    OeqDirs(
      installDir,
      configDir,
      managerDir
    )
  }

  protected def removeInstallDir(installDir: Path): Unit = {
    Files
      .walk(installDir)
      .sorted(java.util.Comparator.reverseOrder())
      .forEach(Files.delete)
  }

  protected def normalizeLineEndings(content: String): String =
    content.replaceAll("\r\n", "\n").replaceAll("\r", "\n")

  protected def copyResourceFile(relativeResourcePath: String, targetPath: Path): Unit =
    Files.copy(getResourceFile(relativeResourcePath), targetPath)

  protected def readResourceFile(relativeResourcePath: String): String =
    Source.fromInputStream(getResourceFile(relativeResourcePath)).mkString

  protected def optionalConfigPath(installDir: Path): Path =
    installDir.resolve("learningedge-config/optional-config.properties")

  private def getResourceFile(relativeResourcePath: String): InputStream = {
    val resourceStream =
      getClass.getClassLoader.getResourceAsStream(s"$resourceFolder/$relativeResourcePath")
    require(resourceStream != null, s"Resource not found: $relativeResourcePath")

    resourceStream
  }
}
