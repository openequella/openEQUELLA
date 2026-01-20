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

import java.util.{Date, Objects, UUID}
import java.{lang, util}

import com.dytech.devlib.PropBagEx
import com.tle.beans.item.attachments.Attachment
import com.tle.beans.item.{ItemEditingException, ItemIdKey}
import com.tle.common.filesystem.handle.FileHandle
import com.tle.core.item.edit.attachment.{AttachmentEditor, AttachmentEditorProvider}
import com.tle.core.item.edit.impl.ItemEditorImpl
import com.tle.core.item.edit.{DRMEditor, ItemEditor, ItemEditorChangeTracker, NavigationEditor}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean
import com.tle.web.wizard.WizardStateInterface
import scala.jdk.CollectionConverters._
import scala.collection.mutable

class WizardItemEditor(wsi: WizardStateInterface) extends ItemEditor with ItemEditorChangeTracker {

  val item       = wsi.getItem
  val itemPack   = wsi.getItemPack
  var xml        = itemPack.getXml
  val fileHandle = wsi.getFileHandle

  val attachmentMap   = mutable.Map[String, Attachment]()
  val attachmentOrder = mutable.Buffer[String]()

  item.getAttachments.asScala.foreach { attachment =>
    val attachUuid = attachment.getUuid
    attachmentMap.put(attachUuid, attachment)
    attachmentOrder += attachUuid
  }

  override def doEdits(itemBean: EquellaItemBean): Unit = unsupported

  override def finishedEditing(ensureOnIndexList: Boolean): ItemIdKey = {
    val attachments = item.getAttachments
    attachmentOrder.iterator.zipWithIndex.foreach { case (attachUuid, i) =>
      val attach = attachmentMap(attachUuid)
      if (attachments.size <= i) attachments.add(attach)
      else attachments.set(i, attach)
    }
    val existingSize = attachments.size()
    val newSize      = attachmentOrder.size
    if (existingSize > newSize) {
      Range(newSize, existingSize).foreach(attachments.remove)
    }
    itemPack.setXml(xml)
    wsi.setItemPack(itemPack)
    null
  }

  def unsupported = throw new UnsupportedOperationException("Can't be called in this context")

  override def getMetadata: PropBagEx = xml

  override def preventSaveScript(): Unit = unsupported

  override def editDates(dateCreated: Date, dateModified: Date): Unit = unsupported

  override def editItemStatus(status: String): Unit = unsupported

  override def editOwner(owner: String): Unit = unsupported

  override def editCollaborators(collaborators: util.Set[String]): Unit = unsupported

  override def editRating(rating: lang.Float): Unit = unsupported

  override def editMetadata(xml: String): Unit = unsupported

  override def editMetadata(xml: PropBagEx): Unit = this.xml = xml

  override def editThumbnail(thumbnail: String): Unit = unsupported

  override def getAttachmentEditor[T <: AttachmentEditor](uuid_ : String, `type`: Class[T]): T = {
    val (uuid, existingAttachmentO) = Option(uuid_) match {
      case None => (UUID.randomUUID().toString, None)
      case Some(exUuid) =>
        ItemEditorImpl.checkValidUuid(exUuid)
        (exUuid, attachmentMap.get(exUuid))
    }
    val attachEditor = AttachmentEditorProvider.createEditorForType(`type`.getName)
    attachEditor.setItemEditorChangeTracker(this)
    attachEditor.setItem(item)
    attachEditor.setFileHandle(fileHandle)
    val attachment = existingAttachmentO.filter(attachEditor.canEdit).getOrElse {
      val attachment = attachEditor.newAttachment
      attachment.setUuid(uuid)
      attachmentMap.put(uuid, attachment)
      attachment
    }
    attachEditor.setAttachment(attachment)
    return `type`.cast(attachEditor)
  }

  override def editAttachmentOrder(_attachmentUuids: util.List[String]): Unit = {
    val attachmentUuids = _attachmentUuids.asScala
    if (attachmentOrder != attachmentUuids) {
      attachmentOrder.clear()
      attachmentOrder ++= attachmentUuids
    }
  }

  override def processExportDetails(itemBean: EquellaItemBean): Unit = unsupported

  override def getNavigationEditor: NavigationEditor = unsupported

  override def getDRMEditor: DRMEditor = unsupported

  override def unlock(): Unit = unsupported

  override def getFileHandle: FileHandle = fileHandle

  override def isNewItem: Boolean = unsupported

  override def isForceFileCheck: Boolean = false

  override def hasBeenEdited(oldValue: Any, newValue: Any): Boolean = {
    if (Objects.equals(oldValue, newValue)) false
    else {
      editDetected()
      true
    }
  }

  override def editDetected(): Unit = {}

  override def attachmentEditDetected(): Unit = {}

  override def addIndexingEdit(editType: String): Unit = {}

  override def editWithPrivilege(priv: String): Unit = {}

  override def attachmentForUuid(uuid: String): Attachment = attachmentMap.get(uuid).orNull

  override def getAttachmentOrder: lang.Iterable[String] = attachmentOrder.asJava
}
