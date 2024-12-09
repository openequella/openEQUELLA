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

import java.util.UUID

import com.tle.beans.item.attachments.{Attachment, CustomAttachment}
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor
import io.circe.Json

class CloudAttachmentEditor extends AbstractCustomAttachmentEditor {
  def finish() = {
    CloudAttachmentJson.encodeJson(customAttachment, cloudJson)
  }

  var cloudJson: CloudAttachmentJson = null

  override def setAttachment(attachment: Attachment): Unit = {
    super.setAttachment(attachment)
    cloudJson = CloudAttachmentJson.decodeJson(attachment.asInstanceOf[CustomAttachment])
  }

  def editVendorId(vendorId: String): Unit = customAttachment.setValue3(vendorId)

  def editCloudType(cloudType: String): Unit = customAttachment.setValue2(cloudType)

  def editProviderId(providerId: UUID): Unit = {
    cloudJson = cloudJson.copy(providerId = providerId)
  }

  def editDisplay(display: Option[Map[String, Json]]): Unit = {
    cloudJson = cloudJson.copy(display = display)
  }

  def editMeta(meta: Option[Map[String, Json]]): Unit = {
    cloudJson = cloudJson.copy(meta = meta)
  }

  def editIndexText(indexText: Option[String]): Unit = {
    cloudJson = cloudJson.copy(indexText = indexText)
  }

  def editIndexFiles(indexFiles: Option[Iterable[String]]): Unit = {
    cloudJson = cloudJson.copy(indexFiles = indexFiles)
  }

  override def getCustomType: String = "cloud"

  override def newAttachment(): Attachment = {
    val attach = super.newAttachment()
    CloudAttachmentJson.encodeJson(
      attach.asInstanceOf[CustomAttachment],
      CloudAttachmentJson.defaultJson
    )
    attach
  }
}
