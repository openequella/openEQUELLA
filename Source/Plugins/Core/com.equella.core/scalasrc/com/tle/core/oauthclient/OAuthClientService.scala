package com.tle.core.oauthclient

import java.nio.ByteBuffer
import java.time.Instant
import java.util.concurrent._

import cats.effect.IO
import cats.syntax.applicative._
import cats.syntax.apply._
import com.google.common.cache.CacheBuilder
import com.softwaremill.sttp.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import com.tle.core.cache.{Cacheable, DBCacheBuilder}
import com.tle.core.db._
import com.tle.core.db.tables.CachedValue
import com.tle.web.oauth.OAuthWebConstants
import fs2.Stream
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.doolse.simpledba.circe._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._

import scala.concurrent.ExecutionContext

object OAuthTokenType extends Enumeration {
  val Bearer, EquellaApi = Value

  def fromString(s: Option[String]): Value =
    s.map {
        case "bearer"      => Bearer
        case "equella_api" => EquellaApi
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

  val blockingEC            = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
  implicit val contextShift = IO.contextShift(blockingEC)

  implicit val sttpBackend = AsyncHttpClientFs2Backend[IO]()

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

  def tokenForClient(authTokenUrl: String,
                     clientId: String,
                     clientSecret: String): DB[OAuthTokenState] = {
    val tokenRequest = TokenRequest(authTokenUrl, clientId, clientSecret)
    for {
      cachedToken <- clientTokenCache.get(tokenRequest)
      refreshedToken <- if (tokenStillValid(cachedToken)) cachedToken.pure[DB]
      else {
        OAuthTokenCache.removeFromDB(tokenRequest) *>
          clientTokenCache.invalidate(tokenRequest).flatMap(dbLiftIO.liftIO) *>
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
    }
    request.headers(authHeader).send[IO]()
  }
}
