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

import com.tle.common.ExpiringValue
import com.tle.common.oauth.beans.{OAuthClient, OAuthToken}
import com.tle.common.usermanagement.user.UserState
import com.tle.core.i18n.CoreStrings
import com.tle.core.oauth.OAuthConstants
import com.tle.legacy.LegacyGuice
import com.tle.web.oauth.OAuthException
import com.tle.web.oauth.service.OAuthWebService.AuthorisationDetails
import com.tle.web.oauth.service.{IOAuthClient, IOAuthToken}

import java.time.Instant
import javax.servlet.http.HttpServletRequest

object OAuthServerAccess {

  private final val KEY_TOKEN_NOT_FOUND = "oauth.error.tokennotfound"

  case class StdOAuthClient(client: OAuthClient) extends IOAuthClient {
    override def getUserId: String = client.getUserId

    override def getClientId: String = client.getClientId

    override def getRedirectUrl: String = client.getRedirectUrl

    override def getClientSecret: String = client.getClientSecret

    override def secretMatches(clientSecret: String): Boolean =
      LegacyGuice.encryptionService.decrypt(client.getClientSecret) == clientSecret
  }

  case class StdOAuthToken(token: OAuthToken) extends IOAuthToken {
    override def getToken: String = token.getToken

    override def getExpiry: Instant = Option(token.getExpiry).map(_.toInstant).orNull
  }

  def byClientId(clientId: String): IOAuthClient = {
    Option(LegacyGuice.oAuthService.getByClientIdOnly(clientId)).map { client =>
      StdOAuthClient(client): IOAuthClient
    }.orNull
  }

  def byClientIdAndRedirect(clientId: String, redirect: String): IOAuthClient = {
    Option(LegacyGuice.oAuthService.getByClientIdAndRedirectUrl(clientId, redirect)).map { client =>
      StdOAuthClient(client)
    }.orNull
  }

  def getOrCreateToken(
      authDetails: AuthorisationDetails,
      authClient: IOAuthClient,
      code: String
  ): IOAuthToken = authClient match {
    case StdOAuthClient(client) =>
      val username = Option(authDetails.getUsername).getOrElse {
        LegacyGuice.userService.getInformationForUser(authDetails.getUserId).getUsername
      }
      StdOAuthToken(
        LegacyGuice.oAuthService.getOrCreateToken(authDetails.getUserId, username, client, code)
      )
  }

  private def authWithUsername(username: String, request: HttpServletRequest): UserState = {
    LegacyGuice.userService
      .authenticateAsUser(username, LegacyGuice.userService.getWebAuthenticationDetails(request))
  }

  def findUserState(tokenData: String, request: HttpServletRequest): ExpiringValue[UserState] = {
    Option(LegacyGuice.oAuthService.getToken(tokenData))
      .map { token =>
        val us = authWithUsername(token.getUsername, request)
        Option(token.getExpiry)
          .map(_.getTime)
          .map(ExpiringValue.expireAt(us, _))
          .getOrElse(ExpiringValue.expireNever(us))
      }
      .getOrElse {
        throw tokenNotFound()
      }
  }

  def tokenNotFound(): OAuthException =
    new OAuthException(
      403,
      OAuthConstants.ERROR_ACCESS_DENIED,
      CoreStrings.text(KEY_TOKEN_NOT_FOUND)
    )
}
