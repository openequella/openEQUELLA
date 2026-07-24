package com.tle.upgrade.upgraders.v20261

import com.dytech.edge.common.Constants
import com.tle.upgrade.upgraders.{OeqDirs, UpgraderTest}

import java.nio.file.{Files, Path}

class AddSystemExecuteAllowlistConfigTest
    extends UpgraderTest("AddSystemExecuteAllowlistConfigTest") {
  describe("AddSystemExecuteAllowlistConfig") {
    it("appends the allow-list configuration to an existing optional-config.properties") {
      Given("an installation directory with an existing optional-config.properties")
      val installDir = setupInstallDir()

      When("the upgrader is run")
      runUpgrader(new AddSystemExecuteAllowlistConfig(), installDir)

      Then("the allow-list configuration should be appended to optional-config.properties")
      val configContent   = Files.readString(optionalConfigPath(installDir))
      val expectedContent = readResourceFile("after_optional-config.properties")

      normalizeLineEndings(configContent) shouldBe normalizeLineEndings(expectedContent)

      removeInstallDir(installDir)
    }
  }

  private def setupInstallDir(): Path = {
    val OeqDirs(installDir, configDir, _) = setupOeqDirs()
    Files.writeString(configDir.resolve("mandatory-config.properties"), Constants.BLANK)
    copyResourceFile("before_optional-config.properties", optionalConfigPath(installDir))
    installDir
  }
}
