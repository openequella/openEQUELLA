package equellatests.models.wizardfileupload

import java.util.UUID

import equellatests.domain.{ItemId, StandardMimeTypes, TestFile}
import equellatests.pages.wizard._
import equellatests.sections.wizard.AttachmentEditPage

case class FileUniversalControl(num: Int, canRestrict: Boolean, canSuppress: Boolean, defaultSuppressed: Boolean, noUnzip: Boolean = false,
                                maximumAttachments: Int = Int.MaxValue, mimeTypes: Option[Set[String]] = None) {

  def illegalReason: PartialFunction[TestFile, String => String] = {
    case tf if mimeTypes.fold(false)(!_.apply(StandardMimeTypes.extMimeMapping(tf.extension))) => fn => s"""This control is restricted to certain file types. "$fn" is not allowed to be uploaded."""
    case tf if TestFile.bannedExt(tf.extension) => fn => s"$fn: File upload cancelled.  File extension has been banned"
  }

}

case class Attachment(id: UUID, control: FileUniversalControl, filename: String, description: String, restricted: Boolean,
                      suppressThumb: Boolean, file: TestFile, viewer: Option[String] = None) {
  def nameInTable: String = if (restricted) description + " (hidden from summary view)" else description

  lazy val details = StandardMimeTypes.commonDetailsForFile(file, filename, description)

  def ispackage = file.ispackage

  def viewerOptions: Option[Set[String]] = Option(StandardMimeTypes.viewersForFile(file)).filter(_.size > 1)
}

case class Item(itemId: Option[ItemId], name: String, wizard: String, attachments: Seq[Attachment]) {
  def attachmentForId(attachUuid: UUID): Attachment = attachments.find(_.id == attachUuid).get

  def addAttachment(a: Attachment): Item = copy(attachments = attachments :+ a)

  def updateAttachment(a: Attachment): Item = copy(attachments = attachments.updated(attachments.indexWhere(_.id == a.id), a))
}

case class FileUploadState(savedItem: Option[Item], verified: Set[UUID], edited: Set[UUID], currentPage: Location, failedUploadAttempts: Int = 0)


sealed trait FileUploadCommand

case object EditItem extends FileUploadCommand

case class CreateItem(name: String, wizard: String) extends FileUploadCommand

case class UploadInlineFile(tf: TestFile, filename: String, control: FileUniversalControl, id: UUID, failed: Boolean) extends FileUploadCommand


case class StartEditingAttachment(attachUuid: UUID) extends FileUploadCommand


case object CloseEditDialog extends FileUploadCommand

case class EditAttachmentDetails(changes: AttachmentEdit) extends FileUploadCommand

case object SaveAttachment extends FileUploadCommand

case object SaveItem extends FileUploadCommand


sealed trait Location

case object SummaryPageLoc extends Location

case object LoginPageLoc extends Location

case class Page1(editingItem: Item) extends Location

case class AttachmentDetailsPage(original: Attachment, edited: Attachment, control: FileUniversalControl, page1: Page1) extends Location

object AttachmentDetailsPage {
  def collectDetails(a: AttachmentEditPage): AttachmentDetails =
    AttachmentDetails(a.description, a.restricted, a.suppressThumb, a.details().toSet, a.viewerOptions)

}

case class AttachmentDetails(description: String, restricted: Option[Boolean], suppressThumb: Option[Boolean],
                             details: Set[(String, String)], viewers: Option[Set[String]])

sealed trait AttachmentEdit {
  def edit: Attachment => Attachment

  def run: AttachmentEditPage => Unit

  def label: String

  override def toString = label
}

case class DescriptionEdit(nd: String) extends AttachmentEdit {
  def edit = _.copy(description = nd)

  def run = _.description = nd

  def label = s"Editing description to $nd"
}

case class RestrictionEdit(nr: Boolean) extends AttachmentEdit {
  def edit = _.copy(restricted = nr)

  def run = _.restricted = nr

  def label = s"Changing restricted to $nr"
}

case class ThumbSettingEdit(ns: Boolean) extends AttachmentEdit {
  def edit = _.copy(suppressThumb = ns)

  def run = _.suppressThumb = ns

  def label = s"Changing suppressThumb to $ns"
}

case class ViewerEdit(viewer: Option[String]) extends AttachmentEdit {
  def edit = _.copy(viewer = viewer)

  def run = _.viewer = viewer

  def label = s"Changing viewer to $viewer"
}

