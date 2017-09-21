package equellatests.models.wizardfileupload

import java.util.UUID

import equellatests._
import equellatests.domain.{ItemId, StandardMimeTypes, TestFile, TestLogon}
import equellatests.pages.wizard._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop}

case class FileUniversalControl(num: Int, canRestrict: Boolean, canSuppress: Boolean, defaultSuppressed: Boolean, noUnzip: Boolean = false,
                                maximumAttachments: Int = Int.MaxValue, mimeTypes: Option[Set[String]] = None) {

  def illegalReason: PartialFunction[TestFile, String => String] = {
    case tf if TestFile.bannedExt(tf.extension) => fn => s"$fn: File upload cancelled.  File extension has been banned"
    case tf if mimeTypes.fold(false)(!_.apply(StandardMimeTypes.extMimeMapping(tf.extension))) => fn => s"""This control is restricted to certain file types. "$fn" is not allowed to be uploaded."""
  }

}


object FileUniversalControl {
  implicit val ucEncoder: Encoder[FileUniversalControl] = deriveEncoder
  implicit val ucDecoder: Decoder[FileUniversalControl] = deriveDecoder
}

case class Attachment(id: UUID, control: FileUniversalControl, filename: String, description: String, restricted: Boolean,
                      suppressThumb: Boolean, file: TestFile, viewer: Option[String] = None, verified: Boolean = false) {
  def nameInTable: String = if (restricted) description + " (hidden from summary view)" else description

  lazy val details = StandardMimeTypes.commonDetailsForFile(file, filename, description)

  def ispackage = file.ispackage

  def viewerOptions: Option[Set[String]] = Option(StandardMimeTypes.viewersForFile(file)).filter(_.size > 1)
}

object Attachment {
  implicit val attachEncoder: Encoder[Attachment] = deriveEncoder
  implicit val attachDecoder: Decoder[Attachment] = deriveDecoder
}

case class Item(itemId: Option[ItemId], name: String, wizard: String, attachments: Seq[Attachment]) {
  def attachmentForId(attachUuid: UUID): Attachment = attachments.find(_.id == attachUuid).get

  def addAttachment(a: Attachment): Item = copy(attachments = attachments :+ a)

  def updateAttachment(a: Attachment): Item = copy(attachments = attachments.updated(attachments.indexWhere(_.id == a.id), a))
}

object Item {
  implicit val itemEncoder: Encoder[Item] = deriveEncoder
  implicit val itemDecoder: Decoder[Item] = deriveDecoder
}

case class TestState(logon: TestLogon, savedItem: Option[Item], currentPage: Location)

object TestState {
  implicit val fucEncoder: Encoder[TestState] = deriveEncoder
  implicit val fucDecoder: Decoder[TestState] = deriveDecoder

  val wizards = Seq("Navigation and Attachments",
    "Attachment mimetype restriction collection")

}

case class FileUploadTestCase(initialState: TestState, commands: List[FileUploadCommand]) extends TestCase with LogonTestCase {
  type State = TestState
  type Browser = SimpleSeleniumBrowser

  def createInital = SimpleSeleniumBrowser

  def logon = initialState.logon
}

object FileUploadTestCase {
  implicit val sacEncoder: Encoder[FileUploadTestCase] = deriveEncoder
  implicit val sacDecoder: Decoder[FileUploadTestCase] = deriveDecoder
}

sealed trait FileUploadCommand extends Command {
  type State = TestState
  type Browser = SimpleSeleniumBrowser
}

object FileUploadCommand {
  implicit val fileUpEncoder: Encoder[FileUploadCommand] = deriveEncoder
  implicit val fileUpDecoder: Decoder[FileUploadCommand] = deriveDecoder
}

case class CreateItem(name: String, wizard: String) extends UnitCommand with FileUploadCommand {

  def run(sut: Browser, state: TestState): Unit =
    sut.page = new ContributePage(sut.page.ctx).load().openWizard(wizard)

  def nextState(state: TestState): TestState = state.copy(currentPage = Page1(Item(None, name, wizard, Seq.empty)))
}

case class UploadInlineFile(tf: TestFile, filename: String, control: FileUniversalControl, id: UUID, failed: Boolean) extends UnitCommand with FileUploadCommand {
  def run(sut: Browser, state: TestState): Unit = {
    sut.page match {
      case page1: WizardPageTab =>
        val ctrl = page1.universalControl(control.num)
        val expectedDescription = tf.packageName.getOrElse(filename)
        val failure = control.illegalReason.lift(tf).map(_ (filename))
        val w = ExpectedConditions.and(ctrl.updatedExpectation(), failure.map(ctrl.errorExpectation)
          .getOrElse(ctrl.attachNameWaiter(expectedDescription, false)))
        ctrl.uploadInline(tf, filename, w)
    }
  }

  def nextState(state: TestState): TestState = state.currentPage match {
    case p1@Page1(item) if !failed =>
      val desc = tf.packageName.getOrElse(filename)
      val attach = Attachment(id, control, filename, desc, restricted = false, suppressThumb = control.defaultSuppressed, tf)
      state.copy(currentPage = p1.copy(editingItem = item.addAttachment(attach)))
    case _ => state
  }

}

