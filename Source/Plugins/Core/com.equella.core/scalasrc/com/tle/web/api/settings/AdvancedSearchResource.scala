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
import com.tle.web.api.ApiErrorResponse.resourceNotFound
import com.tle.web.api.entity.BaseEntitySummary
import com.tle.web.api.language.LanguageStringHelper.getStringFromCurrentLocale
import com.tle.web.api.wizard.WizardControlHelper.wizardControlConverter
import com.tle.web.api.wizard._
import io.swagger.annotations.{Api, ApiModelProperty, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, PathParam, Produces}
import scala.jdk.CollectionConverters._

case class AdvancedSearch(
    name: Option[String],
    description: Option[String],
    collections: List[BaseEntitySummary],
    // WizardControlDefinition is an abstraction without real data structure so use 'ApiModelProperty'
    // to provide a concrete structure.
    @ApiModelProperty(dataType = "com.tle.web.api.wizard.WizardBasicControl")
    controls: List[WizardControlDefinition]
)

/** API for managing Advanced Searches (internally - and historically - known as Power Searches).
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
    response = classOf[BaseEntitySummary],
    responseContainer = "List"
  )
  def getAll: Response =
    Response
      .ok()
      .entity(
        powerSearchService
          .enumerateSearchable()
          .asScala
          .map(be => BaseEntitySummary(be))
      )
      .build()

  @GET
  @Path("{uuid}")
  @ApiOperation(
    value = "Get Advanced Search definition",
    notes =
      "This endpoint is used to retrieve an Advanced Search's name, description, Collections and Wizard definition by UUID.",
    response = classOf[AdvancedSearch]
  )
  def getAdvancedSearchWizardDefinition(@PathParam("uuid") uuid: String): Response = {
    Option(powerSearchService.getByUuid(uuid)) match {
      case Some(ps) =>
        val controls    = wizardControlConverter(ps.getWizard.getControls.asScala.toList)
        val collections = ps.getItemdefs.asScala.map(c => BaseEntitySummary(c)).toList
        val name        = getStringFromCurrentLocale(ps.getName)
        val description = getStringFromCurrentLocale(ps.getDescription)
        Response
          .ok()
          .entity(AdvancedSearch(name, description, collections, controls))
          .build()
      case None => resourceNotFound(s"Failed to find Advanced search for ID: ${uuid}")
    }
  }
}
