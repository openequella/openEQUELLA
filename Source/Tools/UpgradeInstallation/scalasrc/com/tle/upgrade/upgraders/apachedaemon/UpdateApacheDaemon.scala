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
import com.tle.upgrade.upgraders.AbstractUpgrader.{
  EQUELLA_SERVER_CONFIG_LINUX,
  EQUELLA_SERVER_CONFIG_WINDOWS
}
import com.tle.upgrade.{LineFileModifier, UpgradeResult}
import java.io.File
import scala.util.{Failure, Success, Try}

/** This upgrade aims to update Apache Daemon executables to version v1.3.3 which is compatible with
  * Java 11.
  *
  * For Linux, it will replace file 'jsvc' as well as the two scripts for Equella server and the
  * Manager server. For Windows, it will replace 'prunsrv.exe', 'prunmgr.exe' and the two BAT files
  * for Equella server and the Manager server.
  *
  * The upgrade will also try to replace the old Java GC configuration with a new one in the Equella
  * configuration file.
  */
class UpdateApacheDaemon extends AbstractUpgrader {
  val JSVC    = "jsvc"
  val PRUNMGR = "prunmgr.exe"
  val PRUNSRV = "prunsrv.exe"

  val EQUELLA_SERVER_LINUX   = "equellaserver"
  val EQUELLA_SERVER_WINDOWS = "equellaserver.bat"

  val MANAGER_SERVER_LINUX   = "manager"
  val MANAGER_SERVER_WINDOWS = "manager.bat"

  // The ID of this upgrade was initially done by using `UpgradeMain.getCommit`, which results in
  // different IDs in each release. This will break the upgrade process because this upgrade is
  // mandatory so its ID MUST be consistent across all the releases; otherwise, an exception about
  // missing this upgrade will be thrown during the upgrade.
  // Considering this upgrade was implemented in 2023.1, it makes sense to use the short commit hash
  // tagged by '2023.1' as the ID.
  override def getId: String = s"UpdateApacheDaemon-g448a8ab"

  override def canBeRemoved: Boolean = false

  override def upgrade(result: UpgradeResult, installDir: File): Unit = {
    val managerDir = new File(installDir, Constants.MANAGER_FOLDER)

    def updateExecutables(files: Array[String], os: String) =
      Try {
        files.foreach(file => {
          val bakFile = new File(managerDir, s"$file.bak")
          rename(new File(managerDir, file), bakFile)
          copyResource(s"/daemon/$os/$file", managerDir, true)
          bakFile.delete()
        })
      }

    // Any version before 2023.1 may have three JVM options that have been deprecated or
    // removed since Java 11. So this upgrade will remove them.
    def updateJVMOptions(configFile: String) =
      Try {
        new LineFileModifier(new File(managerDir, configFile), result) {
          override protected def processLine(line: String): String = {
            // Regex for the three JVM options defined in the config file. Each option is separated by a
            // semicolon on Windows and a whitespace on Linux.
            // For example: -Xmx512m;-XX:MaxPermSize=256m;-XX:GCTimeRatio=16;-XX:+UseConcMarkSweepGC;-XX:+UseParNewGC)
            val JvmOptions =
              ".*(-XX:MaxPermSize=\\d+m[;|\\s]).*(-XX:\\+UseConcMarkSweepGC[;|\\s]-XX:\\+UseParNewGC).*".r

            line match {
              case JvmOptions(permSize, gc) =>
                line.replace(permSize, "").replace(gc, "-XX:+UseG1GC")
              case _ => line
            }
          }
        }.update()
      }

    def update(executables: Array[String], configFile: String, os: String): Unit = {
      val updateResult = for {
        _ <- updateExecutables(executables, os)
        _ <- updateJVMOptions(configFile)
      } yield ()

      updateResult match {
        case Success(_)     => result.info("Successfully update Apache Daemon.")
        case Failure(error) => result.info(s"Failed to update Apache Daemon: ${error.getMessage}")
      }
    }

    ExecUtils.determinePlatform() match {
      case ExecUtils.PLATFORM_WIN64 =>
        update(
          Array(PRUNMGR, PRUNSRV, EQUELLA_SERVER_WINDOWS, MANAGER_SERVER_WINDOWS),
          EQUELLA_SERVER_CONFIG_WINDOWS,
          "windows"
        )
      case ExecUtils.PLATFORM_LINUX64 =>
        update(
          Array(JSVC, EQUELLA_SERVER_LINUX, MANAGER_SERVER_LINUX),
          EQUELLA_SERVER_CONFIG_LINUX,
          "linux"
        )
      case other => result.info(s"Unsupported OS $other")
    }
  }
}
