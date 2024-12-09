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

package com.tle.web.controls.universal.handlers.fileupload

import com.tle.beans.item.attachments.{Attachment, FileAttachment, ZipAttachment}
import com.tle.web.controls.universal.StagingContext

/** Standard file commit. If it's not an IMS package file then all types of file would belongs to
  * StandardFileCommit at first.
  *
  * @param uploaded
  *   The uploaded file.
  * @param suppressThumb
  *   Do not generate thumbnails if it's true.
  * @param unzippedTo
  *   Where the zip file should be unzipped to.
  */
case class StandardFileCommit(
    uploaded: SuccessfulUpload,
    suppressThumb: Boolean,
    unzippedTo: Option[String]
) extends AttachmentCommit {
  def apply(a: Attachment, stg: StagingContext): Attachment = a match {
    case fa: FileAttachment =>
      stg.moveFile(uploaded.uploadPath, uploaded.originalFilename)
      fa.setFilename(uploaded.originalFilename)
      fa.setThumbnail(
        if (suppressThumb) WebFileUploads.SUPPRESS_THUMB_VALUE
        else stg.thumbRequest(uploaded.originalFilename)
      )
      stg.deregisterFilename(uploaded.id)
      unzippedTo.foreach(stg.delete)
      fa
  }

  def cancel(a: Attachment, stg: StagingContext): Unit = {
    stg.delete(uploaded.uploadPath)
    unzippedTo.foreach(stg.delete)
    stg.deregisterFilename(uploaded.id)
  }
}

/** Zip file commit type. This is not a specific commit type for zip file. Only if user uploads a
  * zip file and then triggered the `unzip operation` the commit type would become this.
  *
  * @param original
  *   The commit type before becoming a `ZipFileCommit`.
  * @param unzippedPath
  *   Where the zip file should be unzipped to.
  */
case class ZipFileCommit(original: Option[StandardFileCommit], unzippedPath: String)
    extends AttachmentCommit {
  def apply(a: Attachment, stg: StagingContext): Attachment = {
    original match {
      // when create file
      case Some(sfc) =>
        val up = sfc.uploaded
        stg.deregisterFilename(up.id)
        a match {
          case fa: FileAttachment => fa.setFilename(up.originalFilename)
        }
        // move zip file from _uploads to _zip path
        stg.moveFile(up.uploadPath, WebFileUploads.zipPath(up.originalFilename))
        // move unzip folder out of _uploads
        stg.moveFile(unzippedPath, up.originalFilename)
      // when edit existing file
      case None =>
        a match {
          case fa: FileAttachment =>
            stg.moveFile(fa.getFilename, WebFileUploads.zipPath(fa.getFilename))
            stg.moveFile(unzippedPath, fa.getFilename)
        }
    }
    a
  }

  override def cancel(a: Attachment, stg: StagingContext): Unit = {
    original.foreach(_.cancel(a, stg))
  }
}

/** Cleanup unzip commit type. If users chooses `unzip file` and then `remove unzip files` in the
  * same edit or create session, then the commit type would become this.
  *
  * @param unzippedPath
  *   Where the original zip file was unzipped to.
  */
case class CleanupUnzipCommit(unzippedPath: String) extends AttachmentCommit {
  override def apply(a: Attachment, stg: StagingContext): Attachment = {
    stg.delete(unzippedPath)
    a
  }

  override def cancel(a: Attachment, stg: StagingContext): Unit = {
    stg.delete(unzippedPath)
  }
}

/** Remove zip commit type. This is used after you `unzip file` and save the file, then choose
  * editing the file and triggering `remove unzip files` operation the commit type will become this.
  *
  * The difference between RemoveZipCommit and CleanupUnzipCommit: when you choose `unzip file`
  * operation, the commit type will become ZipFileCommit. At this point you can save, or remove
  * unzip files. If you choose to remove unzip files, then the commit type will become
  * CleanupUnzipCommit, which means undo the unzip action, because the ZipFileCommit is not actually
  * executed. But if you choose the save action, the ZipFileCommit will be executed. So next time
  * when you edit this file, you will see all unzip files are already there, and then once you
  * trigger `remove unzip files` operation, the commit type will become RemoveZipCommit, which means
  * remove your last ZipFileCommit.
  */
object RemoveZipCommit extends AttachmentCommit {
  override def apply(a: Attachment, stg: StagingContext): Attachment = {
    a match {
      case zf: ZipAttachment =>
        val newFn = WebFileUploads.removeZipPath(zf.getUrl)
        stg.delete(newFn)
        stg.moveFile(zf.getUrl, newFn)
    }
    a
  }
  override def cancel(a: Attachment, stg: StagingContext): Unit = {}
}
