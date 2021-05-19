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

package com.tle.web.settings

import com.fasterxml.jackson.annotation.JsonIgnore
import com.tle.common.institution.CurrentInstitution
import io.circe.syntax._
import com.tle.legacy.LegacyGuice
import io.circe.generic.auto._
import io.circe.parser.parse

case class NewUISettings(enabled: Boolean, newSearch: Boolean = false)

case class UISettings(newUI: NewUISettings) {
  @JsonIgnore
  def isNewSearchActive: Boolean = newUI.enabled && newUI.newSearch
}

object UISettings {

  private val UIPropName = "ui"

  val defaultSettings = UISettings(NewUISettings(enabled = false))

  def getUISettings: UISettings = {
    Option(CurrentInstitution.get()) match {
      case Some(_) =>
        // `Circe parse` returns a Either where left is ParsingFailure and right is Json.
        // So we decode the value of right to UISettings by `Circe as`.
        parse(LegacyGuice.configService.getProperty(UIPropName))
          .flatMap(_.as[UISettings])
          .getOrElse(defaultSettings)
      case None => defaultSettings
    }
  }

  def setUISettings(settings: UISettings): Unit = {
    LegacyGuice.configService.setProperty(UIPropName, settings.asJson.noSpaces)
  }
}
