package com.tle.web.controls.universal.handlers.fileupload.details

import com.tle.beans.item.attachments.Attachment
import com.tle.common.Check
import com.tle.common.filesystem.FileEntry
import com.tle.core.services.ZipProgress
import com.tle.web.controls.universal.handlers.fileupload.{AttachmentDelete, WebFileUploads}
import com.tle.web.controls.universal.{ControlContext, DialogRenderOptions, StagingContext}
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.events.RenderContext
import com.tle.web.sections.render.{Label, SectionRenderable}
import com.tle.web.sections.standard.TextField
import com.tle.web.viewurl.ViewableResource

trait ZipHandler {
  def zipProgress: Option[ZipProgress]

  def selectedAttachments: Map[String, Attachment]

  def unzip: ZipProgress

  def removeUnzipped(): Unit

  def unzipped: Boolean

  def unzippedEntries: Seq[FileEntry]
}

trait ViewerHandler {
  def viewableResource(info: SectionInfo): ViewableResource

  def viewerListModel: ViewersListModel
}

trait EditingHandler {
  def editingArea: String

  def syncEdits(filename: String): Unit
}

object DetailsPage {
  val LABEL_ERROR_BLANK = WebFileUploads.label("handlers.abstract.error.blank")
}

import DetailsPage._

trait DetailsPage {

  def editingAttachment: SectionInfo => Attachment

  def previewable: Boolean

  def renderDetails(context: RenderContext): (SectionRenderable, DialogRenderOptions => Unit)

  def prepareUI(info: SectionInfo): Unit

  def editAttachment(info: SectionInfo, a: Attachment, ctx: ControlContext): (Attachment, Option[AttachmentDelete])

  def validate(info: SectionInfo): Boolean

  def displayName: TextField

  def validateDisplayName(info: SectionInfo): Option[(String, Label)] = {
    if (Check.isEmpty(displayName.getValue(info))) {
      Some("displayName" -> LABEL_ERROR_BLANK)
    } else None
  }
}

