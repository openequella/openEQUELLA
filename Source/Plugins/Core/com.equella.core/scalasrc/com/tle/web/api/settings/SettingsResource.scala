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

package com.tle.web.api.settings

import java.net.URI
import java.util.UUID

import com.tle.common.institution.CurrentInstitution
import com.tle.common.settings.ConfigurationProperties
import com.tle.common.settings.standard.SearchSettings
import com.tle.common.settings.standard.SearchSettings.SearchFilter
import com.tle.core.cloud.settings.CloudSettings
import com.tle.core.db.RunWithDB
import com.tle.core.settings.SettingsDB
import com.tle.legacy.LegacyGuice
import com.tle.web.settings.{EditableSettings, SettingsList, UISettings}
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.{DELETE, GET, PUT, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache

case class SettingTypeLinks(web: Option[URI], rest: Option[URI], route: Option[String])
case class SettingType(id: String,
                       name: String,
                       description: String,
                       group: String,
                       links: SettingTypeLinks)

object SettingTypeLinks {
  def apply(instUri: URI, ed: EditableSettings): SettingTypeLinks = ed.uriType match {
    case "rest" => SettingTypeLinks(None, Option(instUri.resolve(ed.uri)), None)
    case _ =>
      SettingTypeLinks(Option(instUri.resolve(ed.uri)),
                       None,
                       if (ed.isRoute) Some("/" + ed.uri) else None)
  }
}

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SettingsResource {

  val searchPrivProvider = LegacyGuice.searchPrivProvider

  def loadSettings[T <: ConfigurationProperties](settings: T): T = {
    searchPrivProvider.checkAuthorised()
    LegacyGuice.configService.getProperties(settings)
  }

  def updateSettings[T <: ConfigurationProperties](settings: T): Unit = {
    searchPrivProvider.checkAuthorised()
    LegacyGuice.configService.setProperties(settings)
  }

  @GET
  def settings: Iterable[SettingType] = {
    val baseUri = CurrentInstitution.get().getUrlAsUri
    SettingsList.allSettings.filter(_.isEditable).map { s =>
      SettingType(s.id, s.name, s.description, s.group, SettingTypeLinks(baseUri, s))
    }
  }

  @GET
  @Path("ui")
  def getUISettings: UISettings =
    RunWithDB.execute(UISettings.getUISettings).getOrElse(UISettings.defaultSettings)

  @PUT
  @Path("ui")
  def setUISettings(in: UISettings): Unit = RunWithDB.executeWithPostCommit(
    SettingsDB.ensureEditSystem(UISettings.setUISettings(in))
  )

  @GET
  @Path("search")
  @ApiOperation(
    value = "Load Search settings",
    notes =
      "This endpoint is used to retrieve general search settings excluding search filter. The corresponding Model class is SearchSettings."
  )
  def loadSearchSettings: SearchSettings = {
    loadSettings(new SearchSettings)
  }

  @PUT
  @Path("search")
  @ApiOperation(
    value = "Update Search settings",
    notes =
      "This endpoint is used to update general search settings excluding search filter. It updates the values of fields in SearchSettings, except 'filter'."
  )
  def updateSearchSettings(settings: SearchSettings): Unit = {
    updateSettings(settings)
  }

  @GET
  @Path("search/cloud")
  @ApiOperation(
    value = "Load Cloud settings",
    notes =
      "This endpoint is used to retrieve cloud settings. The corresponding Model class is CloudSettings")
  def loadCloudSettings: CloudSettings = {
    loadSettings(new CloudSettings)
  }

  @PUT
  @Path("search/cloud")
  @ApiOperation(
    value = "Update Cloud settings",
    notes =
      "This endpoint is used to update cloud settings. It updates the values of fields in CloudSettings.")
  def updateCloudSettings(settings: CloudSettings): Unit = {
    updateSettings(settings)
  }
}
