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

import com.tle.legacy.LegacyGuice
import com.tle.web.api.entity.BaseEntitySummary
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

@Path("settings/remotesearch/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class RemoteSearchResource {
  private val federatedSearchService = LegacyGuice.federatedSearchService

  @GET
  @ApiOperation(
    value = "List available Remote (Federated) Searches",
    notes =
      "This endpoint is used to retrieve available Remote Searches and is secured by SEARCH_FEDERATED_SEARCH",
    response = classOf[BaseEntitySummary],
    responseContainer = "List"
  )
  def getAll: Response =
    Response
      .ok()
      .entity(
        federatedSearchService.enumerateSearchable().asScala.map(be => BaseEntitySummary(be))
      )
      .build()
}
