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

package com.tle.web.api.wizard

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.tle.beans.item.{Item, ItemPack}
import com.tle.common.filesystem.FileEntry
import com.tle.core.cloudproviders.{CloudProviderHelper, CloudProviderService}
import com.tle.core.item.operations.{ItemOperationParams, WorkflowOperation}
import com.tle.core.item.standard.operations.DuringSaveOperation
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean
import com.tle.web.api.item.{ItemEditResponses, ItemEdits}
import com.tle.web.wizard.impl.WizardServiceImpl.WizardSessionState
import com.tle.web.wizard.{WizardState, WizardStateInterface}
import fs2.Stream
import fs2.io._
import io.swagger.annotations.Api
import org.jboss.resteasy.annotations.cache.NoCache
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3._
import sttp.model.{HeaderNames, Uri}

import java.io.{InputStream, OutputStream}
import java.nio.channels.Channels
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import javax.ws.rs.core.Response.{ResponseBuilder, Status}
import javax.ws.rs.core.{Context, Response, StreamingOutput, UriInfo}
import scala.jdk.CollectionConverters._

case class FileInfo(size: Long, files: Option[Map[String, FileInfo]])
case class ItemState(
    xml: String,
    attachments: Iterable[EquellaAttachmentBean],
    files: Map[String, FileInfo],
    stateVersion: Int
)

@Api("Wizard editing")
@Path("wizard/{wizid}")
class WizardApi {

  def withWizardState[A](wizid: String, req: HttpServletRequest, edit: Boolean)(
      f: WizardStateInterface => A
  ): A = {
    val sessionService = LegacyGuice.userSessionService
    sessionService.reenableSessionUse()
    Option(sessionService.getAttribute(wizid).asInstanceOf[WizardSessionState])
      .map { wss =>
        val wsi = wss.getWizardState
        sessionService.getSessionLock.synchronized {
          if (edit) {
            wsi match {
              case wizstate: WizardState => wizstate.incrementVersion()
              case _                     => ()
            }
          }
          val res = f(wsi)
          if (edit) sessionService.setAttribute(wizid, new WizardSessionState(wsi))
          res
        }
      }
      .getOrElse(throw new WebApplicationException(404))
  }

  @GET
  @NoCache
  @Path("state")
  def getState(@PathParam("wizid") wizid: String, @Context req: HttpServletRequest): ItemState = {
    withWizardState(wizid, req, false) { wsi =>
      val attachments =
        wsi.getItem.getAttachments.asScala.map(a =>
          ItemEdits.attachmentSerializers.serializeAttachment(a)
        )
      val itemPack = wsi.getItemPack

      def writeFile(fileInfo: FileEntry): (String, FileInfo) = {
        val childFiles = if (fileInfo.isFolder) {
          Some(writeFiles(fileInfo.getFiles.asScala))
        } else {
          None
        }
        (fileInfo.getName, FileInfo(fileInfo.getLength, childFiles))
      }

      def writeFiles(entries: Iterable[FileEntry]): Map[String, FileInfo] = {
        entries.map(writeFile).toMap
      }
      val files = LegacyGuice.fileSystemService.enumerateTree(wsi.getFileHandle, "", null)
      ItemState(
        itemPack.getXml.toString,
        attachments,
        writeFiles(files.getFiles.asScala),
        wsi.getStateVersion
      )
    }
  }

  @PUT
  @Path("edit")
  def editAttachments(
      @PathParam("wizid") wizid: String,
      itemEdit: ItemEdits,
      @Context req: HttpServletRequest
  ): ItemEditResponses = {
    withWizardState(wizid, req, true) { wsi =>
      val editor   = new WizardItemEditor(wsi)
      val response = ItemEdits.performEdits(itemEdit, editor)
      editor.finishedEditing(false)
      response
    }
  }

  @POST
  @Path("notify")
  def registerCallback(
      @PathParam("wizid") wizid: String,
      @QueryParam("providerId") providerId: String,
      @Context req: HttpServletRequest
  ): Response = {
    withWizardState(wizid, req, true) { case ws: WizardState =>
      ws.setWizardSaveOperation(providerId, NotifyProvider(UUID.fromString(providerId)))
    }
    Response.ok().build()
  }

  def streamedResponse(
      response: sttp.client3.Response[Either[String, Stream[IO, Byte]]]
  ): ResponseBuilder = {
    val statusCode = response.code.code

    response.body match {
      case Right(responseStream) =>
        val stream = new StreamingOutput {
          override def write(output: OutputStream): Unit = {
            val channel = Channels.newChannel(output)
            responseStream.chunks
              .evalMap(c => IO(channel.write(c.toByteBuffer)))
              .compile
              .drain
              .unsafeRunSync()
          }
        }
        Response
          .status(statusCode)
          .header(HeaderNames.ContentType, response.contentType.orNull)
          .header(HeaderNames.ContentLength, response.contentLength.orNull)
          .entity(stream)
      case Left(_) => Response.status(statusCode)
    }
  }

