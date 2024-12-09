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

package com.tle.web.api.cloudprovider

import cats.data.Validated.{Invalid, Valid}
import com.tle.common.institution.CurrentInstitution
import com.tle.core.cloudproviders._
import com.tle.core.validation.EntityValidation.collectErrors
import com.tle.legacy.LegacyGuice
import com.tle.web.api.ApiErrorResponse.{badRequest, resourceNotFound}
import com.tle.web.api.settings.SettingsApiHelper.ensureEditSystem
import com.tle.web.api.{ApiHelper, EntityPaging}
import com.tle.web.settings.SettingsList
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.parsing.UrlParser
import io.swagger.annotations.{Api, ApiOperation}

import javax.ws.rs._
import javax.ws.rs.core._
import org.jboss.resteasy.annotations.cache.NoCache

import scala.util.Success
import java.util.UUID

case class CloudProviderForward(url: String)
@NoCache
@Api("Cloud Providers")
@Path("cloudprovider")
@Produces(value = Array(MediaType.APPLICATION_JSON))
class CloudProviderApi {

  import CloudProviderApi._

  val registrationService = LegacyGuice.cloudProviderRegistrationService
  val entityService       = LegacyGuice.entityService

  @POST
  @Path("register")
  @ApiOperation(
    value = "Register a cloud provider",
    response = classOf[CloudProviderRegistrationResponse]
  )
  def register(
      @QueryParam(TokenParam) @DefaultValue("") regtoken: String,
      registration: CloudProviderRegistration
  ): Response = {
    def forwardUrl: String =
      UriBuilder
        .fromUri(CurrentInstitution.get.getUrlAsUri)
        .path(SettingsList.CloudProviderListPage)
        .build()
        .toString

    registrationService
      .register(regtoken, registration)
      .map(CloudProviderRegistrationResponse(_, forwardUrl))
      .map(Response.ok(_).build)
      .leftMap(collectErrors)
      .valueOr(badRequest(_: _*))
  }

  @POST
  @Path("register/init")
  @ApiOperation(
    value = "Generate a cloud provider registration URL",
    notes =
      "Given a URL to a cloud provider, generate a response with that URL and two extra parameters.\n" +
        "'institution' - The institution URL to register against\n" +
        "'register' - a relative URI which the cloud provider should post it's registration to."
  )
  def prepareRegistration(
      @QueryParam("url") @DefaultValue("") providerUrl: String,
      @Context uriInfo: UriInfo
  ): CloudProviderForward = {
    checkPermissions()
    UrlParser.parseUrl(providerUrl) match {
      case Success(u: Url) =>
        def returnUrl: String =
          ApiHelper
            .apiUriBuilder()
            .path(classOf[CloudProviderApi])
            .path(classOf[CloudProviderApi], "register")
            .queryParam(TokenParam, registrationService.createRegistrationToken)
            .build()
            .toString

        ensureEditSystem()
        CloudProviderForward(
          u.addParam(RegistrationParam, returnUrl)
            .addParam(InstUrl, LegacyGuice.urlService.getBaseInstitutionURI.toString)
            .toString
        )
      case _ => throw new BadRequestException("Invalid provider registration url")
    }
  }

  @PUT
  @Path("provider/{uuid}")
  @ApiOperation(value = "Edit a cloud provider's service details")
  def editServiceDetails(
      @PathParam("uuid") uuid: UUID,
      registration: CloudProviderRegistration
  ): Response = {
    checkPermissions()
    Option(entityService.getByUuid(uuid.toString))
      .map(registrationService.editRegistered(_, registration))
      .map(
        _.map(_ => Response.noContent.build)
          .leftMap(collectErrors)
          .valueOr(badRequest(_: _*))
      )
      .getOrElse(resourceNotFound(s"Failed to find Cloud provider matching UUID: $uuid"))
  }

  @DELETE
  @Path("provider/{uuid}")
  @ApiOperation(value = "Delete a cloud provider")
  def deleteRegistration(@PathParam("uuid") uuid: UUID): Response = {
    checkPermissions()
    Option(entityService.getByUuid(uuid.toString))
      .map(entityService.delete)
      .map(_ => Response.noContent().build())
      .getOrElse(resourceNotFound(s"Not Cloud Provider matching UUID: $uuid"))
  }

  @POST
  @Path("provider/{uuid}/refresh")
  @ApiOperation(value = "Refresh a cloud provider")
  def refreshRegistration(@PathParam("uuid") uuid: UUID): Response = {
    checkPermissions()
    Option(entityService.getByUuid(uuid.toString))
      .map(registrationService.refreshRegistration)
      .map {
        case Invalid(error)   => Response.serverError.entity(error.mkString("\n")).build
        case Valid(refreshed) => Response.ok(refreshed).build()
      }
      .getOrElse(resourceNotFound(s"Failed to find Cloud provider matching UUID: $uuid"))
  }

  @GET
  @Path("")
  @ApiOperation("List current cloud providers")
  def list(): EntityPaging[CloudProviderDetails] = {
    checkPermissions()
    registrationService.getAllProviders
      .map(EntityPaging.allResults)
      .leftMap(collectErrors)
      .valueOr(s => throw new BadRequestException(s.mkString))
  }
}

object CloudProviderApi {
  final val TokenParam        = "token"
  final val InstUrl           = "institution"
  final val RegistrationParam = "register"
}
