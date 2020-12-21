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

import com.tle.core.cloud.settings.CloudSettings
import com.tle.legacy.LegacyGuice
import com.tle.web.api.settings.SettingsApiHelper.{loadSettings, updateSettings}
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.{GET, PUT, Path, Produces}
import org.jboss.resteasy.annotations.cache.NoCache

@NoCache
@Path("settings/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class CloudSearchSettingsResource {

  @GET
  @Path("search/cloud")
  @ApiOperation(value = "List Cloud settings",
                notes = "This endpoint is used to retrieve cloud settings.")
  def listCloudSettings: CloudSettings = {
    LegacyGuice.searchPrivProvider.checkAuthorised()
    loadSettings(new CloudSettings)
  }

  @PUT
  @Path("search/cloud")
  @ApiOperation(value = "Update Cloud settings",
                notes = "This endpoint is used to update cloud settings.")
  def updateCloudSettings(settings: CloudSettings): Unit = {
    LegacyGuice.searchPrivProvider.checkAuthorised()
    updateSettings(settings)
  }
}