  private def proxyRequest[T](
      wizid: String,
      request: HttpServletRequest,
      providerId: UUID,
      serviceId: String,
      uriInfo: UriInfo
  )(f: Uri => Request[T, Fs2Streams[IO]]): Response = {
    withWizardState(wizid, request, false) { _ =>
      ()
    }
    val queryParams = uriInfo.getQueryParameters.asScala.view.mapValues(_.asScala).toMap
    CloudProviderHelper
      .getByUuid(providerId)
      .flatMap(cp => {
        cp.serviceUrls
          .get(serviceId)
          .map(serviceUri =>
            CloudProviderService
              .serviceRequest(
                serviceUri,
                cp,
                queryParams,
                uri => f(uri).response(asStreamUnsafe(Fs2Streams[IO]))
              )
              .unsafeRunSync
          )
          .map(streamedResponse)
      })
      .getOrElse(Response.status(Status.NOT_FOUND))
      .build()
  }

  private def getStreamedBody(content: InputStream): Stream[IO, Byte] =
    readInputStream(IO(content), 4096)

  private def getRequestHeaders(req: HttpServletRequest): Map[String, String] = {
    val headers = (for {
      headerName <- req.getHeaderNames.asScala
    } yield (headerName, req.getHeader(headerName))).toMap

    val filterCookies = {
      val filterList = List("JSESSIONID")
      val cookies =
        req.getCookies.filter(cookie => filterList.exists(!_.startsWith(cookie.getName)))
      // Generate a string which include cookie pairs separated by a semi-colon
      cookies.map(cookie => s"${cookie.getName}=${cookie.getValue}").mkString(";")
    }
    // If have cookies apart from those unneeded then reset cookie in the header; otherwise remove cookie from the header.
    val filterHeaders = {
      if (!filterCookies.isEmpty) {
        headers + ("cookie" -> filterCookies)
      } else {
        headers - "cookie"
      }
    }
    filterHeaders - "host"
  }

  @NoCache
  @GET
  @Path("provider/{providerId}/{serviceId}")
  def proxyGET(
      @PathParam("wizid") wizid: String,
      @PathParam("providerId") providerId: UUID,
      @PathParam("serviceId") serviceId: String,
      @Context uriInfo: UriInfo,
      @Context req: HttpServletRequest
  ): Response = {
    proxyRequest(wizid, req, providerId, serviceId, uriInfo) { uri =>
      basicRequest.get(uri).headers(getRequestHeaders(req))
    }
  }

  @POST
  @Path("provider/{providerId}/{serviceId}")
  def proxyPOST(
      @PathParam("wizid") wizid: String,
      @PathParam("providerId") providerId: UUID,
      @PathParam("serviceId") serviceId: String,
      @Context uriInfo: UriInfo,
      @Context req: HttpServletRequest,
      content: InputStream
  ): Response = {
    proxyRequest(wizid, req, providerId, serviceId, uriInfo) { uri =>
      basicRequest
        .post(uri)
        .headers(getRequestHeaders(req))
        .streamBody(Fs2Streams[IO])(getStreamedBody(content))
    }
  }

  @PUT
  @Path("provider/{providerId}/{serviceId}")
  def proxyPUT(
      @PathParam("wizid") wizid: String,
      @PathParam("providerId") providerId: UUID,
      @PathParam("serviceId") serviceId: String,
      @Context uriInfo: UriInfo,
      @Context req: HttpServletRequest,
      content: InputStream
  ): Response = {
    proxyRequest(wizid, req, providerId, serviceId, uriInfo) { uri =>
      basicRequest
        .put(uri)
        .headers(getRequestHeaders(req))
        .streamBody(Fs2Streams[IO])(getStreamedBody(content))
    }
  }

  @DELETE
  @Path("provider/{providerId}/{serviceId}")
  def proxyDELETE(
      @PathParam("wizid") wizid: String,
      @PathParam("providerId") providerId: UUID,
      @PathParam("serviceId") serviceId: String,
      @Context uriInfo: UriInfo,
      @Context req: HttpServletRequest
  ): Response = {
    proxyRequest(wizid, req, providerId, serviceId, uriInfo) { uri =>
      basicRequest.delete(uri).headers(getRequestHeaders(req))
    }
  }
}

class SimpleWorkflowOperation extends WorkflowOperation {
  var params: ItemOperationParams = null;

  override def setParams(params: ItemOperationParams): Unit = this.params = params

  override def getItemPack: ItemPack[Item] = params.getItemPack

  override def getItem: Item = getItemPack.getItem

  override def execute(): Boolean = false

  override def isReadOnly: Boolean = true

  override def failedToAutowire(): Boolean = false

  override def isDeleteLike: Boolean = false
}

case class NotifyProvider(providerId: UUID) extends DuringSaveOperation with Serializable {
  override def getName: String = "Notify providers"

  override def createPreSaveWorkflowOperation(): WorkflowOperation = new SimpleWorkflowOperation

  override def createPostSaveWorkflowOperation(): WorkflowOperation = new SimpleWorkflowOperation {
    override def execute(): Boolean =
      CloudProviderHelper
        .getByUuid(providerId)
        .flatMap(cp =>
          cp.serviceUrls
            .get("itemNotification")
            .map(serviceUri => {
              CloudProviderService
                .serviceRequest(
                  serviceUri,
                  cp,
                  Map("uuid" -> getItem.getUuid, "version" -> getItem.getVersion.toString),
                  basicRequest.post
                )
                .unsafeRunSync
              false
            })
        )
        .getOrElse(false)
  }
}
