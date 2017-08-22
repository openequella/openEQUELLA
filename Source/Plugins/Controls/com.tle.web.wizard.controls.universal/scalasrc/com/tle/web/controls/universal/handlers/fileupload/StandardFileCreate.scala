package com.tle.web.controls.universal.handlers.fileupload

import com.tle.beans.item.attachments.FileAttachment

object StandardFileCreate {

  def fileAttachmentFromUpload(uploaded: SuccessfulUpload, suppressThumb: Boolean): AttachmentCreate =
    AttachmentCreate({ stg =>
      stg.moveFile(uploaded.uploadPath, uploaded.originalFilename)
      stg.deregisterFilename(uploaded.id)
      val fa = new FileAttachment
      fa.setFilename(uploaded.originalFilename)
      fa.setDescription(uploaded.description)
      fa.setMd5sum(uploaded.fileInfo.getMd5CheckSum)
      fa.setSize(uploaded.fileInfo.getLength)
      if (suppressThumb) fa.setThumbnail(WebFileUploads.SUPPRESS_THUMB_VALUE)
      stg.gatherAdditionalMetadata(uploaded.originalFilename).foreach { a =>
        fa.setData(a._1, a._2)
      }
      fa
    }, (_,stg) => stg.delete(uploaded.originalFilename))
}
