package com.tle.upgrade.upgraders.v20252

import com.tle.common.util.ExecUtils
import com.tle.upgrade.ApplicationFiles.{EQUELLA_SERVER_CONFIG_LINUX, EQUELLA_SERVER_CONFIG_WINDOWS}
import com.tle.upgrade.UpgradeResult
import org.apache.commons.logging.LogFactory
import org.mockito.Mockito
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.InputStream
import java.nio.file.{Files, Path}
import scala.io.Source
import scala.util.Using

class UpdateAddOpensTest extends AnyFunSpec with Matchers with GivenWhenThen {
  describe("UpdateAddOpens") {
    it("updates files that require modification on Linux") {
      Given("an installation directory with a Linux config file that needs modification")
      val installDir = setupInstallDir()

      When("the upgrader is run")
      runUpgrader(installDir)

      Then("the Linux config file should be updated correctly")
      validateUpdates(installDir, EQUELLA_SERVER_CONFIG_LINUX, "after_equellaserver-config.sh")

      removeInstalldir(installDir)
    }

    it("updates files that require modification on Windows") {
      asWindows(() => {
        Given("an installation directory with a Windows config file that needs modification")
        val installDir = setupInstallDir()

        When("the upgrader is run")
        runUpgrader(installDir)

        Then("the Windows config file should be updated correctly")
        validateUpdates(installDir, EQUELLA_SERVER_CONFIG_WINDOWS, "after_equellaserver-config.bat")

        removeInstalldir(installDir)
      })
    }

    it("does not modify files that do not require modification") {
      Given("an installation directory with config files that do not need modification")
      val installDir = Files.createTempDirectory("installDir")
      val managerDir = Files.createDirectory(installDir.resolve("manager"))
      copyResourceFile("nothing-to-update.sh", managerDir.resolve(EQUELLA_SERVER_CONFIG_LINUX))

      When("the upgrader is run")
      runUpgrader(installDir)

      Then("the config files should remain unchanged")
      validateUpdates(installDir, EQUELLA_SERVER_CONFIG_LINUX, "nothing-to-update.sh")

      removeInstalldir(installDir)
    }
  }

  private def removeInstalldir(installDir: Path): Unit = {
    System.out.println("Cleaning up temporary directory: " + installDir)

    Files
      .walk(installDir)
      .sorted(java.util.Comparator.reverseOrder())
      .forEach(Files.delete)
  }

  private def runUpgrader(installDir: Path): Unit = {
    val upgrader = new UpdateAddOpens()
    val result   = new UpgradeResult(LogFactory.getLog(classOf[UpdateAddOpensTest]))
    upgrader.upgrade(result, installDir.toFile)
  }

  private def validateUpdates(
      installDir: Path,
      configFile: String,
      expectedResource: String
  ): Unit = {
    val configPath      = installDir.resolve(s"manager/$configFile")
    val configContent   = Files.readString(configPath)
    val expectedContent = readResourceFile(expectedResource)

    // Because we can't mock System (and thereby can't mock System.lineSeparator()), we normalize
    // line endings to avoid test failures due to different line endings on different OSes.
    val normalizedConfigContent   = normalizeLineEndings(configContent)
    val normalizedExpectedContent = normalizeLineEndings(expectedContent)

    normalizedConfigContent shouldBe normalizedExpectedContent
  }

  private def normalizeLineEndings(content: String): String = {
    content.replaceAll("\r\n", "\n").replaceAll("\r", "\n")
  }

  private def setupInstallDir(): Path = {
    val installDir = Files.createTempDirectory("installDir")
    val managerDir = Files.createDirectory(installDir.resolve("manager"))

    Seq(
      (EQUELLA_SERVER_CONFIG_LINUX, "before_equellaserver-config.sh"),
      (EQUELLA_SERVER_CONFIG_WINDOWS, "before_equellaserver-config.bat")
    ).foreach { case (filename, resourcePath) =>
      copyResourceFile(resourcePath, managerDir.resolve(filename))
    }

    System.out.println("Created temporary installation directory: " + installDir)
    installDir
  }

  private def copyResourceFile(relativeResourcePath: String, targetPath: Path): Unit = {
    val resourceStream = getResourceFile(relativeResourcePath)
    Files.copy(resourceStream, targetPath)
  }

  private def readResourceFile(relativeResourcePath: String): String = {
    val resourceStream = getResourceFile(relativeResourcePath)
    Source.fromInputStream(resourceStream).mkString
  }

  private def getResourceFile(relativeResourcePath: String): InputStream = {
    val resourceStream =
      getClass.getClassLoader.getResourceAsStream("UpdateAddOpensTest/" + relativeResourcePath)
    require(resourceStream != null, s"Resource not found: $relativeResourcePath")

    resourceStream
  }

  def asWindows(test: () => Unit): Unit = {
    Using(Mockito.mockStatic(classOf[ExecUtils])) { execUtilsMock =>
      execUtilsMock.when(() => ExecUtils.determinePlatform()).thenReturn(ExecUtils.PLATFORM_WIN64)
      test()
    }.get
  }
}
