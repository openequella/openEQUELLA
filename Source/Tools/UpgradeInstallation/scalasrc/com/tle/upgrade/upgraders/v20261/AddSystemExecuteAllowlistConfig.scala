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

package com.tle.upgrade.upgraders.v20261

import com.dytech.edge.common.Constants
import com.google.common.collect.Lists
import com.tle.common.util.EquellaConfig
import com.tle.upgrade.upgraders.AbstractUpgrader
import com.tle.upgrade.{LineFileModifier, PropertyFileModifier, UpgradeResult}

import java.io.File
import java.util
import scala.util.{Failure, Success, Try}

/** Adds the `system.execute.allowedExecutables` property to 'optional-config.properties'. This
  * property is an operator-managed allow-list of absolute executable paths that scripts are
  * permitted to invoke via `system.execute()` / `system.executeInBackground()`; an empty list
  * denies all executables.
  */
class AddSystemExecuteAllowlistConfig extends AbstractUpgrader {

  override def getId: String = "AddSystemExecuteAllowlistConfig"

  override def canBeRemoved: Boolean = false

  override def upgrade(result: UpgradeResult, tleInstallDir: File): Unit = {
    result.addLogMessage(
      "Adding the system.execute allow-list"
    )

    val config: EquellaConfig = new EquellaConfig(tleInstallDir)
    Try {
      new LineFileModifier(
        new File(config.getConfigDir, PropertyFileModifier.OPTIONAL_CONFIG),
        result
      ) {
        override protected def processLine(line: String): String = line

        override protected def addLines(): util.List[String] = {
          val comment: String =
            "# Comma separated list of absolute paths to executables that scripts are permitted" +
              " to run via system.execute()/system.executeInBackground(). Empty (the default)" +
              " denies all executables."
          val prop: String = "#system.execute.allowedExecutables ="
          Lists.newArrayList(Constants.BLANK, comment, prop)
        }
      }.update()
    } match {
      case Success(_) =>
        result.info("Successfully added the configuration of system.execute allow-list.")
      case Failure(error) =>
        result.info(s"Failed to add system.execute allow-list: ${error.getMessage}")
    }
  }
}
