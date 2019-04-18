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

package com.tle.web.api.cloudprovider

import java.util.UUID
import cats.syntax.functor._
import com.tle.core.cloudproviders._
import com.tle.core.db._
import com.tle.core.settings.SettingsDB
import com.tle.legacy.LegacyGuice
import com.tle.web.api.{ApiHelper, EntityPaging}
import com.tle.web.settings.SettingsList
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.parsing.UrlParser
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs._
import javax.ws.rs.core._

case class CloudProviderForward(url: String)

@Api("Cloud Providers")
@Path("cloudprovider")
@Produces(value = Array(MediaType.APPLICATION_JSON))
class CloudProviderApi {

  import CloudProviderApi._

  @POST
  @Path("register")
  @ApiOperation(value = "Register a cloud provider",
                response = classOf[CloudProviderRegistrationResponse])
  def register(@QueryParam(TokenParam) @DefaultValue("") regtoken: String,
               registration: CloudProviderRegistration): Response = {
    ApiHelper.runAndBuild {
      for {
        ctx               <- getContext
        validatedInstance <- CloudProviderDB.register(regtoken, registration)
      } yield {
        val forwardUrl = UriBuilder
          .fromUri(ctx.inst.getUrlAsUri)
          .path(SettingsList.CloudProviderListPage)
          .build()
          .toString
        ApiHelper.validationOrEntity(validatedInstance.map { inst =>
          CloudProviderRegistrationResponse(inst, forwardUrl)
        })
      }
    }
  }

  @POST
  @Path("register/init")
  @ApiOperation(
    value = "Generate a cloud provider registration URL",
    notes = "Given a URL to a cloud provider, generate a response with that URL and two extra parameters.\n" +
      "'institution' - The institution URL to register against\n" +
      "'register' - a relative URI which the cloud provider should post it's registration to."
  )
  def prepareRegistration(@QueryParam("url") @DefaultValue("") providerUrl: String,
                          @Context uriInfo: UriInfo): CloudProviderForward = {
    checkPermissions()
    UrlParser.parseUrl(providerUrl) match {
      case u: Url =>
        RunWithDB.execute {
          SettingsDB.ensureEditSystem {
            for {
              token <- CloudProviderDB.createRegistrationToken
            } yield {
              val returnUrl = ApiHelper
                .apiUriBuilder()
                .path(classOf[CloudProviderApi])
                .path(classOf[CloudProviderApi], "register")
                .queryParam(TokenParam, token)
                .build()
                .toString
              CloudProviderForward(
                u.addParam(RegistrationParam, returnUrl)
                  .addParam(InstUrl, LegacyGuice.urlService.getBaseInstitutionURI.toString)
                  .toString)
            }
          }
        }
      case _ => throw new BadRequestException("Invalid provider registration url")
    }

  }

  @PUT
  @Path("provider/{uuid}")
  @ApiOperation(value = "Edit a cloud provider's service details")
  def editServiceDetails(@PathParam("uuid") uuid: UUID,
                         registration: CloudProviderRegistration): Response = {
    checkPermissions()
    ApiHelper.runAndBuild {
      CloudProviderDB
        .editRegistered(uuid, registration)
        .map { validatedInstance =>
          ApiHelper.validationOr(validatedInstance.map { inst =>
            Response.noContent()
          })
        }
        .getOrElse(Response.status(404))
    }
  }

  @DELETE
  @Path("provider/{uuid}")
  @ApiOperation(value = "Delete a cloud provider")
  def deleteRegistration(@PathParam("uuid") uuid: UUID): Response = ApiHelper.runAndBuild {
    checkPermissions()
    CloudProviderDB.deleteRegistration(uuid).as(Response.noContent())
  }

  @GET
  @Path("")
  @ApiOperation("List current cloud providers")
  def list(): EntityPaging[CloudProviderDetails] = {
    checkPermissions()
    RunWithDB.execute {
      ApiHelper.allEntities {
        CloudProviderDB.allProviders
      }
    }
  }
}

object CloudProviderApi {
  final val TokenParam        = "token"
  final val InstUrl           = "institution"
  final val RegistrationParam = "register"
}
