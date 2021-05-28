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

package com.tle.core.oauthclient

import java.nio.ByteBuffer
import java.time.Instant
import java.util.concurrent._
import cats.effect.IO
import cats.syntax.applicative._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.tle.common.institution.CurrentInstitution
import com.tle.core.db._
import com.tle.web.oauth.OAuthWebConstants
import fs2.Stream
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import com.tle.core.httpclient._
import com.tle.core.oauthclient.OAuthClientService.{replicatedCache, responseToState}
import com.tle.core.oauthclient.OAuthTokenCacheHelper.{
  buildCacheKey,
  cacheId,
  getOrBuildCache,
  removeFromDB
}
import com.tle.core.replicatedcache.dao.CachedValue
import com.tle.legacy.LegacyGuice
import java.nio.charset.StandardCharsets
import java.util.Date
import io.circe.syntax._
import io.circe.parser.parse

object OAuthTokenType extends Enumeration {
  val Bearer, EquellaApi = Value

  def fromString(s: Option[String]): Value =
    s.map {
        _.toLowerCase match {
          case "bearer"      => Bearer
          case "equella_api" => EquellaApi
        }
      }
      .getOrElse(Bearer)

}

case class TokenRequest(authTokenUrl: String, clientId: String, clientSecret: String) {
  def key: String = clientId + authTokenUrl
}

case class OAuthTokenState(token: String,
                           tokenType: OAuthTokenType.Value,
                           expires: Option[Instant],
                           refreshToken: Option[String])

case class OAuthTokenResponse(access_token: String,
                              refresh_token: Option[String],
                              token_type: Option[String],
                              expires_in: Option[Long],
                              state: Option[String])

object OAuthTokenResponse {
  implicit val dec = deriveDecoder[OAuthTokenResponse]
}

object OAuthTokenState {
  implicit val encodeEnum    = Encoder.enumEncoder(OAuthTokenType)
  implicit val decodeEnum    = Decoder.enumDecoder(OAuthTokenType)
  implicit val encodeInstant = Encoder.encodeInstant
  implicit val decodeInstant = Decoder.decodeInstant
  implicit val enc           = deriveEncoder[OAuthTokenState]
  implicit val dec           = deriveDecoder[OAuthTokenState]

}

object OAuthTokenCacheHelper {
  val cacheId: String = "oauthCache"

  def buildCacheKey(tokenRequest: TokenRequest): String = {
    s"${CurrentInstitution.get().getUniqueId}_${tokenRequest.authTokenUrl}_${tokenRequest.clientId}"
  }

  def getCacheFromDB(token: TokenRequest): Option[CachedValue] = {
    Option(LegacyGuice.replicatedCacheDao.get(cacheId, token.key))
  }

  def removeFromDB(token: TokenRequest): Unit = {
    getCacheFromDB(token).foreach(cv => LegacyGuice.replicatedCacheDao.delete(cv))
  }

  def requestTokenAndSave(token: TokenRequest): OAuthTokenState = {
    val postRequest = sttp.auth
      .basic(token.clientId, token.clientSecret)
      .body(
        OAuthWebConstants.PARAM_GRANT_TYPE -> OAuthWebConstants.GRANT_TYPE_CREDENTIALS
      )
      .response(asJson[OAuthTokenResponse])
      .post(uri"${token.authTokenUrl}")
    lazy val oauthTokenState = postRequest
      .send()
      .map(_.unsafeBody.fold(de => throw de.error, responseToState))
      .unsafeRunSync()

    LegacyGuice.replicatedCacheDao.put(
      cacheId,
      token.key,
      Date.from(oauthTokenState.expires.getOrElse(Instant.now())),
      oauthTokenState.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
    )

    oauthTokenState
  }

  def getOrBuildCache(tokenRequest: TokenRequest, tokenKey: String): OAuthTokenState = {
    getCacheFromDB(tokenRequest) match {
      case Some(cv) =>
        parse(new String(cv.getValue)).flatMap(_.as[OAuthTokenState]) match {
          case Left(failure) =>
            throw new RuntimeException(
              s"Failed to get OAUTH token for client: ${tokenRequest.clientId} due to ParsingFailure: ${failure.getMessage}")
          case Right(tokenState) => tokenState
        }
      case None =>
        val newOAuthTokenState = requestTokenAndSave(tokenRequest)
        replicatedCache.put(tokenKey, newOAuthTokenState)
        newOAuthTokenState
    }
  }
}

object OAuthClientService {
  val replicatedCache = LegacyGuice.replicatedCacheService
    .getCache[OAuthTokenState](cacheId, 1000, 10, TimeUnit.MINUTES)

  def responseToState(response: OAuthTokenResponse): OAuthTokenState = {
    val expires = response.expires_in.filterNot(_ == Long.MaxValue).map(Instant.now().plusSeconds)
    OAuthTokenState(response.access_token,
                    OAuthTokenType.fromString(response.token_type),
                    expires,
                    response.refresh_token)
  }

  private def tokenStillValid(token: OAuthTokenState): Boolean =
    token.expires.isEmpty || token.expires.exists(_.isAfter(Instant.now))

  def removeToken(tokenRequest: TokenRequest): Unit = {
    removeFromDB(tokenRequest)
    replicatedCache.invalidate()
  }

  def tokenForClient(tokenRequest: TokenRequest): OAuthTokenState = {
    val tokenKey = buildCacheKey(tokenRequest)
    // If the token is not saved in Cache, find it from DB.
    // If it's not saved in DB as well, create a new one and save in DB and Cache.
    val cachedToken =
      replicatedCache.get(tokenKey).or(() => getOrBuildCache(tokenRequest, tokenKey))

    // If the token has expired, remove it from Cache and DB, and then create a new one.
    val refreshedToken = if (tokenStillValid(cachedToken)) {
      cachedToken
    } else {
      removeToken(tokenRequest)
      getOrBuildCache(tokenRequest, tokenKey)
    }
    refreshedToken
  }

  def requestWithToken[T](request: Request[T, Stream[IO, ByteBuffer]],
                          token: String,
                          tokenType: OAuthTokenType.Value): Response[T] = {
    val authHeader = tokenType match {
      case OAuthTokenType.EquellaApi =>
        OAuthWebConstants.HEADER_X_AUTHORIZATION -> s"${OAuthWebConstants.AUTHORIZATION_ACCESS_TOKEN}=$token"
      case OAuthTokenType.Bearer =>
        OAuthWebConstants.HEADER_AUTHORIZATION -> s"${OAuthWebConstants.AUTHORIZATION_BEARER} $token"
    }
    request.headers(authHeader).send[IO].unsafeRunSync()
  }

  def authorizedRequest[T](authTokenUrl: String,
                           clientId: String,
                           clientSecret: String,
                           request: Request[T, Stream[IO, ByteBuffer]]): DB[Response[T]] = {
    val tokenRequest = TokenRequest(authTokenUrl, clientId, clientSecret)
    val token        = tokenForClient(tokenRequest)
    val res          = requestWithToken(request, token.token, token.tokenType)
    if (res.code == StatusCodes.Unauthorized) removeToken(tokenRequest)
    res.pure[DB]
  }
}
