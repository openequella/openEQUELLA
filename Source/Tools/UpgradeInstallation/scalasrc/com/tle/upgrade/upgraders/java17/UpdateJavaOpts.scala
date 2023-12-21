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

package com.tle.upgrade.upgraders.java17

import com.dytech.edge.common.Constants
import com.tle.common.util.ExecUtils
import com.tle.upgrade.{LineFileModifier, UpgradeResult}
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.upgraders.AbstractUpgrader.{
  EQUELLA_SERVER_CONFIG_LINUX,
  EQUELLA_SERVER_CONFIG_WINDOWS
}

import java.io.File
import scala.util.{Failure, Success, Try}

/**
  * This upgrade drops JVM option 'CMSInitiatingOccupancyFraction' which is removed in Java 17.
  * It also adds JVM option '--add-opens=java.base/java.util=ALL-UNNAMED' as a workaround to support
  * some libraries like XStream which is not fully compatible with Java 17 by 12/12/2023.
  */
class UpdateJavaOpts extends AbstractUpgrader {

  override def getId: String = "UpdateJavaOpts"

  override def isBackwardsCompatible: Boolean = false

  override def upgrade(result: UpgradeResult, installDir: File): Unit = {
    val managerDir = new File(installDir, Constants.MANAGER_FOLDER)

    def update(configFile: String): Unit = {
      val updateResult = Try {
        new LineFileModifier(new File(managerDir, configFile), result) {
          override protected def processLine(line: String): String = {
            val RemovedOption =
              ".*(-XX:CMSInitiatingOccupancyFraction=\\d+).*".r

            line match {
              case RemovedOption(option) =>
                line.replace(option, "--add-opens=java.base/java.util=ALL-UNNAMED")
              case _ => line
            }
          }
        }.update()
      }

      updateResult match {
        case Success(_)     => result.info("Successfully updated Java options.")
        case Failure(error) => result.info(s"Failed to update Java options: ${error.getMessage}")
      }
    }

    ExecUtils.determinePlatform() match {
      case ExecUtils.PLATFORM_WIN64 =>
        update(EQUELLA_SERVER_CONFIG_WINDOWS)
      case ExecUtils.PLATFORM_LINUX64 =>
        update(EQUELLA_SERVER_CONFIG_LINUX)
      case other => result.info(s"Unsupported OS $other")
    }
  }
}
