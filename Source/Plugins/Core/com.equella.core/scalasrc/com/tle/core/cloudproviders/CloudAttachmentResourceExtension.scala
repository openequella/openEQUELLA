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

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.util

import cats.data.OptionT
import cats.effect.IO
import com.softwaremill.sttp._
import com.tle.beans.item.attachments.{CustomAttachment, IAttachment}
import com.tle.core.db.{DB, RunWithDB}
import com.tle.web.sections.render.TextLabel
import com.tle.web.sections.standard.model.SimpleBookmark
import com.tle.web.sections.{Bookmark, SectionInfo}
import com.tle.web.stream.{AbstractContentStream, ContentStream}
import com.tle.web.viewurl.{AttachmentDetail, ViewableResource}
import com.tle.web.viewurl.attachments.AttachmentResourceExtension
import com.tle.web.viewurl.resource.AbstractWrappedResource
import fs2.Stream
import io.circe.Json.Folder
import io.circe.{Json, JsonNumber, JsonObject}

import scala.collection.JavaConverters._

class CloudAttachmentResourceExtension extends AttachmentResourceExtension[IAttachment] {
  override def process(info: SectionInfo,
                       resource: ViewableResource,
                       attachment: IAttachment): ViewableResource = attachment match {
    case attach: CustomAttachment => CloudAttachmentViewableResource(info, resource, attach)
  }
}

case class CloudAttachmentViewableResource(info: SectionInfo,
                                           parent: ViewableResource,
                                           attach: CustomAttachment)
    extends AbstractWrappedResource(parent) {

  val fields      = CloudAttachmentFields(attach)
  lazy val itemId = parent.getViewableItem.getItemId
  lazy val directLink = RunWithDB.executeWithHibernate {
    (for {
      cp <- CloudProviderDB.get(fields.providerId)
      viewerDeets <- OptionT.fromOption[DB](
        serviceUriForViewer(cp, "").filterNot(_._2.authenticated))
      viewerUri <- OptionT {
        CloudProviderService.serviceUri(cp, viewerDeets._2, uriParameters).map(_.toOption)
      }
    } yield viewerUri).value
  }

  def serviceUriForViewer(provider: CloudProviderInstance,
                          viewerId: String): Option[(Viewer, ServiceUri)] =
    for {
      viewerMap  <- provider.viewers.get(fields.cloudType)
      viewer     <- viewerMap.get(viewerId)
      serviceUri <- provider.serviceUris.get(viewer.serviceId)
    } yield (viewer, serviceUri)

  def uriParameters: Map[String, Any] = {
    val metaParams =
      fields.cloudJson.meta.map(_.mapValues(_.foldWith(MetaJsonFolder))).getOrElse(Map.empty)
    Map("item" -> itemId.getUuid, "version" -> itemId.getVersion, "attachment" -> attach.getUuid) ++ metaParams
  }

  override def getCommonAttachmentDetails: util.List[AttachmentDetail] = {
    fields.cloudJson.display
      .getOrElse(Map.empty)
      .map {
        case (name, value) =>
          AbstractWrappedResource.makeDetail(
            new TextLabel(name + ":"),
            new TextLabel(value.foldWith(CloudAttachmentSerializer.javaFolder).toString))
      }
      .toBuffer
      .asJava
  }

  override def isExternalResource: Boolean = true

  override def createCanonicalUrl(): Bookmark =
    directLink.map(uri => new SimpleBookmark(uri.toString())).getOrElse {
      super.createCanonicalUrl()
    }

  override def hasContentStream: Boolean = directLink.isEmpty

  override def getContentStream: ContentStream = {
    RunWithDB.executeWithHibernate {
      (for {
        provider      <- CloudProviderDB.get(fields.providerId)
        viewerDetails <- OptionT.fromOption[DB] { serviceUriForViewer(provider, "") }
        response <- OptionT.liftF(
          CloudProviderService.serviceRequest(
            viewerDetails._2,
            provider,
            uriParameters,
            uri => sttp.get(uri).response(asStream[Stream[IO, ByteBuffer]])))
      } yield response).value
    } match {
      case None => EmptyResponseStream
      case Some(response) =>
        response.body match {
          case Right(responseStream) => SttpResponseContentStream(response, responseStream)
          case Left(failure)         => sys.error(failure)
        }
    }
  }

}

object MetaJsonFolder extends Folder[Any] {
  override def onNull: Any = None

  override def onBoolean(value: Boolean): Any = value

  override def onNumber(value: JsonNumber): Any =
    value.toLong
      .getOrElse(value.toDouble)

  override def onString(value: String): Any = value

  override def onArray(value: Vector[Json]): Any = value.map(_.foldWith(this))

  override def onObject(value: JsonObject): Any =
    value.toMap.mapValues(_.foldWith(this))
}

case class SttpResponseContentStream(response: Response[fs2.Stream[IO, ByteBuffer]],
                                     responseStream: fs2.Stream[IO, ByteBuffer])
    extends AbstractContentStream(null, response.contentType.orNull) {
  override def getContentLength: Long      = response.contentLength.getOrElse(-1L)
  override def getInputStream: InputStream = null
  override def mustWrite(): Boolean        = true
  override def write(out: OutputStream): Unit = {
    val channel = Channels.newChannel(out)
    responseStream.evalMap(bb => IO(channel.write(bb))).compile.drain.unsafeRunSync()
  }
}

object EmptyResponseStream extends AbstractContentStream(null, null) {
  override def getInputStream: InputStream = null
  override def getContentLength: Long      = 0L
  override def exists(): Boolean           = false
}
