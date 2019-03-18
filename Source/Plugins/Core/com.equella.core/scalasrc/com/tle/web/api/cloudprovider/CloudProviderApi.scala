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

import com.tle.core.cloudproviders._
import com.tle.core.db.RunWithDB
import com.tle.core.settings.SettingsDB
import com.tle.web.api.{ApiHelper, EntityPaging}
import io.lemonlabs.uri.{Uri, Url}
import io.lemonlabs.uri.parsing.{UriParser, UrlParser}
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.{Context, MediaType, Response, UriInfo}
import javax.ws.rs._

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
      CloudProviderDB.register(regtoken, registration).map { validatedInstance =>
        ApiHelper.validationOrEntity(
          validatedInstance.map(inst => CloudProviderRegistrationResponse(inst, "http://")))
      }
    }
  }

  @POST
  @Path("register/init")
  @ApiOperation(
    value = "Generate a cloud provider registration URL",
    notes = "Given a URL to a cloud provider, generate a response with that URL and an extra parameter ('registration') containing " +
      "a valid Cloud Provider callback URL"
  )
  def prepareRegistration(@QueryParam("url") @DefaultValue("") providerUrl: String,
                          @Context uriInfo: UriInfo): CloudProviderForward = {
    UrlParser.parseUrl(providerUrl) match {
      case u: Url =>
        RunWithDB.execute {
          SettingsDB.ensureEditSystem {
            for {
              token <- CloudProviderDB.createRegistrationToken
            } yield {
              val returnUrl = uriInfo.getBaseUriBuilder
                .path(classOf[CloudProviderApi])
                .path("register")
                .queryParam(TokenParam, token)
                .build()
                .toString
              CloudProviderForward(u.addParam(RegistrationParam, returnUrl).toString)
            }
          }
        }
      case _ => throw new BadRequestException("Invalid provider registration url")
    }

  }

  @GET
  @Path("")
  @ApiOperation("List current cloud providers")
  def list(): EntityPaging[CloudProviderDetails] = {
    EntityPaging.allResults(
      Iterable(
        CloudProviderDetails(
          UUID.randomUUID(),
          "Edalex",
          Some("The Edalex cloud provider"),
          Some(
            "https://static.wixstatic.com/media/ed3f73_4a88d00cc545486eb879e2752339390e~mv2.png/v1/fill/w_454,h_331,al_c,usm_0.66_1.00_0.01/edalexcloud_edited.png")
        ),
        CloudProviderDetails(UUID.randomUUID(),
                             "Penghai solutions",
                             Some("Penghai provides clouds"),
                             None),
        CloudProviderDetails(UUID.randomUUID(), "Doolsoft", None, None),
      )
    )
  }
}

object CloudProviderApi {
  final val TokenParam        = "token"
  final val RegistrationParam = "registration"
}