case class StartEditingAttachment(attachUuid: UUID) extends VerifyCommand with FileUploadCommand {
  type BrowserResult = Option[AttachmentDetails]

  def currentAttachment(state: TestState): Attachment = state.currentPage match {
    case Page1(item) => item.attachmentForId(attachUuid)
  }

  def run(sut: Browser, state: TestState): Option[AttachmentDetails] = {
    sut.page match {
      case page1: WizardPageTab =>
        val a = currentAttachment(state)
        val uc = page1.universalControl(a.control.num)
        val waitPage = if (a.ispackage) new PackageAttachmentEditPage(uc) else new FileAttachmentEditPage(uc)
        uc.editResource(a.nameInTable, waitPage.pageExpectation).map { edPage =>
          sut.page = edPage
          AttachmentDetailsPage.collectDetails(edPage)
        }
    }
  }

  def nextState(state: TestState): TestState = state.currentPage match {
    case p1@Page1(item) => val a = item.attachmentForId(attachUuid)
      state.copy(currentPage = AttachmentDetailsPage(a, a, a.control, p1))
  }

  def postCondition(state: TestState, result: Option[AttachmentDetails]): Prop = {
    val a = currentAttachment(state)
    result.map(compareDetails(_, a)).getOrElse(Prop.falsified.label(s"No attachment in table called '${a.nameInTable}'"))
      .label(s"attachment:${a.toString}")
  }

  def compareDetails(ad: AttachmentDetails, attachment: Attachment): Prop = {
    val uc = attachment.control
    all(
      (ad.description ?= attachment.description) :| "description",
      if (uc.canRestrict) (ad.restricted ?= Some(attachment.restricted)) :| "restricted flag" else Prop(ad.restricted.isEmpty) :| "no restrict flag",
      (ad.details ?= attachment.details) :| "details",
      (ad.viewers ?= attachment.viewerOptions) :| "viewers",
      if (uc.canSuppress && !attachment.ispackage) (ad.suppressThumb ?= Some(attachment.suppressThumb)) :| "thumb supression flag" else Prop(ad.suppressThumb.isEmpty) :| "no suppress flag"
    )
  }

}

case object CloseEditDialog extends UnitCommand with FileUploadCommand {
  def run(sut: Browser, state: TestState): Unit = {
    val np = sut.page match {
      case fae: AttachmentEditPage => fae.close()
    }
    sut.page = np
  }

  def nextState(state: TestState): TestState = state.currentPage match {
    case AttachmentDetailsPage(_, _, _, page1) => state.copy(currentPage = page1)
  }
}

case class EditAttachmentDetails(changes: AttachmentEdit) extends UnitCommand with FileUploadCommand {

  def nextState(state: TestState): TestState = state.currentPage match {
    case adp: AttachmentDetailsPage => state.copy(currentPage = adp.copy(edited = changes.edit(adp.edited)))
  }

  def run(sut: Browser, state: TestState): Unit = {
    sut.page match {
      case aep: AttachmentEditPage => changes.run(aep)
    }
  }
}

case object SaveAttachment extends UnitCommand with FileUploadCommand {
  def run(sut: Browser, state: TestState): Unit = sut.page = sut.page match {
    case aep: AttachmentEditPage => aep.save()
  }

  def nextState(state: TestState): TestState = state.currentPage match {
    case AttachmentDetailsPage(_, a, _, page1) => state.copy(currentPage = page1.copy(page1.editingItem.updateAttachment(a)))
  }
}

case object SaveItem extends UnitCommand with FileUploadCommand {
  override def run(b: SimpleSeleniumBrowser, state: TestState): Unit = b.page = b.page match {
    case page1: WizardPageTab =>
      page1.save()
  }

  override def nextState(state: TestState): TestState = state.currentPage match {
    case Page1(item) => state.copy(savedItem = Some(item))
  }
}

sealed trait Location

object Location {
  implicit val locationEnc: Encoder[Location] = deriveEncoder
  implicit val locationDec: Decoder[Location] = deriveDecoder
}

case object LoginPageLoc extends Location

case class Page1(editingItem: Item) extends Location

object Page1 {
  implicit val p1Enc: Encoder[Page1] = deriveEncoder
  implicit val p1Dec: Decoder[Page1] = deriveDecoder
}

case class AttachmentDetailsPage(original: Attachment, edited: Attachment, control: FileUniversalControl, page1: Page1) extends Location

object AttachmentDetailsPage {
  implicit val adpEncoder: Encoder[AttachmentDetailsPage] = deriveEncoder
  implicit val adpDecoder: Decoder[AttachmentDetailsPage] = deriveDecoder

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

object AttachmentEdit {
  implicit val aeEnc: Encoder[AttachmentEdit] = deriveEncoder
  implicit val aeDec: Decoder[AttachmentEdit] = deriveDecoder
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

