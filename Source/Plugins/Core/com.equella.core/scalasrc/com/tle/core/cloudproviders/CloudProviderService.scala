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

package com.tle.core.cloudproviders

import cats.effect.IO
import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.tle.beans.cloudproviders.{CloudControlDefinition, ProviderControlDefinition}
import com.tle.beans.item.attachments.{CustomAttachment, UnmodifiableAttachments}
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.httpclient._
import com.tle.core.oauthclient.OAuthClientService
import com.tle.legacy.LegacyGuice
import fs2.Stream
import org.apache.commons.lang.RandomStringUtils
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.util.Collections
import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._

sealed trait CloudProviderError
case class IOError(throwable: Throwable)                          extends CloudProviderError
case class HttpError(message: String)                             extends CloudProviderError
case class JSONError(error: DeserializationError[io.circe.Error]) extends CloudProviderError

object CloudProviderService {

  val Logger = LoggerFactory.getLogger(getClass)

  val ControlCacheValidSeconds   = 60
  val InvalidControlRetrySeconds = 20

  final val OAuthServiceId      = "oauth"
  final val ControlsServiceId   = "controls"
  final val CloudAttachmentType = "cloud"

  val controlListCache = LegacyGuice.replicatedCacheService
    .getCache[List[CloudControlDefinition]]("cloudControlLists", 1000, 1, TimeUnit.MINUTES)

  def tokenUrlForProvider(provider: CloudProviderInstance): IO[Uri] = {
    provider.serviceUrls
      .get(OAuthServiceId)
      .map { oauthService =>
        IO.fromEither(
          UriTemplateService.replaceVariables(oauthService.url, provider.baseUrl, Map()))
      }
      .getOrElse(IO.raiseError(new Throwable("No OAuth service URL")))
  }

  def serviceUri(provider: CloudProviderInstance,
                 serviceUri: ServiceUrl,
                 params: Map[String, Any]): Either[UriParseError, Uri] = {
    UriTemplateService.replaceVariables(serviceUri.url, provider.baseUrl, contextParams ++ params)
  }

  // Use Any probably because this map will later concat with other maps which have unknown type for values.
  def contextParams: Map[String, Any] = Map("userid" -> CurrentUser.getDetails.getUniqueID)

  def serviceRequest[T](serviceUri: ServiceUrl,
                        provider: CloudProviderInstance,
                        params: Map[String, Any],
                        f: Uri => Request[T, Stream[IO, ByteBuffer]]): IO[Response[T]] = {
    for {
      uri <- IO.fromEither(
        UriTemplateService
          .replaceVariables(serviceUri.url, provider.baseUrl, contextParams ++ params))

      req            = f(uri)
      requestContext = "[" + RandomStringUtils.randomAlphanumeric(6) + "] provider: " + provider.id + ", vendor: " + provider.vendorId
      _ = Logger.debug(
        requestContext + ", method: " + req.method.m + ", request: " + uri.toString.split('?')(0))
      auth = provider.providerAuth
      response <- if (serviceUri.authenticated) {
        tokenUrlForProvider(provider).map { oauthUrl =>
          OAuthClientService
            .authorizedRequest(oauthUrl.toString, auth.clientId, auth.clientSecret, req)
        }
      } else req.send()

    } yield {
      Logger.debug(requestContext + ", response status: " + response.code)
      response
    }
  }

  private def getControlDefinition(
      provider: CloudProviderInstance): Either[String, List[CloudControlDefinition]] = {
    provider.serviceUrls.get(ControlsServiceId) match {
      case None => Right(List.empty)
      case Some(controlsService) => {
        serviceRequest(
          controlsService,
          provider,
          Map(),
          u => sttp.get(u).response(asJson[Map[String, ProviderControlDefinition]])).attempt
          .map { responseOrError =>
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
              }.toList
            }
          }
          .map(_.leftMap(err =>
            s"Failed to retrieve the Cloud Control definitions of ${provider.name} due to $err"))
          .unsafeRunSync
      }
    }
  }

  def queryControls: List[CloudControlDefinition] = {
    val cacheKey = "cloudControlDefs"

    def controls: List[CloudControlDefinition] =
      CloudProviderHelper.getAll
        .map(getControlDefinition)
        .separate match {
        case (errors: List[String], cloudControlDefs: List[List[CloudControlDefinition]]) =>
          errors.foreach(Logger.error) // Log all the errors captured during the whole process.
          val defs = cloudControlDefs.flatten // Put all the control definitions in one list.
          controlListCache.put(cacheKey, defs) // Save the definition list to cache and return it.
          defs
        case _ =>
          Logger.error("Unknown type returned when retrieving CloudControlDefinition")
          List.empty
      }

    controlListCache.get(cacheKey).or(() => controls)
  }

  def collectBodyText(attachments: UnmodifiableAttachments): String = {
    attachments
      .getCustomList(CloudAttachmentType)
      .asScala
      .flatMap { attach =>
        CloudAttachmentJson.decodeJson(attach).indexText
      }
      .mkString(" ", " ", "")
  }

  def filesToIndex(attach: CustomAttachment): java.lang.Iterable[String] = {
    CloudAttachmentJson
      .decodeJson(attach)
      .indexFiles
      .map(_.asJava)
      .getOrElse(Collections.emptyList())
  }
}
