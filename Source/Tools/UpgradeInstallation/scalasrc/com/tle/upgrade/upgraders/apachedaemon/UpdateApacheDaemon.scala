/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.upgrade.upgraders.apachedaemon

import com.dytech.edge.common.Constants
import com.tle.common.util.ExecUtils
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.{UpgradeMain, UpgradeResult}
import java.io.File

/**
  * This upgrade aims to update Apache Daemon executables to a version that is compatible with Java 11.
  *
  * For Linux, it will replace file 'jsvc' as well as the two scripts for Equella server and the Manager server.
  * For Windows, it will replace 'prunsrv.exe', 'prunmgr.exe' and the two BAT files for Equella server and the Manager server.
  */
class UpdateApacheDaemon extends AbstractUpgrader {
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
        updateFiles(Array("prunmgr.exe", "prunsrv.exe", "equellaserver.bat", "manager.bat"),
                    "windows")
      case ExecUtils.PLATFORM_LINUX64 =>
        updateFiles(Array("jsvc", "equellaserver", "manager"), "linux")
      case other => result.info(s"Unsupported OS $other")
    }
  }
}
