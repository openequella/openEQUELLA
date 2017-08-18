package equellatests.tests

import java.util.UUID

import equellatests._
import equellatests.domain._
import equellatests.instgen.fiveo._
import equellatests.pages.wizard._
import equellatests.tests.WizardFileUploadsProperties.{collectDetails, compareDetails}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop, Test}

case class FileUploadControl(logon: TestLogon, items: Seq[Item], currentPage: Location, testFailures: Boolean)

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

case class CreateItem(name: String, wizard: String) extends UnitCommand with FileUploadCommand {
  def postCondition(state: FileUploadControl, success: Boolean): Prop = success

  def run(sut: Browser, state: FileUploadControl): Unit = {
    sut.page = new ContributePage(sut.page.ctx).load().openWizard(wizard)

  }

  def nextState(state: FileUploadControl): FileUploadControl = state.copy(currentPage = Page1(Item(None, name, Seq.empty),
    wizard match {
      case "Navigation and Attachments" => Seq(
        FileUniversalControl(2, canRestrict = true, canSuppress = false, defaultSuppressed = false),
        FileUniversalControl(3, canRestrict = true, canSuppress = false, defaultSuppressed = false, maximumAttachments = 1)
      )
      case "Attachment mimetype restriction collection" => Seq(
        FileUniversalControl(2, canRestrict = true, canSuppress = false, defaultSuppressed = false, maximumAttachments = 1,
          mimeTypes = Some(Set("image/jpeg")))
      )
    }
  ))
}

case class UploadInlineFile(tf: TestFile, filename: String, control: FileUniversalControl, id: UUID, failed: Boolean) extends UnitCommand with FileUploadCommand {
  def run(sut: Browser, state: FileUploadControl): Unit = {
    sut.page match {
      case page1: WizardPageTab =>
        val ctrl = page1.universalControl(control.num)
        val expectedDescription = tf.packageName.getOrElse(filename)
        val failure = control.illegalReason.lift(tf).map(_(filename))
        val w = ExpectedConditions.and(ctrl.updatedExpectation(), failure.map(ctrl.errorExpectation)
          .getOrElse(ctrl.attachNameWaiter(expectedDescription, false)))
        ctrl.uploadInline(tf, filename, w)
    }
  }

  def nextState(state: FileUploadControl): FileUploadControl = state.currentPage match {
      case p1@Page1(item, _) if !failed =>
        val desc = tf.packageName.getOrElse(filename)
        val attach = Attachment(id, control, filename, desc, restricted = false, suppressThumb = control.defaultSuppressed, tf)
        state.copy(currentPage = p1.copy(editingItem = item.addAttachment(attach)))
      case _ => state
    }

}

case class StartEditingAttachment(attachUuid: UUID) extends VerifyCommand with FileUploadCommand {
  type BrowserResult = Option[AttachmentDetails]

  def currentAttachment(state: FileUploadControl): Attachment = state.currentPage match {
    case Page1(item, _) => item.attachmentForId(attachUuid)
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
    case p1@Page1(item, _) => val a = item.attachmentForId(attachUuid)
      state.copy(currentPage = AttachmentDetailsPage(a, a, a.control, p1))
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

case class Page1(editingItem: Item, controls: Seq[FileUniversalControl]) extends Location

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
                                maximumAttachments: Int = Int.MaxValue, mimeTypes: Option[Set[String]] = None) {

  def testFileGen(invalid: Boolean): Gen[TestFile] = Gen.oneOf(TestFile.testFiles.filter(tf => illegalReason.isDefinedAt(tf) == invalid))

  def illegalReason : PartialFunction[TestFile, String => String] = {
    case tf if TestFile.bannedExt(tf.extension) => fn => s"$fn: File upload cancelled.  File extension has been banned"
    case tf if mimeTypes.fold(false)(!_.apply(StandardMimeTypes.extMimeMapping(tf.extension))) => fn => s"""This control is restricted to certain file types. "$fn" is not allowed to be uploaded."""
  }

}


object FileUniversalControl {
  implicit val ucEncoder: Encoder[FileUniversalControl] = deriveEncoder
  implicit val ucDecoder: Decoder[FileUniversalControl] = deriveDecoder
}

case class Attachment(id: UUID, control: FileUniversalControl, filename: String, description: String, restricted: Boolean,
                      suppressThumb: Boolean, file: TestFile, viewer: Option[String] = None) {
  def nameInTable: String = if (restricted) description + " (hidden from summary view)" else description

  lazy val details = StandardMimeTypes.commonDetailsForFile(file, filename, description)

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

case class ViewerEdit(viewer: Option[String]) extends AttachmentEdit {
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

  val wizards = Seq("Navigation and Attachments",
    "Attachment mimetype restriction collection")

}


object WizardFileUploadsProperties extends StatefulProperties("Wizard file uploads") {
  type TC = StateAndCommands

  val testCaseDecoder = Decoder.apply
  val testCaseEncoder = Encoder.apply

  def genAllCommands(testFailures: Boolean): Gen[StateAndCommands] = {
    def genNext(left: Int, s: FileUploadControl, curList: List[FileUploadCommand]): Gen[List[FileUploadCommand]] = {
      if (left == 0) curList else genCommand(s).flatMap(com => genNext(left - 1, com.nextState(s), com :: curList))
    }

    for {
      is <- genInitialState(testFailures)
      commandsLeft <- Gen.size
      cl <- genNext(commandsLeft, is, Nil)
    } yield StateAndCommands(is, cl.reverse)
  }

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


  def genInitialState(testFailures: Boolean): Gen[FileUploadControl] = for {
    tl <- arbitrary[TestLogon]
  } yield FileUploadControl(tl, Seq.empty, LoginPageLoc, testFailures)

  def genCommand(state: FileUploadControl): Gen[FileUploadCommand] = {
    state.currentPage match {
      case LoginPageLoc =>
        for {
          name <- arbitrary[UniqueRandomWord]
          wizard <- Gen.oneOf(FileUploadControl.wizards)
        } yield CreateItem(name.word, wizard)
      case p1@Page1(item, ctrls) =>

        val addableCtrls = ctrls.filter(fuc => item.attachments.count(_.control == fuc) < fuc.maximumAttachments)

        def newAttachment = for {
          fuc <- Gen.oneOf(addableCtrls)
          tf <- fuc.testFileGen(state.testFailures)
          vfn <- arbitrary[ValidFilename]
          actualFilename = s"${vfn.filename}.${tf.extension}"
        } yield UploadInlineFile(tf, actualFilename, fuc, UUID.randomUUID(), state.testFailures)

        def editAttachment = Gen.oneOf(item.attachments).map(a => StartEditingAttachment(a.id))
        if (item.attachments.isEmpty) newAttachment else
        if (addableCtrls.isEmpty) editAttachment else {
          Gen.frequency(1 -> newAttachment, 5 -> editAttachment)
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

  property("test valid uploads") = statefulProp(genAllCommands(false))

  property("test invalid uploads") = statefulProp(genAllCommands(true))


}
