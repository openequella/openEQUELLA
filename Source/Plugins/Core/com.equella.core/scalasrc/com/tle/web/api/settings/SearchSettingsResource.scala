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

import com.tle.common.settings.standard.SearchSettings
import com.tle.legacy.LegacyGuice
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.{GET, PUT, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache
import com.tle.web.api.settings.SettingsApiHelper.{loadSettings, updateSettings}

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class SearchSettingsResource {

  @GET
  @Path("search")
  @ApiOperation(
    value = "List Search settings",
    notes = "This endpoint is used to retrieve general search settings excluding search filters."
  )
  def listSearchSettings: SearchSettings = {
    LegacyGuice.searchPrivProvider.checkAuthorised()
    loadSettings(new SearchSettings)
  }

  @PUT
  @Path("search")
  @ApiOperation(
    value = "Update Search settings",
    notes = "This endpoint is used to update general search settings excluding search filters."
  )
  def updateSearchSettings(newSettings: SearchSettings): Unit = {
    LegacyGuice.searchPrivProvider.checkAuthorised()

    // The updated settings only include all general settings, so manually add search filters
    val oldSettings = loadSettings(new SearchSettings)
    newSettings.getFilters.addAll(oldSettings.getFilters)
    updateSettings(newSettings)
  }
}
