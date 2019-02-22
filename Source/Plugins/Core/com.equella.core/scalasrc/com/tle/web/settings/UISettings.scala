/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import cats.effect.IO
import cats.syntax.apply._
import com.tle.core.cache.{Cache, InstCacheable}
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.settings.SettingsDB
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._

case class NewUISettings(enabled: Boolean, newSearch: Boolean = false)

case class UISettings(newUI: NewUISettings) {
  def isNewSearchActive: Boolean = newUI.enabled && newUI.newSearch
}

object UISettings {
  implicit val customConfig: Configuration = Configuration.default.withDefaults

  private val UIPropName = "ui"

  val defaultSettings = UISettings(NewUISettings(enabled = false))

  val getUISettings: DB[Option[UISettings]] = SettingsDB.jsonProperty[UISettings](UIPropName).value

  implicit val cacheable = InstCacheable[Option[UISettings]]("uiSettings", getUISettings)

  def setUISettings(in: UISettings): DB[IO[Unit]] =
    SettingsDB.setJsonProperty(UIPropName, in) *>
      Cache.invalidate[Option[UISettings]]

  def cachedUISettings: DB[Option[UISettings]] = Cache.get[Option[UISettings]]
}

object UISettingsJava {
  def getUISettings: UISettings = RunWithDB.executeWithHibernate {
    UISettings.getUISettings.map(_.getOrElse(UISettings.defaultSettings))
  }

}
