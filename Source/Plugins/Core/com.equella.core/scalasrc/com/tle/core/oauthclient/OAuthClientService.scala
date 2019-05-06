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
import cats.syntax.apply._
import com.google.common.cache.CacheBuilder
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.tle.core.cache.{Cacheable, DBCacheBuilder}
import com.tle.core.db._
import com.tle.core.db.tables.CachedValue
import com.tle.web.oauth.OAuthWebConstants
import fs2.Stream
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.doolse.simpledba.circe._
import com.tle.core.httpclient._

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

object OAuthClientService {

  val stateJson = circeIsoUnsafe[OAuthTokenState]

  def responseToState(response: OAuthTokenResponse): OAuthTokenState = {
    val expires = response.expires_in.filterNot(_ == Long.MaxValue).map(Instant.now().plusSeconds)
    OAuthTokenState(response.access_token,
                    OAuthTokenType.fromString(response.token_type),
                    expires,
                    response.refresh_token)
  }

  object OAuthTokenCache extends Cacheable[TokenRequest, OAuthTokenState] {

    private val cacheQueries: CachedValueQueries = DBSchema.queries.cachedValueQueries

    def getDBCacheForRequest(token: TokenRequest): Stream[DB, CachedValue] = dbStream { ctx =>
      cacheQueries.getForKey(cacheId, token.key, ctx.inst)
    }

    def removeFromDB(token: TokenRequest): DB[Unit] =
      toJDBCStream(getDBCacheForRequest(token)).flatMap { s =>
        flushDB(cacheQueries.writes.deleteAll(s))
      }

    def requestTokenAndSave(token: TokenRequest): DB[OAuthTokenState] = {
      val postRequest = sttp
        .body(
          OAuthWebConstants.PARAM_GRANT_TYPE    -> OAuthWebConstants.GRANT_TYPE_CREDENTIALS,
          OAuthWebConstants.PARAM_CLIENT_ID     -> token.clientId,
          OAuthWebConstants.PARAM_CLIENT_SECRET -> token.clientSecret
        )
        .response(asJson[OAuthTokenResponse])
        .post(uri"${token.authTokenUrl}")

      def newCacheValue(state: OAuthTokenState): DB[Unit] =
        dbStream { uc =>
          cacheQueries.insertNew { id =>
            CachedValue(id,
                        cache_id = cacheId,
                        key = token.key,
                        ttl = state.expires,
                        value = stateJson.to(state),
                        institution_id = uc.inst)
          }
        }.compile.drain

      for {
        tokenState <- dbLiftIO
          .liftIO(postRequest.send())
          .map(_.unsafeBody.fold(de => throw de.error, responseToState))
        _ <- newCacheValue(tokenState)
      } yield tokenState
    }

    override def cacheId: String = "oauthCache"

    override def key(userContext: UserContext, v: TokenRequest): String = {
      s"${userContext.inst.getUniqueId}_${v.authTokenUrl}_${v.clientId}"
    }

    override def query: TokenRequest => DB[OAuthTokenState] = (token: TokenRequest) => {
      for {
        dbCachedValue <- getDBCacheForRequest(token).compile.last
        state <- dbCachedValue match {
          case Some(cv) => stateJson.from(cv.value).pure[DB]
          case None     => requestTokenAndSave(token)
        }
      } yield state
    }
  }

  private val clientTokenCache = DBCacheBuilder.buildCache(
    OAuthTokenCache,
    CacheBuilder
      .newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .build[String, AnyRef]()
      .asMap())

  private def tokenStillValid(token: OAuthTokenState): Boolean =
    token.expires.isEmpty || token.expires.exists(_.isAfter(Instant.now))

  def removeToken(tokenRequest: TokenRequest): DB[Unit] = {
    OAuthTokenCache.removeFromDB(tokenRequest) *>
      clientTokenCache.invalidate(tokenRequest).flatMap(dbLiftIO.liftIO)
  }

  def tokenForClient(tokenRequest: TokenRequest): DB[OAuthTokenState] = {
    for {
      cachedToken <- clientTokenCache.get(tokenRequest)
      refreshedToken <- if (tokenStillValid(cachedToken)) cachedToken.pure[DB]
      else {
        removeToken(tokenRequest) *>
          OAuthTokenCache.requestTokenAndSave(tokenRequest)
      }
    } yield refreshedToken
  }

  def requestWithToken[T](request: Request[T, Stream[IO, ByteBuffer]],
                          token: String,
                          tokenType: OAuthTokenType.Value): IO[Response[T]] = {
    val authHeader = tokenType match {
      case OAuthTokenType.EquellaApi =>
        OAuthWebConstants.HEADER_X_AUTHORIZATION -> s"${OAuthWebConstants.AUTHORIZATION_ACCESS_TOKEN}=$token"
      case OAuthTokenType.Bearer =>
        OAuthWebConstants.HEADER_AUTHORIZATION -> s"${OAuthWebConstants.AUTHORIZATION_BEARER} $token"
    }
    request.headers(authHeader).send[IO]()
  }

  def authorizedRequest[T](authTokenUrl: String,
                           clientId: String,
                           clientSecret: String,
                           request: Request[T, Stream[IO, ByteBuffer]]): DB[Response[T]] = {
    val tokenRequest = TokenRequest(authTokenUrl, clientId, clientSecret)
    for {
      token    <- tokenForClient(tokenRequest)
      response <- dbLiftIO.liftIO(requestWithToken(request, token.token, token.tokenType))
      _        <- if (response.code == StatusCodes.Unauthorized) removeToken(tokenRequest) else ().pure[DB]
    } yield response
  }
}
