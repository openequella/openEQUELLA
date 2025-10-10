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

package com.tle.upgrade.upgraders.ffmpeg

import com.dytech.edge.common.Constants
import com.google.common.collect.Lists
import com.tle.common.util.EquellaConfig
import com.tle.upgrade.{LineFileModifier, PropertyFileModifier, UpgradeResult}
import com.tle.upgrade.upgraders.AbstractUpgrader

import java.io.{File, IOException}
import java.util
import java.util.regex.Pattern

/** This migration is intended to replace tool libav with ffmpeg.
  *
  * File to be updated:
  *   1. 'optional-config.properties' under folder `learningedge-config`;
  *
  * The result of this migration is to replace property `libav.path`` with `ffmpeg.path`
  */
class AddFfmpegConfig extends AbstractUpgrader {

  override def getId: String = "AddFfmpegConfig"

  override def canBeRemoved: Boolean = true

  @throws[Exception]
  override def upgrade(result: UpgradeResult, tleInstallDir: File): Unit = {
    val config: EquellaConfig = new EquellaConfig(tleInstallDir)
    result.addLogMessage("Updating optional-config properties to support use of FFmpeg")
    updateOptionalProperties(result, config.getConfigDir)
  }

  private def updateOptionalProperties(result: UpgradeResult, configDir: File): Unit = {
    try {
      new LineFileModifier((new File(configDir, PropertyFileModifier.OPTIONAL_CONFIG)), result) {
        val libavCommentPattern = "^#.*Libav path. For example C:/Libav/usr/bin.*"
        val libavPathPattern    = ".*libav.path =.*"

        override protected def processLine(line: String): String = {
          line match {
            case l if Pattern.matches(libavCommentPattern, l) => null
            case l if Pattern.matches(libavPathPattern, l)    => null
            case _                                            => line
          }
        }

        override protected def addLines: util.List[String] = {
          val ffmpegComment: String = "# FFmpeg path"
          val ffmpegProp: String    = "#ffmpeg.path = /path/to/ffmpeg"
          Lists.newArrayList(Constants.BLANK, ffmpegComment, ffmpegProp)
        }
      }.update()
    } catch {
      case e: IOException =>
        throw new RuntimeException(
          "Failed to update optional-config.properties to support use of FFmpeg.",
          e
        )
    }
  }
}
