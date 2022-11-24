package com.tle.upgrade.upgraders.jsvc

import com.dytech.edge.common.Constants
import com.tle.common.util.ExecUtils
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.{UpgradeMain, UpgradeResult}
import java.io.File

class UpdateJSVC extends AbstractUpgrader {
  override def getId: String = s"UpdateJSVC-${UpgradeMain.getCommit}"

  override def isBackwardsCompatible: Boolean = true

  override def upgrade(result: UpgradeResult, installDir: File): Unit = {
    val managerDir = new File(installDir, Constants.MANAGER_FOLDER)

    def updateFiles(files: Array[String], os: String): Unit = {
      files.foreach(file => {
        val bakFile = new File(managerDir, s"$file.bak")
        rename(new File(managerDir, file), bakFile)
        copyResource(s"/daemon/$os/$file", managerDir, true)
        bakFile.delete()
      })
    }

    ExecUtils.determinePlatform() match {
      case ExecUtils.PLATFORM_WIN64 =>
        updateFiles(Array("prunmgr.exe", "prunsrv.exe"), "windows")
      case ExecUtils.PLATFORM_LINUX64 =>
        updateFiles(Array("jsvc", "equellaserver", "manager"), "linux")
      case other => result.info(s"Unsupported OS $other")
    }
  }
}
