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

import com.tle.beans.entity.BaseEntityLabel
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiHelper
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

/**
  * A summary of key details of an Advanced Search Summary
  * @param uuid the UUID for the specific Advanced Search
  * @param name the default locale human readable name for this search
  */
case class AdvancedSearchSummary(uuid: String, name: String)

object AdvancedSearchSummary {
  private val powerSearchService = LegacyGuice.powerSearchService

  def apply(bel: BaseEntityLabel): AdvancedSearchSummary =
    AdvancedSearchSummary(uuid = bel.getUuid,
                          name = ApiHelper.getEntityName(powerSearchService.getByUuid(bel.getUuid)))
}

/**
  * API for managing Advanced Searches (internally - and historically - known as Power Searches).
  */
@Path("settings/advancedsearch/")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class AdvancedSearchResource {
  private val powerSearchService = LegacyGuice.powerSearchService

  @GET
  @ApiOperation(
    value = "List available Advanced Searches",
    notes =
      "This endpoint is used to retrieve available Advanced Searches and is secured by SEARCH_POWER_SEARCH",
    response = classOf[AdvancedSearchSummary],
    responseContainer = "List"
  )
  def getAll: Response =
    Response
      .ok()
      .entity(
        powerSearchService
          .listSearchable()
          .asScala
          .map(bel => AdvancedSearchSummary(bel)))
      .build()
}
