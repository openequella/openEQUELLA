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

package com.tle.web.api.item

import com.dytech.devlib.PropBagEx
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.tle.beans.item.{Item, ItemEditingException}
import com.tle.core.item.edit.ItemEditor
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean

import scala.collection.JavaConverters._

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

case class ItemEdits(xml: Option[String], edits: Iterable[ItemEditCommand])
case class ItemEditResponses(xml: String, results: Iterable[ItemEditResponse])

object ItemEdits {
  val attachmentSerializers = LegacyGuice.attachmentSerializerProvider
  val editorMap             = attachmentSerializers.getAttachmentSerializers.asScala

  def performEdits(itemEdit: ItemEdits, editor: ItemEditor): ItemEditResponses = {

    val metadata            = itemEdit.xml.map(new PropBagEx(_)).getOrElse(editor.getMetadata)
    val existingAttachments = editor.getAttachmentOrder.asScala.toBuffer

    def serializeAttach(uuid: String): EquellaAttachmentBean =
      attachmentSerializers.serializeAttachment(editor.attachmentForUuid(uuid))

    val responses = itemEdit.edits.map {
      case AddAttachment(attachment, xmlPath) =>
        val edited = editorMap(attachment.getRawAttachmentType)
          .deserialize(attachment, editor)
        xmlPath.foreach(p => metadata.newSubtree(p).setNode("", edited))
        existingAttachments += edited
        AddAttachmentResponse(serializeAttach(edited))
      case DeleteAttachment(uuid, xmlPath) =>
        existingAttachments -= uuid
        xmlPath.foreach(p => metadata.deleteAllWithValue(p, uuid))
        DeleteAttachmentResponse(uuid)
      case EditAttachment(attachment) =>
        val attachUuid = attachment.getUuid
        Option(editor.attachmentForUuid(attachUuid))
          .map { _ =>
            editorMap(attachment.getRawAttachmentType)
              .deserialize(attachment, editor)
            EditAttachmentResponse(serializeAttach(attachUuid))
          }
          .getOrElse(throw new ItemEditingException(s"No attachment for uuid '$attachUuid'"))
      case c => throw new ItemEditingException(s"Invalid item edit command: $c")
    }
    editor.editMetadata(metadata)
    editor.editAttachmentOrder(existingAttachments.asJava)
    ItemEditResponses(metadata.toString, responses)
  }
}
