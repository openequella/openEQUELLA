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

package com.tle.web.api.settings.FacetedSearch

import java.util.Date

import com.tle.common.Check
import com.tle.common.institution.CurrentInstitution
import com.tle.core.facetedsearch.bean.FacetedSearchClassification
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.{DELETE, GET, POST, PUT, Path, PathParam, Produces}
import org.jboss.resteasy.annotations.cache.NoCache

import scala.collection.mutable.ArrayBuffer

@NoCache
@Path("settings/facetedsearch/classification")
@Produces(value = Array("application/json"))
@Api(value = "Settings")
class FacetedSearchClassificationResource {
  val service          = LegacyGuice.facetedSearchClassificationService
  val privilegeChecker = LegacyGuice.searchPrivProvider

  @GET
  @ApiOperation(
    value = "List faceted search classifications",
    notes = "This endpoint is used to retrieve faceted search classifications.",
    response = classOf[FacetedSearchClassification],
    responseContainer = "List"
  )
  def getAll: Response = {
    privilegeChecker.checkAuthorised()
    val classifications = service.enumerateAll()
    Response.ok().entity(classifications).build()
  }

  @GET
  @Path("/{id}")
  @ApiOperation(
    value = "Retrieve one faceted search classification",
    notes = "This endpoint is used to retrieve one faceted search classification.",
    response = classOf[FacetedSearchClassification]
  )
  def get(@ApiParam("ID") @PathParam("id") id: Long): Response = {
    privilegeChecker.checkAuthorised()
    Option(service.getById(id)) match {
      case Some(classification) => Response.ok().entity(classification).build()
      case None                 => ApiErrorResponse.resourceNotFound(notFound(id))
    }
  }

  @POST
  @ApiOperation(
    value = "Add one faceted search classification",
    notes = "This endpoint is used to add one faceted search classification.",
    response = classOf[FacetedSearchClassification]
  )
  def add(classification: FacetedSearchClassification): Response = {
    privilegeChecker.checkAuthorised()
    validate(classification) match {
      case Left(errors) => ApiErrorResponse.badRequest(errors: _*)
      case Right(_) =>
        classification.setDateCreated(new Date())
        classification.setDateModified(new Date())
        classification.setInstitution(CurrentInstitution.get)
        service.add(classification)
        Response.status(Status.CREATED).entity(classification).build()
    }
  }

  @PUT
  @Path("/{id}")
  @ApiOperation(
    value = "Update one faceted search classification",
    notes = "This endpoint is used to update one faceted search classification.",
    response = classOf[FacetedSearchClassification]
  )
  def update(@ApiParam("ID") @PathParam("id") id: Long,
             jsonBody: FacetedSearchClassification): Response = {
    privilegeChecker.checkAuthorised()
    Option(service.getById(id)) match {
      case Some(classification) =>
        validate(jsonBody) match {
          case Left(errors) => ApiErrorResponse.badRequest(errors: _*)
          case Right(_) =>
            classification.setName(jsonBody.getName)
            classification.setMaxResults(jsonBody.getMaxResults)
            classification.setOrderIndex(jsonBody.getOrderIndex)
            classification.setSchemaNode(jsonBody.getSchemaNode)
            classification.setDateModified(new Date())
            service.update(classification)
            Response.ok().entity(classification).build()
        }
      case None => ApiErrorResponse.resourceNotFound(notFound(id))
    }
  }

  @DELETE
  @Path("/{id}")
  @ApiOperation(
    value = "Delete one faceted search classification",
    notes = "This endpoint is used to delete one faceted search classifications.",
  )
  def delete(@ApiParam(value = "ID") @PathParam("id") id: Long): Response = {
    privilegeChecker.checkAuthorised()
    Option(service.getById(id)) match {
      case Some(classification) =>
        service.delete(classification)
        Response.ok().build()
      case None => ApiErrorResponse.resourceNotFound(notFound(id))
    }
  }

  private def notFound(id: Long) = s"No Faceted Search classification matching ID: $id."

  private def validate(classification: FacetedSearchClassification) = {
    val errorMessages = ArrayBuffer[String]()

    if (Check.isEmpty(classification.getName)) {
      errorMessages += "Name cannot be empty."
    }

    if (Check.isEmpty(classification.getSchemaNode)) {
      errorMessages += "Schema node cannot be empty."
    }

    if (errorMessages.nonEmpty) Left(errorMessages.toArray) else Right()
  }
}
