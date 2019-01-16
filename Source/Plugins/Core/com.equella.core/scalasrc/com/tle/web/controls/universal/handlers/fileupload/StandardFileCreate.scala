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

package com.tle.web.controls.universal.handlers.fileupload

import com.tle.beans.item.attachments.{Attachment, FileAttachment}
import com.tle.web.controls.universal.StagingContext

object StandardFileCreate {

  def fileAttachmentFromUpload(uploaded: SuccessfulUpload,
                               suppressThumb: Boolean): AttachmentCreate = {
    def createStaged(stg: StagingContext) = {
      val fa = new FileAttachment
      fa.setFilename(uploaded.uploadPath)
      fa.setDescription(uploaded.description)
      fa.setMd5sum(uploaded.fileInfo.getMd5CheckSum)
      fa.setSize(uploaded.fileInfo.getLength)
      stg.gatherAdditionalMetadata(uploaded.originalFilename).foreach { a =>
        fa.setData(a._1, a._2)
      }
      fa
    }

    def commit(a: Attachment, stg: StagingContext): Attachment = a match {
      case fa: FileAttachment =>
        stg.moveFile(uploaded.uploadPath, uploaded.originalFilename)
        fa.setFilename(uploaded.originalFilename)
        fa.setThumbnail(
          if (suppressThumb) WebFileUploads.SUPPRESS_THUMB_VALUE
          else stg.thumbRequest(uploaded.originalFilename))
        stg.deregisterFilename(uploaded.id)
        fa
    }

    AttachmentCreate(createStaged,
                     commit,
                     (_, stg) => stg.delete(uploaded.uploadPath))
  }
}
