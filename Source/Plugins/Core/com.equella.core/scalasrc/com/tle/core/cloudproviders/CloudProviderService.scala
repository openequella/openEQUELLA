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

package com.tle.core.cloudproviders

import java.net.URI
import java.util.UUID

import cats.data.OptionT
import cats.effect.IO
import com.softwaremill.sttp._
import com.tle.core.httpclient._
import com.tle.core.db._
import com.tle.core.oauthclient.{OAuthClientService, OAuthTokenState}

object CloudProviderService {

  val OAuthServiceId = "oauth"

  def tokenForProvider(cp: CloudProviderInstance): DB[OAuthTokenState] = {
    cp.serviceUris
      .get(OAuthServiceId)
      .map { oauthService =>
        dbLiftIO
          .liftIO(
            IO.fromEither(UriTemplateService.replaceVariables(oauthService.uri, cp.baseUrl, Map())))
          .flatMap { uri =>
            OAuthClientService.tokenForClient(uri.toString,
                                              cp.providerAuth.clientId,
                                              cp.providerAuth.clientSecret)
          }
      }
      .getOrElse(dbLiftIO.liftIO(IO.raiseError(new Throwable("No OAuth service URL"))))
  }

  def proxyRequest(uuid: UUID, serviceId: String): DB[String] = {
    CloudProviderDB
      .get(uuid)
      .flatMap { cp =>
        val tempCP = cp.copy(
          baseUrl = "http://doolse-sabre:8080/my/",
          serviceUris = Map(
            "oauth"       -> ServiceUri("${baseurl}oauth/access_token", authenticated = false),
            "currentuser" -> ServiceUri("${baseurl}api/content/currentuser", authenticated = true)
          ),
          providerAuth = CloudOAuthCredentials("aadd359a-3478-484f-8aca-97b18901bcd9",
                                               "985477ca-52ee-400d-a162-7ad403149352")
        )
        OptionT.fromOption[DB](tempCP.serviceUris.get(serviceId)).semiflatMap {
          case ServiceUri(uriTemplate, auth) =>
            for {
              uri <- dbLiftIO.liftIO {
                IO.fromEither(
                  UriTemplateService.replaceVariables(uriTemplate, tempCP.baseUrl, Map()))
              }
              req = sttp.get(uri)
              response <- if (auth) {
                tokenForProvider(tempCP).flatMap { token =>
                  dbLiftIO.liftIO {
                    OAuthClientService.requestWithToken(req, token.token, token.tokenType)
                  }
                }
              } else dbLiftIO.liftIO { req.send() }

            } yield response.unsafeBody
        }
      }
      .getOrElse("")
  }

}
