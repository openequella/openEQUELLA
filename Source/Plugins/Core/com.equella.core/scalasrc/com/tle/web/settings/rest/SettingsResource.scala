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

package com.tle.web.settings.rest

import java.net.URI
import java.util

import javax.ws.rs.{GET, PUT, Path, Produces}
import com.tle.common.institution.CurrentInstitution
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.security.AclChecks
import com.tle.web.settings.{EditableSettings, SettingsList, UISettings}
import io.swagger.annotations.Api

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

case class SettingTypeLinks(@BeanProperty web: URI, @BeanProperty rest: URI)
case class SettingType(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty description: String,
                       @BeanProperty group: String, @BeanProperty links: SettingTypeLinks)

object SettingTypeLinks {
  def apply(instUri: URI, ed: EditableSettings): SettingTypeLinks = ed.uriType match {
    case "rest" => SettingTypeLinks(null, instUri.resolve(ed.uri))
    case _ => SettingTypeLinks(instUri.resolve(ed.uri), null : URI)
  }
}

@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SettingsResource {

  @GET
  def settings : util.Collection[SettingType] = {
    SettingsList.allSettings.filter(_.isEditable).map { s =>
      val instUri = CurrentInstitution.get().getUrlAsUri
      SettingType(s.id, s.name, s.description, s.group, SettingTypeLinks(instUri, s))
    }.asJavaCollection
  }

  def ensureEditSystem[A](db: DB[A]): DB[A] = AclChecks.ensureOnePriv("EDIT_SYSTEM_SETTINGS")(db)

  @GET
  @Path("ui")
  def getUISettings: UISettings = RunWithDB.execute(ensureEditSystem(UISettings.getUISettings)).getOrElse(UISettings.defaultSettings)

  @PUT
  @Path("ui")
  def setUISettings(in: UISettings) : Unit = RunWithDB.executeWithPostCommit(ensureEditSystem(UISettings.setUISettings(in)))
}
