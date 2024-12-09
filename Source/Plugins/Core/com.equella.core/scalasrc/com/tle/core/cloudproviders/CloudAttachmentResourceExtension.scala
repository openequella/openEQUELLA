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
import java.util.Collections
import cats.data.OptionT
import cats.effect.IO
import sttp.client._
import com.tle.beans.item.Item
import com.tle.beans.item.attachments.{CustomAttachment, IAttachment}
import com.tle.common.NameValue
import com.tle.legacy.LegacyGuice
import com.tle.web.sections.equella.viewers.AbstractResourceViewer
import com.tle.web.sections.render.TextLabel
import com.tle.web.sections.standard.model.SimpleBookmark
import com.tle.web.sections.{Bookmark, SectionId, SectionInfo}
import com.tle.web.stream.{AbstractContentStream, ContentStream}
import com.tle.web.viewable.ViewableItem
import com.tle.web.viewitem.section.RootItemFileSection
import com.tle.web.viewurl.attachments.AttachmentResourceExtension
import com.tle.web.viewurl.resource.AbstractWrappedResource
import com.tle.web.viewurl.{
  AttachmentDetail,
  ResourceViewer,
  ResourceViewerConfig,
  ViewAttachmentUrl,
  ViewableResource
}
import fs2.Stream
import io.circe.Json.Folder
import io.circe.{Json, JsonNumber, JsonObject}
import scala.jdk.CollectionConverters._

class CloudAttachmentResourceExtension extends AttachmentResourceExtension[IAttachment] {
  override def process(
      info: SectionInfo,
      resource: ViewableResource,
      attachment: IAttachment
  ): ViewableResource = attachment match {
    case attach: CustomAttachment => CloudAttachmentViewableResource(info, resource, attach)
  }
}

object CloudAttachmentViewableResource {
  final val EquellaViewerId = "file"
}

case class CloudAttachmentViewableResource(
    info: SectionInfo,
    parent: ViewableResource,
    attach: CustomAttachment
) extends AbstractWrappedResource(parent) {

  import CloudAttachmentViewableResource._
  val fields = CloudAttachmentFields(attach)
  val itemId = parent.getViewableItem.getItemId
  val viewerId = Option(info.lookupSection[RootItemFileSection, RootItemFileSection](classOf))
    .flatMap { rif =>
      Option(rif.getModel(info).getViewer)
    }
    .filterNot(_ == EquellaViewerId)

  def cloudViewer(provider: CloudProviderInstance): String = {
    viewerId.filter(vId => viewerForId(provider, vId).isDefined).getOrElse("")
  }
  lazy val providerO = CloudProviderHelper.getByUuid(fields.providerId)

  lazy val directLink =
    for {
      cp          <- providerO
      viewerDeets <- serviceUriForViewer(cp, cloudViewer(cp)).filterNot(_._2.authenticated)
      viewerUri   <- CloudProviderService.serviceUri(cp, viewerDeets._2, uriParameters).toOption

    } yield viewerUri

  def viewerMap(provider: CloudProviderInstance): Option[Map[String, Viewer]] =
    provider.viewers.get(fields.cloudType)

  def viewerForId(provider: CloudProviderInstance, viewerId: String): Option[Viewer] =
    viewerMap(provider).flatMap(_.get(viewerId))

  def serviceUriForViewer(
      provider: CloudProviderInstance,
      viewerId: String
  ): Option[(Viewer, ServiceUrl)] =
    for {
      viewer     <- viewerForId(provider, viewerId)
      serviceUri <- provider.serviceUrls.get(viewer.serviceId)
    } yield (viewer, serviceUri)

  def uriParameters: Map[String, Any] = {
    val metaParams =
      fields.cloudJson.meta
        .map(_.view.mapValues(_.foldWith(MetaJsonFolder)))
        .getOrElse(Map.empty)
        .toMap
    metaParams ++ Map(
      "item"       -> itemId.getUuid,
      "version"    -> itemId.getVersion,
      "attachment" -> attach.getUuid,
      "viewer"     -> viewerId
    )
  }

  override def getCommonAttachmentDetails: util.List[AttachmentDetail] = {
    fields.cloudJson.display
      .getOrElse(Map.empty)
      .map { case (name, value) =>
        AbstractWrappedResource.makeDetail(
          new TextLabel(name + ":"),
          new TextLabel(value.foldWith(CloudAttachmentSerializer.javaFolder).toString)
        )
      }
      .toBuffer
      .asJava
  }

  override def isExternalResource: Boolean = true

  override def createCanonicalUrl(): Bookmark =
    directLink.map(uri => new SimpleBookmark(uri.toString())).getOrElse {
      val vurl = LegacyGuice.viewItemUrlFactory
        .createItemUrl(info, getViewableItem.asInstanceOf[ViewableItem[Item]])
      vurl.add(new ViewAttachmentUrl(attach.getUuid, true))
      viewerId.foreach(vurl.setViewer)
      vurl
    }

  override def getResourceSpecificViewers: util.List[NameValue] =
    providerO
      .flatMap(viewerMap)
      .map { viewers =>
        viewers.toList.collect {
          case (id, viewer) if id.nonEmpty => new NameValue(viewer.name, id)
        }.asJava
      }
      .getOrElse(Collections.emptyList())

  override def getResourceViewer(viewerId: String): ResourceViewer = viewerId match {
    case EquellaViewerId => null
    case vId             => new CloudResourceViewer(vId)
  }

  class CloudResourceViewer(viewerId: String) extends AbstractResourceViewer {
    override def getViewerConfig(resource: ViewableResource): ResourceViewerConfig = null

    override def getViewerId: String = viewerId

    override def getViewerSectionClass: Class[_ <: SectionId] = null

    override def supports(info: SectionInfo, resource: ViewableResource): Boolean = true
  }

  override def hasContentStream: Boolean = directLink.isEmpty

  override def getContentStream: ContentStream = {
    (for {
      provider <- OptionT.fromOption[IO](providerO)
      viewerDetails <- OptionT.fromOption[IO] {
        serviceUriForViewer(provider, cloudViewer(provider))
      }
      response <- OptionT.liftF(
        CloudProviderService.serviceRequest(
          viewerDetails._2,
          provider,
          uriParameters,
          uri => basicRequest.get(uri).response(asStream[Stream[IO, Byte]])
        )
      )
    } yield response).value map {
      case None => EmptyResponseStream
      case Some(response) =>
        response.body match {
          case Right(responseStream) => SttpResponseContentStream(response, responseStream)
          case Left(failure)         => sys.error(failure)
        }
    }
  }.unsafeRunSync
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
    value.toMap.view.mapValues(_.foldWith(this))
}

case class SttpResponseContentStream(
    response: Response[Either[String, fs2.Stream[IO, Byte]]],
    responseStream: fs2.Stream[IO, Byte]
) extends AbstractContentStream(null, response.contentType.orNull) {
  override def getContentLength: Long      = response.contentLength.getOrElse(-1L)
  override def getInputStream: InputStream = null
  override def mustWrite(): Boolean        = true
  override def write(out: OutputStream): Unit = {
    val channel = Channels.newChannel(out)
    responseStream.chunks.map(c => IO(channel.write(c.toByteBuffer))).compile.drain.unsafeRunSync()
  }
}

object EmptyResponseStream extends AbstractContentStream(null, null) {
  override def getInputStream: InputStream = null
  override def getContentLength: Long      = 0L
  override def exists(): Boolean           = false
}
