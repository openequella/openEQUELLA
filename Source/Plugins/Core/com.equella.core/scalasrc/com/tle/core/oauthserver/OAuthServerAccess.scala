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
import com.tle.common.institution.CurrentInstitution

import java.time.{Duration, Instant}
import java.util.{Date, UUID}
import com.tle.common.oauth.beans.{OAuthClient, OAuthToken}
import com.tle.common.usermanagement.user.{ModifiableUserState, UserState}
import com.tle.core.cloudproviders.{
  CloudOAuthCredentials,
  CloudProviderHelper,
  CloudProviderUserState
}
import com.tle.core.i18n.CoreStrings
import com.tle.core.oauth.OAuthConstants
import com.tle.legacy.LegacyGuice
import com.tle.web.oauth.OAuthException
import com.tle.web.oauth.service.OAuthWebService.AuthorisationDetails
import com.tle.web.oauth.service.{IOAuthClient, IOAuthToken}

import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.NotFoundException

object OAuthServerAccess {

  private final val CloudTokenCache = "cloudProviderToken"

  private final val KEY_TOKEN_NOT_FOUND = "oauth.error.tokennotfound"
  private final val KEY_USER_NOT_FOUND  = "oauth.error.usernotfound"

  private final val TokenTimeout = Duration.ofDays(365)

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
        CloudProviderHelper.getAll.collectFirst {
          case client if client.oeqAuth.clientId == clientId =>
            CloudProviderClient(client.id, client.oeqAuth)
        }
      }
      .orNull
  }

  def byClientIdAndRedirect(clientId: String, redirect: String): IOAuthClient = {
    Option(LegacyGuice.oAuthService.getByClientIdAndRedirectUrl(clientId, redirect)).map { client =>
      StdOAuthClient(client)
    }.orNull
  }

  def getOrCreateToken(
      authDetails: AuthorisationDetails,
      iclient: IOAuthClient,
      code: String
  ): IOAuthToken = iclient match {
    case StdOAuthClient(client) =>
      val username = Option(authDetails.getUsername).getOrElse {
        LegacyGuice.userService.getInformationForUser(authDetails.getUserId).getUsername
      }
      StdOAuthToken(
        LegacyGuice.oAuthService.getOrCreateToken(authDetails.getUserId, username, client, code)
      )

    case CloudProviderClient(clientId, _) =>
      val clientIdBytes = clientId.toString.getBytes(StandardCharsets.UTF_8)
      Option(LegacyGuice.replicatedCacheDao.getByValue(CloudTokenCache, clientIdBytes)) match {
        case Some(tokenEntry) =>
          CloudAuthToken(
            tokenEntry.getKey,
            Option(tokenEntry.getTtl).map(_.toInstant).getOrElse(Instant.now)
          )
        case None =>
          val tokenUuid = UUID.randomUUID().toString
          val expires   = Instant.now().plus(TokenTimeout)
          LegacyGuice.replicatedCacheDao.put(
            CloudTokenCache,
            tokenUuid,
            Date.from(expires),
            clientIdBytes
          )
          CloudAuthToken(tokenUuid, expires)
      }
  }

  private def authWithUsername(username: String, request: HttpServletRequest): UserState = {
    LegacyGuice.userService
      .authenticateAsUser(username, LegacyGuice.userService.getWebAuthenticationDetails(request))
  }

  def lookupCloudToken(
      tokenData: String,
      request: HttpServletRequest
  ): Option[ModifiableUserState] = {
    val (actualToken, impersonateId) = tokenData.indexOf(';') match {
      case -1 => (tokenData, None)
      case i  => (tokenData.substring(0, i), Some(tokenData.substring(i + 1)))
    }

    val cpUser = Option(LegacyGuice.replicatedCacheDao.get(CloudTokenCache, actualToken))
      .map(cachedValue =>
        new CloudProviderUserState(
          UUID.fromString(new String(cachedValue.getValue)),
          CurrentInstitution.get()
        )
      )

    def getCloudProviderName: String =
      cpUser.flatMap(user => CloudProviderHelper.getByUuid(user.providerId)) match {
        case Some(cp) => cp.name
        case None =>
          throw new NotFoundException(s"Failed to find Cloud provider for token ${tokenData}")
      }

    impersonateId match {
      case Some(userid) =>
        val impUser = Option(LegacyGuice.userService.getInformationForUser(userid)).getOrElse {
          throw tokenNotFound()
        }
        val user =
          authWithUsername(impUser.getUsername, request).asInstanceOf[ModifiableUserState]
        user.setImpersonatedBy(getCloudProviderName)
        Option(user)
      case None => cpUser
    }
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
      .orElse {
        // Failed to find a plain OAuth token, try a cloud provider token
        lookupCloudToken(tokenData, request)
          .map(ExpiringValue.expireNever[UserState])
      }
      .getOrElse {
        // Ultimately if no token was found, then treat it as an authentication failure
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
