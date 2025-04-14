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

package com.tle.upgrade.upgraders.log4j2

import cats.data.ValidatedNec
import cats.implicits._
import com.tle.upgrade.upgraders.log4j2.PropertyHelper.{readBooleanProperty, readProperty}

import java.util.Properties

sealed trait Layout
case class PatternLayout(Pattern: String) extends Layout
case class HTMLLayout(
    title: Option[String],
    datePattern: Option[String] = None,
    locationInfo: Option[Boolean]
) extends Layout

object Layout {

  /** Build an Appender layout based on the supplied configuration.
    *
    * @param layoutKey
    *   The property key used to define the layout.
    * @param props
    *   Property file which provides details of the layout.
    * @return
    *   `ValidatedNec` where left is a list of error messages and right is the layout.
    */
  def getLayout(layoutKey: String, props: Properties): ValidatedNec[String, Layout] = {
    readProperty(layoutKey, props)
      .toRight(s"Failed to find layout for $layoutKey")
      .flatMap {
        case "org.apache.log4j.PatternLayout" =>
          readProperty(s"$layoutKey.ConversionPattern", props)
            .map(PatternLayout)
            .toRight(s"Failed to find layout pattern for $layoutKey")

        case "org.apache.log4j.HTMLLayout" | "com.dytech.common.log4j.HTMLLayout2" |
            "com.tle.core.equella.runner.HTMLLayout3" =>
          val title = readProperty(s"$layoutKey.title", props)
          val locationInfo =
            readBooleanProperty(s"$layoutKey.LocationInfo", props)
          val datePattern = readProperty(s"$layoutKey.datePattern", props)
          Right(HTMLLayout(title, datePattern, locationInfo))

        case unsupported => Left(s"Unsupported layout $unsupported")
      }
      .toValidatedNec
  }
}
