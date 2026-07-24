package com.tle.upgrade.upgraders.v20252

import com.tle.common.util.ExecUtils
import com.tle.upgrade.ApplicationFiles.{EQUELLA_SERVER_CONFIG_LINUX, EQUELLA_SERVER_CONFIG_WINDOWS}
import com.tle.upgrade.upgraders.{OeqDirs, UpgraderTest}
import org.mockito.Mockito
import java.nio.file.{Files, Path}
import scala.util.Using

class UpdateAddOpensTest extends UpgraderTest("UpdateAddOpensTest") {
  describe("UpdateAddOpens") {
    it("updates files that require modification on Linux") {
      Given("an installation directory with a Linux config file that needs modification")
      val installDir = setupInstallDir()

      When("the upgrader is run")
      runUpgrader(new UpdateAddOpens(), installDir)

      Then("the Linux config file should be updated correctly")
      validateUpdates(installDir, EQUELLA_SERVER_CONFIG_LINUX, "after_equellaserver-config.sh")

      removeInstallDir(installDir)
    }

    it("updates files that require modification on Windows") {
      asWindows(() => {
        Given("an installation directory with a Windows config file that needs modification")
        val installDir = setupInstallDir()

        When("the upgrader is run")
        runUpgrader(new UpdateAddOpens(), installDir)

        Then("the Windows config file should be updated correctly")
        validateUpdates(installDir, EQUELLA_SERVER_CONFIG_WINDOWS, "after_equellaserver-config.bat")

        removeInstallDir(installDir)
      })
    }

    it("does not modify files that do not require modification") {
      Given("an installation directory with config files that do not need modification")
      val installDir = Files.createTempDirectory("installDir")
      val managerDir = Files.createDirectory(installDir.resolve("manager"))
      copyResourceFile("nothing-to-update.sh", managerDir.resolve(EQUELLA_SERVER_CONFIG_LINUX))

      When("the upgrader is run")
      runUpgrader(new UpdateAddOpens(), installDir)

      Then("the config files should remain unchanged")
      validateUpdates(installDir, EQUELLA_SERVER_CONFIG_LINUX, "nothing-to-update.sh")

      removeInstallDir(installDir)
    }
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

  private def setupInstallDir(): Path = {
    val OeqDirs(installDir, _, managerDir) = setupOeqDirs()
    Seq(
      (EQUELLA_SERVER_CONFIG_LINUX, "before_equellaserver-config.sh"),
      (EQUELLA_SERVER_CONFIG_WINDOWS, "before_equellaserver-config.bat")
    ).foreach { case (filename, resourcePath) =>
      copyResourceFile(resourcePath, managerDir.resolve(filename))
    }

    installDir
  }

  def asWindows(test: () => Unit): Unit = {
    Using(Mockito.mockStatic(classOf[ExecUtils])) { execUtilsMock =>
      execUtilsMock.when(() => ExecUtils.determinePlatform()).thenReturn(ExecUtils.PLATFORM_WIN64)
      test()
    }.get
  }
}
