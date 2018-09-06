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

import com.tle.beans.item.attachments.{Attachment, FileAttachment, ZipAttachment}
import com.tle.web.controls.universal.StagingContext

case class StandardFileCommit(uploaded: SuccessfulUpload, suppressThumb: Boolean, unzippedTo: Option[String]) extends AttachmentCommit
{
  def apply(a: Attachment, stg: StagingContext): Attachment = a match {
    case fa: FileAttachment =>
      stg.moveFile(uploaded.uploadPath, uploaded.originalFilename)
      fa.setFilename(uploaded.originalFilename)
      fa.setThumbnail(if (suppressThumb) WebFileUploads.SUPPRESS_THUMB_VALUE else stg.thumbRequest(uploaded.originalFilename))
      stg.deregisterFilename(uploaded.id)
      unzippedTo.foreach(stg.delete)
      fa
  }

  def cancel(a: Attachment, stg: StagingContext): Unit =
  {
    stg.delete(uploaded.uploadPath)
    unzippedTo.foreach(stg.delete)
    stg.deregisterFilename(uploaded.id)
  }
}

case class ZipFileCommit(original: Option[StandardFileCommit], unzippedPath: String) extends AttachmentCommit
{
  def apply(a: Attachment, stg: StagingContext): Attachment = {
    original match {
      case Some(sfc) =>
        val up = sfc.uploaded
        stg.deregisterFilename(up.id)
        a match {
          case fa: FileAttachment => fa.setFilename(up.originalFilename)
        }
        stg.moveFile(up.uploadPath, WebFileUploads.zipPath(up.originalFilename))
        stg.moveFile(unzippedPath, up.originalFilename)
      case None => a match {
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

case class CleanupUnzipCommit(unzippedPath: String) extends AttachmentCommit
{
  override def apply(a: Attachment, stg: StagingContext): Attachment = {
    stg.delete(unzippedPath)
    a
  }

  override def cancel(a: Attachment, stg: StagingContext): Unit = {
    stg.delete(unzippedPath)
  }
}

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

  override def cancel(a: Attachment, stg: StagingContext): Unit = {

  }
}

object StandardFileCreate {

  def fileAttachmentFromUpload(uploaded: SuccessfulUpload, suppressThumb: Boolean): AttachmentCreate = {
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

    AttachmentCreate(createStaged, StandardFileCommit(uploaded,suppressThumb, None))
  }
}
