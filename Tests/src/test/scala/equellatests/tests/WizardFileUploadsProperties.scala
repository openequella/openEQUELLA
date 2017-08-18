package equellatests.tests

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID

import org.scalacheck.Gen.Parameters
import org.scalacheck.{Gen, Prop, Properties, Test}
import Prop._
import com.tle.webtests.framework.{PageContext, TestConfig}
import equellatests._
import equellatests.domain._
import equellatests.pages.{HomePage, LoginPage}
import equellatests.pages.wizard._
import equellatests.tests.WizardFileUploadsProperties.{collectDetails, compareDetails}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.parser._
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalacheck.Arbitrary.arbitrary
import equellatests.instgen.fiveo._

case class FileUploadControl(logon: TestLogon, items: Seq[Item], currentPage: Location)

case class StateAndCommands(initialState: FileUploadControl, commands: List[FileUploadCommand]) extends TestCase with LogonTestCase {
  type State = FileUploadControl
  type Browser = SimpleSeleniumBrowser
  def createInital = SimpleSeleniumBrowser
  def logon = initialState.logon
}

object StateAndCommands {
  implicit val sacEncoder: Encoder[StateAndCommands] = deriveEncoder
  implicit val sacDecoder: Decoder[StateAndCommands] = deriveDecoder
}

sealed trait FileUploadCommand extends Command {
  type State = FileUploadControl
  type Browser = SimpleSeleniumBrowser
}

object FileUploadCommand {
  implicit val fileUpEncoder: Encoder[FileUploadCommand] = deriveEncoder
  implicit val fileUpDecoder: Decoder[FileUploadCommand] = deriveDecoder
}

case class CreateItem(name: String) extends UnitCommand with FileUploadCommand {
  def postCondition(state: FileUploadControl, success: Boolean): Prop = success

  def run(sut: Browser, state: FileUploadControl): Unit = {
    sut.page = new ContributePage(sut.page.ctx).load().openWizard("Navigation and Attachments")
  }

  def nextState(state: FileUploadControl): FileUploadControl = state.copy(currentPage = Page1(Item(None, name, Seq.empty)))
}

case class UploadInlineFile(tf: TestFile, control: FileUniversalControl, id: UUID) extends UnitCommand with FileUploadCommand {
  def run(sut: Browser, state: FileUploadControl): Unit = {
    sut.page match {
      case page1: WizardPageTab =>
        val ctrl = page1.universalControl(control.num)
        val expectedDescription = tf.packageName.getOrElse(tf.filename)
        val w = ExpectedConditions.and(ctrl.updatedExpectation(), ctrl.attachNameWaiter(expectedDescription, false))
        ctrl.uploadInline(tf, w)
    }
  }

  def nextState(state: FileUploadControl): FileUploadControl = state.currentPage match {
    case Page1(item) =>
      val desc = tf.packageName.getOrElse(tf.filename)
      val attach = Attachment(id, control, desc, restricted = false, suppressThumb = control.defaultSuppressed, tf)
      state.copy(currentPage = Page1(item.addAttachment(attach)))
  }

}

case class StartEditingAttachment(attachUuid: UUID) extends VerifyCommand with FileUploadCommand {
  type BrowserResult = Option[AttachmentDetails]

  def currentAttachment(state: FileUploadControl) : Attachment = state.currentPage match {
    case Page1(item) => item.attachmentForId(attachUuid)
  }

  def run(sut: Browser, state: FileUploadControl): Option[AttachmentDetails] = {
    sut.page match {
      case page1: WizardPageTab =>
        val a = currentAttachment(state)
        val uc = page1.universalControl(a.control.num)
        val waitPage = if (a.ispackage) new PackageAttachmentEditPage(uc) else new FileAttachmentEditPage(uc)
        uc.editResource(a.nameInTable, waitPage.pageExpectation).map { edPage =>
          sut.page = edPage
          collectDetails(edPage)
        }
    }
  }

  def nextState(state: FileUploadControl): FileUploadControl = state.currentPage match {
    case Page1(item) => val a = item.attachmentForId(attachUuid)
      state.copy(currentPage = AttachmentDetailsPage(a, a, a.control, Page1(item)))
  }

  def postCondition(state: FileUploadControl, result: Option[AttachmentDetails]): Prop = {
    val a = currentAttachment(state)
    result.map(compareDetails(_, a)).getOrElse(Prop.falsified.label(s"No attachment in table called '${a.nameInTable}'"))
      .label(s"attachment:${a.toString}")
  }

}

