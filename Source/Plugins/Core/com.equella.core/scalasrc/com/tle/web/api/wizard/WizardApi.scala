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

package com.tle.web.api.wizard

import java.io.IOException
import java.util.UUID

import cats.data.OptionT
import com.dytech.devlib.PropBagEx
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.tle.beans.item.{Item, ItemEditingException, ItemPack}
import com.tle.core.cloudproviders.{CloudProviderDB, CloudProviderService}
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.item.operations.{ItemOperationParams, WorkflowOperation}
import com.tle.core.item.serializer.impl.AttachmentSerializerProvider
import com.tle.core.item.standard.operations.{
  AbstractStandardWorkflowOperation,
  DuringSaveOperation
}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean
import com.tle.web.wizard.{WizardState, WizardStateInterface}
import com.tle.web.wizard.impl.WizardServiceImpl.WizardSessionState
import io.swagger.annotations.Api
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.{GET, POST, PUT, Path, PathParam, QueryParam}
import com.softwaremill.sttp._
import com.tle.common.filesystem.FileEntry
import scala.collection.JavaConverters._

object WizardApi {
  lazy val editorMap = LegacyGuice.attachmentDeserializers.getBeanMap.asScala
}

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "command")
@JsonSubTypes(
  Array(
    new Type(value = classOf[AddAttachment], name = "addAttachment"),
    new Type(value = classOf[EditAttachment], name = "editAttachment"),
    new Type(value = classOf[DeleteAttachment], name = "deleteAttachment")
  )
)
sealed trait ItemEditCommand
case class AddAttachment(attachment: EquellaAttachmentBean, xmlPath: Option[String])
    extends ItemEditCommand
case class EditAttachment(attachment: EquellaAttachmentBean)       extends ItemEditCommand
case class DeleteAttachment(uuid: String, xmlPath: Option[String]) extends ItemEditCommand

case class ItemEdits(xml: Option[String], edits: Iterable[ItemEditCommand])

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new Type(value = classOf[AddAttachmentResponse], name = "added"),
    new Type(value = classOf[EditAttachmentResponse], name = "edited"),
    new Type(value = classOf[DeleteAttachmentResponse], name = "deleted")
  )
)
sealed trait ItemEditResponse
case class AddAttachmentResponse(attachment: EquellaAttachmentBean)  extends ItemEditResponse
case class EditAttachmentResponse(attachment: EquellaAttachmentBean) extends ItemEditResponse
case class DeleteAttachmentResponse(uuid: String)                    extends ItemEditResponse

case class ItemEditResponses(xml: String, results: Iterable[ItemEditResponse])

case class FileInfo(size: Long, files: Option[Map[String, FileInfo]])
case class ItemState(xml: String,
                     attachments: Iterable[EquellaAttachmentBean],
                     files: Map[String, FileInfo])

@Api("Wizard editing")
@Path("wizard/{wizid}")
class WizardApi {

  val attachTypeMap = LegacyGuice.attachmentDeserializers.getBeanMap

  def editWizardSate[A](wizid: String, req: HttpServletRequest)(f: WizardStateInterface => A): A = {
    val sessionService = LegacyGuice.userSessionService
    sessionService.reenableSessionUse()
    val wss = sessionService.getAttribute(wizid).asInstanceOf[WizardSessionState]
    val wsi = wss.getWizardState
    val res = f(wsi)
    sessionService.setAttribute(wizid, new WizardSessionState(wsi))
    res
  }

  @GET
  @Path("state")
  def getState(@PathParam("wizid") wizid: String, @Context req: HttpServletRequest): ItemState = {
    editWizardSate(wizid, req) { wsi =>
      val attachments = wsi.getItem.getAttachments.asScala.map(a =>
        AttachmentSerializerProvider.serializeAttachment(a, attachTypeMap))
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
      ItemState(itemPack.getXml.toString, attachments, writeFiles(files.getFiles.asScala))
    }
  }

  @PUT
  @Path("edit")
  def editAttachments(@PathParam("wizid") wizid: String,
                      itemEdit: ItemEdits,
                      @Context req: HttpServletRequest): ItemEditResponses = {
    editWizardSate(wizid, req) { wsi =>
      val editor              = new WizardItemEditor(wsi)
      val existingAttachments = wsi.getItem.getAttachments.asScala.map(_.getUuid)
      val itemPack            = wsi.getItemPack

      itemEdit.xml.foreach { xml =>
        itemPack.setXml(new PropBagEx(xml))
      }

      def serializeAttach(uuid: String): EquellaAttachmentBean =
        AttachmentSerializerProvider.serializeAttachment(editor.attachmentMap(uuid), attachTypeMap)

      val responses = itemEdit.edits.map {
        case AddAttachment(attachment, xmlPath) =>
          val edited = WizardApi
            .editorMap(attachment.getRawAttachmentType)
            .deserialize(attachment, editor)
          xmlPath.foreach(p => itemPack.getXml.newSubtree(p).setNode("", edited))
          existingAttachments += edited
          AddAttachmentResponse(serializeAttach(edited))
        case DeleteAttachment(uuid, xmlPath) =>
          existingAttachments -= uuid
          xmlPath.foreach(p => itemPack.getXml.deleteAllWithValue(p, uuid))
          DeleteAttachmentResponse(uuid)
        case EditAttachment(attachment) =>
          WizardApi
            .editorMap(attachment.getRawAttachmentType)
            .deserialize(attachment, editor)
          EditAttachmentResponse(serializeAttach(attachment.getUuid))
        case c => throw new ItemEditingException(s"Invalid item edit command: $c")
      }
      wsi.setItemPack(itemPack)
      editor.editAttachmentOrder(existingAttachments.asJava)
      editor.finishedEditing(false)
      ItemEditResponses(wsi.getItemxml.toString, responses)
    }
  }

  @POST
  @Path("notify")
  def registerCallback(@PathParam("wizid") wizid: String,
                       @QueryParam("providerId") providerId: String,
                       @Context req: HttpServletRequest): Response = {
    editWizardSate(wizid, req) {
      case ws: WizardState =>
        ws.setWizardSaveOperation(providerId, NotifyProvider(UUID.fromString(providerId)))
    }
    Response.ok().build()
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
        serviceUri <- OptionT.fromOption[DB](cp.serviceUris.get("itemNotification"))
        notifyParams = Map("uuid" -> getItem.getUuid, "version" -> getItem.getVersion.toString)
        _ <- OptionT.liftF(
          CloudProviderService.serviceRequest(serviceUri, cp, notifyParams, sttp.post))
      } yield false).getOrElse(false)
    }
  }
}
