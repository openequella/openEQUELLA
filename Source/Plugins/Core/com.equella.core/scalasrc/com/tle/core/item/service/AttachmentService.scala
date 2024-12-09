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

package com.tle.core.item.service

import com.tle.beans.item.attachments.{Attachment, CustomAttachment, FileAttachment}
import com.tle.beans.item.{ItemId, ItemIdKey, ItemKey}
import com.tle.legacy.LegacyGuice
import com.tle.web.api.interfaces.beans.AbstractExtendableBean
import com.tle.web.api.item.equella.interfaces.beans.{
  AbstractFileAttachmentBean,
  FileAttachmentBean
}
import com.tle.web.api.item.interfaces.beans.AttachmentBean
import com.tle.web.controls.resource.ResourceAttachmentBean

/** Service layer that deals with Attachment related business logic.
  */
object AttachmentService {

  /** Extract the mimetype for AbstractExtendableBean.
    */
  def getMimetypeForAttachment[T <: AbstractExtendableBean](bean: T): Option[String] =
    bean match {
      case file: AbstractFileAttachmentBean =>
        Some(LegacyGuice.mimeTypeService.getMimeTypeForFilename(file.getFilename))
      case resourceAttachmentBean: ResourceAttachmentBean =>
        Some(
          LegacyGuice.mimeTypeService.getMimeTypeForResourceAttachmentBean(resourceAttachmentBean)
        )
      case _ => None
    }

  /** If the attachment is a file, then return the path for that attachment.
    *
    * @param attachment
    *   a potential file attachment
    * @return
    *   the path of the provided file attachment
    */
  def getFilePathForAttachment(attachment: AttachmentBean): Option[String] =
    attachment match {
      case fileAttachment: FileAttachmentBean => Option(fileAttachment.getFilename)
      case _                                  => None
    }

  /** Determines if attachment contains a generated thumbnail in filestore
    */
  def thumbExists(itemKey: ItemIdKey, attachBean: AttachmentBean): Option[Boolean] = {
    attachBean match {
      case fileBean: FileAttachmentBean =>
        val item = LegacyGuice.viewableItemFactory.createNewViewableItem(itemKey)
        Option(fileBean.getThumbnail).map {
          LegacyGuice.fileSystemService.fileExists(item.getFileHandle, _)
        }
      case _ => None
    }
  }

  /** Determines if a given customAttachment is invalid. Required as these attachments can be
    * recursive.
    * @param customAttachment
    *   The attachment to check.
    * @return
    *   If true, this attachment is broken.
    */
  private def isCustomAttachmentBroken(customAttachment: CustomAttachment): Boolean = {
    val uuid    = customAttachment.getData("uuid").asInstanceOf[String]
    val version = customAttachment.getData("version").asInstanceOf[Int]

    val key = new ItemId(uuid, LegacyGuice.itemService.getRealVersion(version, uuid))

    if (customAttachment.getType != "resource") {
      return false
    }
    customAttachment.getData("type") match {
      case "a" =>
        // Recurse into child attachment
        recurseBrokenAttachmentCheck(key, customAttachment.getUrl).isEmpty
      case "p" =>
        // Get the child item. If it doesn't exist, this is a dead attachment
        Option(LegacyGuice.itemService.getUnsecureIfExists(key)).isEmpty
      case _ => false
    }
  }

  /** Determines if a given attachment is invalid. If it is a resource selector attachment, this
    * gets handled by [[isCustomAttachmentBroken(customAttachment: CustomAttachment)]] which links
    * back in here to recurse through customAttachments to find the root.
    *
    * @param itemKey
    *   the details of the item the attachment belongs to
    * @param attachmentUuid
    *   the UUID of the attachment
    * @return
    *   None if the attachment is broken, otherwise the attachment which was found wrapped in an
    *   Option
    */
  def recurseBrokenAttachmentCheck(itemKey: ItemKey, attachmentUuid: String): Option[Attachment] = {
    // check if file is present in the file-store
    def fileAttachmentExists(fa: FileAttachment): Boolean = {
      val item =
        LegacyGuice.viewableItemFactory.createNewViewableItem(fa.getItem.getItemId)
      LegacyGuice.fileSystemService.fileExists(item.getFileHandle, fa.getFilename)
    }

    def customAttachmentExists(ca: CustomAttachment): Boolean = !isCustomAttachmentBroken(ca)

    LegacyGuice.itemService.getNullableAttachmentForUuid(itemKey, attachmentUuid) match {
      case fa: FileAttachment   => Option(fa).filter(fileAttachmentExists)
      case ca: CustomAttachment => Option(ca).filter(customAttachmentExists)
      case a: Attachment        => Option(a)
      case _                    => None
    }
  }
}