case object CloseEditDialog extends UnitCommand with FileUploadCommand {
  def postCondition(state: FileUploadControl, success: Boolean): Prop = success

  def run(sut: Browser, state: FileUploadControl): Unit = {
    val np = sut.page match {
      case fae: AttachmentEditPage => fae.close()
    }
    sut.page = np
  }

  def nextState(state: FileUploadControl): FileUploadControl = state.currentPage match {
    case AttachmentDetailsPage(_, _, _, page1) => state.copy(currentPage = page1)
  }

  def preCondition(state: FileUploadControl): Boolean = state.currentPage match {
    case aep: AttachmentDetailsPage => true
    case _ => false
  }
}

case class EditAttachmentDetails(changes: AttachmentEdit) extends UnitCommand with FileUploadCommand {

  def nextState(state: FileUploadControl): FileUploadControl = state.currentPage match {
    case adp: AttachmentDetailsPage => state.copy(currentPage = adp.copy(edited = changes.edit(adp.edited)))
  }

  def run(sut: Browser, state: FileUploadControl): Unit = {
    sut.page match {
      case aep: AttachmentEditPage => changes.run(aep)
    }
  }
}

case object SaveAttachment extends UnitCommand with FileUploadCommand {
  def run(sut: Browser, state: FileUploadControl): Unit = sut.page = sut.page match {
    case aep: AttachmentEditPage => aep.save()
  }

  def nextState(state: FileUploadControl): FileUploadControl = state.currentPage match {
    case AttachmentDetailsPage(_, a, _, page1) => state.copy(currentPage = page1.copy(page1.editingItem.updateAttachment(a)))
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
}

case class FileUniversalControl(num: Int, canRestrict: Boolean, canSuppress: Boolean, defaultSuppressed: Boolean, noUnzip: Boolean = false,
                                maximumAttachments: Int = Int.MaxValue)


object FileUniversalControl {
  implicit val ucEncoder: Encoder[FileUniversalControl] = deriveEncoder
  implicit val ucDecoder: Decoder[FileUniversalControl] = deriveDecoder
}

case class Attachment(id: UUID, control: FileUniversalControl, description: String, restricted: Boolean, suppressThumb: Boolean, file: TestFile,
                      viewer: Option[String] = None) {
  def nameInTable: String = if (restricted) description + " (hidden from summary view)" else description

  lazy val details = StandardMimeTypes.commonDetailsForFile(file, description)

  def ispackage = file.ispackage

  def viewerOptions: Option[Set[String]] = Option(StandardMimeTypes.viewersForFile(file)).filter(_.size > 1)
}

object Attachment {
  implicit val attachEncoder: Encoder[Attachment] = deriveEncoder
  implicit val attachDecoder: Decoder[Attachment] = deriveDecoder
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
  def edit: (Attachment) => Attachment = _.copy(description = nd)

  def run: (AttachmentEditPage) => Unit = _.description = nd

  def label: String = s"Editing description to $nd"
}

case class RestrictionEdit(nr: Boolean) extends AttachmentEdit {
  def edit: (Attachment) => Attachment = _.copy(restricted = nr)

  def run: (AttachmentEditPage) => Unit = _.restricted = nr

  def label: String = s"Changing restricted to $nr"
}

case class ThumbSettingEdit(ns: Boolean) extends AttachmentEdit {
  def edit: (Attachment) => Attachment = _.copy(suppressThumb = ns)

  def run: (AttachmentEditPage) => Unit = _.suppressThumb = ns

  def label: String = s"Changing suppressThumb to $ns"
}

case class ViewerEdit(viewer: Option[String]) extends AttachmentEdit
{
  def edit = _.copy(viewer = viewer)
  def run = _.viewer = viewer
  def label = s"Changing viewer to $viewer"
}

case class Item(itemId: Option[ItemId], name: String, attachments: Seq[Attachment]) {
  def attachmentForId(attachUuid: UUID): Attachment = attachments.find(_.id == attachUuid).get

  def addAttachment(a: Attachment): Item = copy(attachments = attachments :+ a)

  def updateAttachment(a: Attachment): Item = copy(attachments = attachments.updated(attachments.indexWhere(_.id == a.id), a))
}

object Item {
  implicit val itemEncoder: Encoder[Item] = deriveEncoder
  implicit val itemDecoder: Decoder[Item] = deriveDecoder
}

object FileUploadControl {
  implicit val fucEncoder: Encoder[FileUploadControl] = deriveEncoder
  implicit val fucDecoder: Decoder[FileUploadControl] = deriveDecoder
}


object WizardFileUploadsProperties extends StatefulProperties("Wizard file uploads") {
  type TC = StateAndCommands

  val testCaseDecoder = Decoder.apply
  val testCaseEncoder = Encoder.apply

  def genAllCommands: Gen[StateAndCommands] = {
    def genNext(left: Int, s: FileUploadControl, curList: List[FileUploadCommand]): Gen[List[FileUploadCommand]] = {
      if (left == 0) curList else genCommand(s).flatMap(com => genNext(left - 1, com.nextState(s), com :: curList))
    }

    for {
      is <- genInitialState
      commandsLeft <- Gen.size
      cl <- genNext(commandsLeft, is, Nil)
    } yield StateAndCommands(is, cl.reverse)
  }

  val allTypesControl = Seq(
    FileUniversalControl(2, canRestrict = true, canSuppress = false, defaultSuppressed = false),
    FileUniversalControl(3, canRestrict = true, canSuppress = false, defaultSuppressed = false, maximumAttachments = 1)
  )

  def editForAttachment(a: Attachment, uc: FileUniversalControl): Gen[AttachmentEdit] = Gen.oneOf(Seq(
    Some(arbitrary[ValidDescription].map(vd => DescriptionEdit(vd.desc))),
    a.viewerOptions.map(ops => Gen.oneOf(ops.toSeq :+ "").map(v => ViewerEdit(Some(v).filterNot(_.isEmpty)))),
    if (uc.canRestrict) Some {
      Gen.const(RestrictionEdit(!a.restricted))
    } else None,
    if (uc.canSuppress) Some {
      Gen.const(ThumbSettingEdit(!a.suppressThumb))
    } else None
  ).flatten).flatMap(identity)


  def genInitialState: Gen[FileUploadControl] = for {
    tl <- arbitrary[TestLogon]
  } yield FileUploadControl(tl, Seq.empty, LoginPageLoc)

  def genCommand(state: FileUploadControl): Gen[FileUploadCommand] = {
    state.currentPage match {
      case LoginPageLoc =>
        for {
          name <- arbitrary[UniqueRandomWord]
        } yield CreateItem(name.word)
      case p1@Page1(item) =>

        val newAttachment = for {
          tf <- arbitrary[TestFile].suchThat(tf => !TestFile.bannedExt(tf.extension))
          fuc <- Gen.oneOf(allTypesControl.filter(fuc => item.attachments.count(_.control == fuc) < fuc.maximumAttachments))
        } yield UploadInlineFile(tf, fuc, UUID.randomUUID())

        if (item.attachments.isEmpty) newAttachment else {
          Gen.frequency(1 -> newAttachment, 5 -> Gen.oneOf(item.attachments).map(a => StartEditingAttachment(a.id)))
        }
      case adp@AttachmentDetailsPage(a, edited, uc, page1) => if (a != edited) Gen.oneOf(CloseEditDialog, SaveAttachment) else {
        editForAttachment(edited, uc).map(ae => EditAttachmentDetails(ae))
      }
    }
  }


  def collectDetails(a: AttachmentEditPage): AttachmentDetails =
    AttachmentDetails(a.description, a.restricted, a.suppressThumb, a.details().toSet, a.viewerOptions)

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


  override def overrideParameters(p: Test.Parameters): Test.Parameters = p.withMinSize(5).withMaxSize(10)

  property("no restrictions") = statefulProp(genAllCommands)


}
