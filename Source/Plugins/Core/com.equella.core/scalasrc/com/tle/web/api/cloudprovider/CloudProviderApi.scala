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

import com.tle.core.cloudproviders.{
  CloudProviderDB,
  CloudProviderRegistration,
  CloudProviderRegistrationResponse
}
import com.tle.core.db.RunWithDB
import com.tle.core.settings.SettingsDB
import com.tle.web.api.ApiHelper
import io.lemonlabs.uri.{Uri, Url}
import io.lemonlabs.uri.parsing.{UriParser, UrlParser}
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.{Context, Response, UriInfo}
import javax.ws.rs._

case class CloudProviderCallback(returnUrl: String)

@Api("Cloud Providers")
@Path("cloudprovider")
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
  @ApiOperation(value = "Prepare openEQUELLA for a cloud provider registration")
  def prepareRegistration(@QueryParam("url") @DefaultValue("") providerUrl: String,
                          @Context uriInfo: UriInfo): CloudProviderCallback = {
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
              CloudProviderCallback(u.addParam(RegistrationParam, returnUrl).toString)
            }
          }
        }
      case _ => throw new BadRequestException("Invalid provider registration url")
    }

  }
}

object CloudProviderApi {
  final val TokenParam        = "token"
  final val RegistrationParam = "registration"
}
