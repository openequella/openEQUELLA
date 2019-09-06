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

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.util.UUID

import cats.data.OptionT
import cats.effect.IO
import com.softwaremill.sttp._
import com.tle.beans.item.{Item, ItemPack}
import com.tle.common.filesystem.FileEntry
import com.tle.core.cloudproviders.{CloudProviderDB, CloudProviderService}
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.httpclient._
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
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import javax.ws.rs.core.Response.{ResponseBuilder, Status}
import javax.ws.rs.core.{Context, Response, StreamingOutput, UriInfo}
import org.jboss.resteasy.annotations.cache.NoCache

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits

case class FileInfo(size: Long, files: Option[Map[String, FileInfo]])
case class ItemState(xml: String,
                     attachments: Iterable[EquellaAttachmentBean],
                     files: Map[String, FileInfo],
                     stateVersion: Int)

@Api("Wizard editing")
@Path("wizard/{wizid}")
class WizardApi {

  def withWizardState[A](wizid: String, req: HttpServletRequest, edit: Boolean)(
      f: WizardStateInterface => A): A = {
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
          ItemEdits.attachmentSerializers.serializeAttachment(a))
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
      ItemState(itemPack.getXml.toString,
                attachments,
                writeFiles(files.getFiles.asScala),
                wsi.getStateVersion)
    }
  }

  @PUT
  @Path("edit")
  def editAttachments(@PathParam("wizid") wizid: String,
                      itemEdit: ItemEdits,
                      @Context req: HttpServletRequest): ItemEditResponses = {
    withWizardState(wizid, req, true) { wsi =>
      val editor   = new WizardItemEditor(wsi)
      val response = ItemEdits.performEdits(itemEdit, editor)
      editor.finishedEditing(false)
      response
    }
  }

  @POST
  @Path("notify")
  def registerCallback(@PathParam("wizid") wizid: String,
                       @QueryParam("providerId") providerId: String,
                       @Context req: HttpServletRequest): Response = {
    withWizardState(wizid, req, true) {
      case ws: WizardState =>
        ws.setWizardSaveOperation(providerId, NotifyProvider(UUID.fromString(providerId)))
    }
    Response.ok().build()
  }

  def streamedResponse(
      response: com.softwaremill.sttp.Response[Stream[IO, ByteBuffer]]): ResponseBuilder = {
    response.body match {
      case Right(responseStream) =>
        val stream = new StreamingOutput {
          override def write(output: OutputStream): Unit = {
            val channel = Channels.newChannel(output)
            responseStream.evalMap(bb => IO(channel.write(bb))).compile.drain.unsafeRunSync()
          }
        }
        Response
          .status(response.code)
          .header(HeaderNames.ContentType, response.contentType.orNull)
          .header(HeaderNames.ContentLength, response.contentLength.orNull)
          .entity(stream)
      case _ => Response.status(response.code)
    }
  }

  private def proxyRequest[T](
      wizid: String,
      request: HttpServletRequest,
      providerId: UUID,
      serviceId: String,
      uriInfo: UriInfo)(f: Uri => Request[T, Stream[IO, ByteBuffer]]): Response = {
    withWizardState(wizid, request, false) { _ =>
      ()
    }
    val queryParams = uriInfo.getQueryParameters.asScala.mapValues(_.asScala).toMap
    RunWithDB
      .execute {
        (for {
          cp         <- CloudProviderDB.get(providerId)
          serviceUri <- OptionT.fromOption[DB](cp.serviceUrls.get(serviceId))
          response <- OptionT.liftF(
            CloudProviderService.serviceRequest(
              serviceUri,
              cp,
              queryParams,
              uri => f(uri).response(asStream[Stream[IO, ByteBuffer]])))
        } yield streamedResponse(response))
          .getOrElse(Response.status(Status.NOT_FOUND))
      }
      .build()

  }

  @GET
  @Path("provider/{providerId}/{serviceId}")
  def proxyGET(@PathParam("wizid") wizid: String,
               @PathParam("providerId") providerId: UUID,
               @PathParam("serviceId") serviceId: String,
               @Context uriInfo: UriInfo,
               @Context req: HttpServletRequest): Response = {
    proxyRequest(wizid, req, providerId, serviceId, uriInfo)(sttp.get)
  }

  @POST
  @Path("provider/{providerId}/{serviceId}")
  def proxyPOST(@PathParam("wizid") wizid: String,
                @PathParam("providerId") providerId: UUID,
                @PathParam("serviceId") serviceId: String,
                @Context uriInfo: UriInfo,
                @Context req: HttpServletRequest,
                content: InputStream): Response = {
    val streamedBody =
      readInputStream(IO(content), 4096, Implicits.global).chunks.map(_.toByteBuffer)
    proxyRequest(wizid, req, providerId, serviceId, uriInfo) { uri =>
      sttp
        .post(uri)
        .streamBody(streamedBody)
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
    override def execute(): Boolean = RunWithDB.executeWithHibernate {
      (for {
        cp         <- CloudProviderDB.get(providerId)
        serviceUri <- OptionT.fromOption[DB](cp.serviceUrls.get("itemNotification"))
        notifyParams = Map("uuid" -> getItem.getUuid, "version" -> getItem.getVersion.toString)
        _ <- OptionT.liftF(
          CloudProviderService.serviceRequest(serviceUri, cp, notifyParams, sttp.post))
      } yield false).getOrElse(false)
    }
  }
}
