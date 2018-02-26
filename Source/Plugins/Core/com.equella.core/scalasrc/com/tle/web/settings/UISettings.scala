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
import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.tle.core.cache.{Cache, InstCacheable}
import com.tle.core.db.DB
import com.tle.core.settings.SettingsDB
import io.circe.generic.auto._
import cats.syntax.apply._
import scala.collection.JavaConverters._

import scala.beans.BeanProperty

case class FacetSetting @JsonCreator() (@JsonProperty("name") @BeanProperty name: String, @JsonProperty("path") @BeanProperty path: String)
case class NewUISettings(@BeanProperty enabled: Boolean, facets: Option[Iterable[FacetSetting]]) {
  @JsonCreator
  def this(@JsonProperty("enabled") enabled: Boolean, @JsonProperty("facets") facets: Array[FacetSetting]) = {
    this(enabled, Option(facets).map(_.toIterable))
  }

  def getFacets = facets.getOrElse(Iterable.empty).asJavaCollection
}

case class UISettings @JsonCreator() (@JsonProperty("newUI") @BeanProperty newUI: NewUISettings)

object UISettings {

  private val UIPropName = "ui"

  val defaultSettings = UISettings(NewUISettings(false, None))

  val getUISettings : DB[Option[UISettings]] = SettingsDB.jsonProperty[UISettings](UIPropName).value

  implicit val cacheable = InstCacheable[Option[UISettings]]("uiSettings", getUISettings)

  def setUISettings(in: UISettings) : DB[IO[Unit]] = SettingsDB.setJsonProperty(UIPropName, in) *>
    Cache.invalidate[Option[UISettings]]

  def cachedUISettings : DB[Option[UISettings]] = Cache.get[Option[UISettings]]
}
