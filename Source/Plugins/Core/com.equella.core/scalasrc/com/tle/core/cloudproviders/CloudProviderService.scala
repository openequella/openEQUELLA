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

import java.nio.ByteBuffer
import java.time.Instant

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.applicative._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.tle.beans.cloudproviders.{CloudControlDefinition, ProviderControlDefinition}
import com.tle.core.cache.{Cacheable, DBCacheBuilder}
import com.tle.core.db._
import com.tle.core.httpclient._
import com.tle.core.oauthclient.OAuthClientService
import fs2.Stream
import org.slf4j.LoggerFactory

sealed trait CloudProviderError
case class IOError(throwable: Throwable)                          extends CloudProviderError
case class HttpError(message: String)                             extends CloudProviderError
case class JSONError(error: DeserializationError[io.circe.Error]) extends CloudProviderError

object CloudProviderService {

  val Logger = LoggerFactory.getLogger(getClass)

  val ControlCacheValidSeconds   = 60
  val InvalidControlRetrySeconds = 20

  val OAuthServiceId    = "oauth"
  val ControlsServiceId = "controls"

  def tokenUrlForProvider(provider: CloudProviderInstance): IO[Uri] = {
    provider.serviceUris
      .get(OAuthServiceId)
      .map { oauthService =>
        IO.fromEither(
          UriTemplateService.replaceVariables(oauthService.uri, provider.baseUrl, Map()))
      }
      .getOrElse(IO.raiseError(new Throwable("No OAuth service URL")))
  }

  def serviceRequest[T](serviceUri: ServiceUri,
                        provider: CloudProviderInstance,
                        params: Map[String, String],
                        f: Uri => Request[T, Stream[IO, ByteBuffer]]): DB[Response[T]] =
    for {
      uri <- dbLiftIO.liftIO {
        IO.fromEither(UriTemplateService.replaceVariables(serviceUri.uri, provider.baseUrl, params))
      }
      req  = f(uri)
      auth = provider.providerAuth
      response <- if (serviceUri.authenticated) {
        dbLiftIO.liftIO(tokenUrlForProvider(provider)).flatMap { oauthUrl =>
          OAuthClientService
            .authorizedRequest(oauthUrl.toString, auth.clientId, auth.clientSecret, req)
        }
      } else dbLiftIO.liftIO(req.send())
    } yield response

  case class ControlListCacheValue(
      expiry: Instant,
      result: Either[CloudProviderError, Iterable[CloudControlDefinition]])

  object ControlListCache extends Cacheable[CloudProviderInstance, ControlListCacheValue] {
    override def cacheId: String = "cloudControlLists"

    override def key(userContext: UserContext, v: CloudProviderInstance): String =
      s"${userContext.inst.getUniqueId}_${v.id}"

    def withTimeout(result: Either[CloudProviderError, Iterable[CloudControlDefinition]])
      : ControlListCacheValue = {
      val timeoutSeconds =
        if (result.isLeft) InvalidControlRetrySeconds else ControlCacheValidSeconds
      ControlListCacheValue(Instant.now().plusSeconds(timeoutSeconds), result)
    }
    override def query: CloudProviderInstance => DB[ControlListCacheValue] = provider => {
      provider.serviceUris.get(ControlsServiceId) match {
        case None => withTimeout(Right(Iterable.empty)).pure[DB]
        case Some(controlsService) =>
          dbAttempt {
            serviceRequest(
              controlsService,
              provider,
              Map(),
              u => sttp.get(u).response(asJson[Map[String, ProviderControlDefinition]]))
          }.map { responseOrError =>
            withTimeout {
              for {
                response   <- responseOrError.leftMap(IOError)
                controlMap <- response.body.leftMap(HttpError).flatMap(_.leftMap(JSONError))
              } yield {
                controlMap.map {
                  case (controlId, config) =>
                    CloudControlDefinition(provider.id,
                                           controlId,
                                           config.name,
                                           config.iconUrl.getOrElse("/icons/control.gif"),
                                           config.configuration)
                }
              }
            }
          }
      }
    }
  }

  val controlListCache = DBCacheBuilder.buildCache(ControlListCache)

  def queryControls(): DB[Vector[CloudControlDefinition]] =
    CloudProviderDB.readAll
      .evalMap { cp =>
        controlListCache
          .getIfValid(cp, _.expiry.isAfter(Instant.now()))
          .map(cv => cp.name -> cv.result)
      }
      .flatMap {
        case (name, Left(error)) =>
          Logger.info(s"Failed querying $name - $error")
          Stream.empty
        case (_, Right(controls)) => Stream.emits(controls.toSeq)
      }
      .compile
      .toVector

}
