package com.tle.web.controls.universal.handlers.fileupload

import java.util.UUID

import com.tle.beans.item.attachments.Attachment
import com.tle.web.controls.universal.StagingContext

case class AttachmentCreate(create: StagingContext => Attachment, cancel: (Attachment, StagingContext) => Unit)
case class AttachmentDelete(attachments: Iterable[Attachment], deleteFiles: StagingContext => Unit)