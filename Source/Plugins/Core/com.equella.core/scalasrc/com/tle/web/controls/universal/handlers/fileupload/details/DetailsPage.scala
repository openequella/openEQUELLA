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

