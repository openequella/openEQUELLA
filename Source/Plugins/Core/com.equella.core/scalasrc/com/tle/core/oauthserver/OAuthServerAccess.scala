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

package com.tle.core.oauthserver

import java.time.{Duration, Instant}
import java.util.{Date, UUID}

import com.tle.common.oauth.beans.{OAuthClient, OAuthToken}
import com.tle.common.usermanagement.user.{SystemUserState, UserState}
import com.tle.core.cloudproviders.{CloudOAuthCredentials, CloudProviderDB, CloudProviderUserState}
import com.tle.core.db._
import com.tle.core.db.tables.CachedValue
import com.tle.core.i18n.CoreStrings
import com.tle.core.oauth.OAuthConstants
import com.tle.legacy.LegacyGuice
import com.tle.web.oauth.OAuthException
import com.tle.web.oauth.service.OAuthWebService.AuthorisationDetails
import com.tle.web.oauth.service.{IOAuthClient, IOAuthToken}
import javax.servlet.http.HttpServletRequest
import fs2.Stream

object OAuthServerAccess {

  private final val CloudTokenCache = "cloudProviderToken"

  private final val KEY_TOKEN_NOT_FOUND = "oauth.error.tokennotfound"

  private final val TokenTimeout = Duration.ofDays(365)

  private val tokenQueries: CachedValueQueries = DBSchema.queries.cachedValueQueries

  case class StdOAuthClient(client: OAuthClient) extends IOAuthClient {
    override def getUserId: String = client.getUserId

    override def getClientId: String = client.getClientId

    override def getRedirectUrl: String = client.getRedirectUrl

    override def getClientSecret: String = client.getClientSecret

    override def secretMatches(clientSecret: String): Boolean =
      LegacyGuice.encryptionService.decrypt(client.getClientSecret) == clientSecret
  }

  case class CloudProviderClient(client: UUID, auth: CloudOAuthCredentials) extends IOAuthClient {
    override def getUserId: String = "_"

    override def getClientId: String = auth.clientId

    override def getRedirectUrl: String = "redirect"

    override def getClientSecret: String = auth.clientSecret

    override def secretMatches(clientSecret: String): Boolean = auth.clientSecret == clientSecret
  }

  case class StdOAuthToken(token: OAuthToken) extends IOAuthToken {
    override def getToken: String = token.getToken

    override def getExpiry: Instant = Option(token.getExpiry).map(_.toInstant).orNull
  }

  case class CloudAuthToken(token: String, expiry: Instant) extends IOAuthToken {
    override def getToken: String = token

    override def getExpiry: Instant = expiry
  }

  def byClientId(clientId: String): IOAuthClient = {

    Option(LegacyGuice.oAuthService.getByClientIdOnly(clientId))
      .map { client =>
        StdOAuthClient(client): IOAuthClient
      }
      .orElse {
        RunWithDB
          .execute {
            CloudProviderDB.readAll
              .collectFirst {
                case client if client.oeqAuth.clientId == clientId =>
                  CloudProviderClient(client.id, client.oeqAuth)
              }
              .compile
              .last
          }
      }
      .orNull
  }

  def byClientIdAndRedirect(clientId: String, redirect: String): IOAuthClient = {
    Option(LegacyGuice.oAuthService.getByClientIdAndRedirectUrl(clientId, redirect)).map { client =>
      StdOAuthClient(client)
    }.orNull
  }

  def getOrCreateToken(authDetails: AuthorisationDetails,
                       iclient: IOAuthClient,
                       code: String): IOAuthToken = iclient match {
    case StdOAuthClient(client) =>
      var username = Option(authDetails.getUsername).getOrElse {
        LegacyGuice.userService.getInformationForUser(authDetails.getUserId).getUsername
      }
      StdOAuthToken(
        LegacyGuice.oAuthService.getOrCreateToken(authDetails.getUserId, username, client, code))

    case CloudProviderClient(clientId, creds) =>
      RunWithDB.execute {
        dbStream { context =>
          for {
            existing <- tokenQueries
              .getForValue((CloudTokenCache, clientId.toString, context.inst))
              .map { tokenEntry =>
                CloudAuthToken(tokenEntry.key.value, tokenEntry.ttl.getOrElse(Instant.now))
              }
              .last
            token <- existing match {
              case Some(already) => Stream(already)
              case None =>
                val tokenUuid = UUID.randomUUID().toString
                val expires   = Instant.now().plus(TokenTimeout)
                tokenQueries
                  .insertNew { valueId =>
                    CachedValue(valueId,
                                CloudTokenCache,
                                tokenUuid,
                                Some(expires),
                                clientId.toString,
                                context.inst)
                  }
                  .map(_ => CloudAuthToken(tokenUuid, expires))
            }
          } yield token
        }.compile.lastOrError
      }
  }

  private def authWithUsername(username: String, request: HttpServletRequest): UserState = {
    LegacyGuice.userService
      .authenticateAsUser(username, LegacyGuice.userService.getWebAuthenticationDetails(request))
  }

  def findUserState(tokenData: String, request: HttpServletRequest): UserState = {
    Option(LegacyGuice.oAuthService.getToken(tokenData))
      .map { token =>
        authWithUsername(token.getUsername, request)
      }
      .orElse {

        val tokenAvailable = RunWithDB.execute {
          dbStream { context =>
            tokenQueries
              .getForKey((CloudTokenCache, tokenData, context.inst))
              .map { tokenValue =>
                new CloudProviderUserState(tokenValue.value, context.inst)
              }
          }.compile.last
        }

        tokenAvailable
          .flatMap { _ =>
            for {
              userid   <- Option(request.getHeader("X-ImpersonateUser"))
              userBean <- Option(LegacyGuice.userService.getInformationForUser(userid))
            } yield authWithUsername(userBean.getUsername, request)
          }
          .orElse(tokenAvailable)
      }
      .getOrElse {
        throw new OAuthException(403,
                                 OAuthConstants.ERROR_ACCESS_DENIED,
                                 CoreStrings.text(KEY_TOKEN_NOT_FOUND))
      }
  }
}
