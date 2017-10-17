package equellatests.tests

import java.util.UUID

import equellatests._
import equellatests.domain._
import equellatests.instgen.fiveo._
import equellatests.models.wizardfileupload._
import equellatests.pages.viewitem.SummaryPage
import equellatests.pages.wizard._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.openqa.selenium.support.ui.ExpectedConditions
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop, Test}


object WizardFileUploadsProperties extends StatefulProperties("Wizard file uploads") with LogonTestCase {
  type State = FileUploadState
  type Command = FileUploadCommand

  val testCaseDecoder = Decoder.apply
  val testCaseEncoder = Encoder.apply

  type AttachmentEditGen = (Attachment, FileUniversalControl) => Option[Gen[AttachmentEdit]]

  val wizards = Seq("Navigation and Attachments",
    "Attachment mimetype restriction collection")

  val ctrlsForWizard: Map[String, Seq[FileUniversalControl]] = Map(
    "Navigation and Attachments" -> Seq(
      FileUniversalControl(2, canRestrict = true, canSuppress = false, defaultSuppressed = false),
      FileUniversalControl(3, canRestrict = true, canSuppress = false, defaultSuppressed = false, maximumAttachments = 1)
    ),
    "Attachment mimetype restriction collection" -> Seq(
      FileUniversalControl(2, canRestrict = true, canSuppress = false, defaultSuppressed = false, maximumAttachments = 1,
        mimeTypes = Some(Set("image/jpeg")))
    ))

  def ctrlsThatCanAdd(item: Item) =
    ctrlsForWizard(item.wizard).filter(fuc => item.attachments.count(_.control == fuc) < fuc.maximumAttachments)

  def canAddMore(item: Item): Boolean = ctrlsThatCanAdd(item).nonEmpty

  def testFileGen(invalid: Boolean, f: FileUniversalControl => TestFile => Boolean)(item: Item): Gen[(FileUniversalControl, TestFile, Boolean)] = for {
    fuc <- Gen.oneOf(ctrlsThatCanAdd(item))
    tf <- Gen.oneOf(TestFile.testFiles.filter(f(fuc)))
  } yield (fuc, tf, invalid)

  val anyValidAttachment = testFileGen(false, fuc => tf => !fuc.illegalReason.isDefinedAt(tf)) _

  def genAllCommands(finished: FileUploadState => Boolean,
                     fileGen: Item => Gen[(FileUniversalControl, TestFile, Boolean)],
                     canAddNewAttachment: Item => Boolean,
                     edits: AttachmentEditGen*): Gen[Seq[FileUploadCommand]] = {

    def editForAttachment(a: Attachment, uc: FileUniversalControl): Gen[AttachmentEdit] =
      Gen.oneOf(edits.flatMap(_.apply(a, uc))).flatMap(identity)

    def genCommand(fileCount: Int, state: FileUploadState): Gen[FileUploadCommand] = state.currentPage match {
      case LoginPageLoc =>
        for {
          name <- arbitrary[UniqueRandomWord]
          wizard <- Gen.oneOf(wizards)
        } yield CreateItem(name.word, wizard)

      case SummaryPageLoc => EditItem
      case Page1(item) if item.attachments.size < fileCount && canAddNewAttachment(item) => for {
        (fuc, tf, failure) <- fileGen(item)
        vfn <- arbitrary[ValidFilename]
        actualFilename = s"${vfn.filename}.${tf.extension}"
      } yield UploadInlineFile(tf, actualFilename, fuc, UUID.randomUUID(), failure)

      case Page1(item) if item.attachments.forall(a => state.edited(a.id)) &&
        item.attachments.forall(a => state.verified(a.id)) => SaveItem

      case Page1(item) =>
        Gen.oneOf(item.attachments.filter(a => !state.edited(a.id) || !state.verified(a.id))).map(a => StartEditingAttachment(a.id))

      case AttachmentDetailsPage(a, _, _, _) if state.edited(a.id) => Gen.oneOf(CloseEditDialog, SaveAttachment)
      case AttachmentDetailsPage(a, edited, uc, _) => editForAttachment(edited, uc).map(ae => EditAttachmentDetails(ae))
    }

    for {
      numAttach <- Gen.choose(1, 3)
      cl <- generateCommands(s => if (finished(s)) Gen.const(List()) else genCommand(numAttach, s).map(List(_)))
    } yield cl
  }

  val editDescription: AttachmentEditGen = (a, uc) =>
    Some(arbitrary[ValidDescription].map(vd => DescriptionEdit(vd.desc)))

  val editViewer: AttachmentEditGen = (a, uc) =>
    a.viewerOptions.map(ops => Gen.oneOf(ops.toSeq :+ "").map(v => ViewerEdit(Some(v).filterNot(_.isEmpty))))

  val editRestriction: AttachmentEditGen = (a, uc) => if (uc.canRestrict) Some {
    Gen.const(RestrictionEdit(!a.restricted))
  } else None

  val editThumbSetting: AttachmentEditGen = (a, uc) => if (uc.canSuppress) Some {
    Gen.const(ThumbSettingEdit(!a.suppressThumb))
  } else None


  override def overrideParameters(p: Test.Parameters): Test.Parameters = p.withMinSize(5).withMaxSize(10)

  def savedAndVerified(s: FileUploadState): Boolean = (s.savedItem, s.currentPage) match {
    case (Some(i), Page1(item)) if item.attachments.map(_.id).forall(s.verified) &&
      item.attachments.map(_.id).forall(s.edited) => true
    case _ => false
  }

  statefulProp("edit description") {
    genAllCommands(savedAndVerified, anyValidAttachment, canAddMore, editDescription)
  }

  statefulProp("edit viewer") {
    genAllCommands(savedAndVerified, testFileGen(false,
      fuc => tf => StandardMimeTypes.viewersForFile(tf).size > 1 && !fuc.illegalReason.isDefinedAt(tf)), canAddMore, editViewer)
  }

  statefulProp("edit restriction") {
    genAllCommands(savedAndVerified, anyValidAttachment, canAddMore, editRestriction)
  }

  //  property("edit thumb setting") = statefulProp(genAllCommands(false, editThumbSetting))

  statefulProp("test invalid uploads") {
    genAllCommands(s => s.failedUploadAttempts > 10, testFileGen(true,
      fuc => tf => fuc.illegalReason.isDefinedAt(tf)), canAddMore)
  }

  override def initialState = FileUploadState(None, Set.empty, Set.empty, LoginPageLoc)

  override def runCommand(c: FileUploadCommand, s: FileUploadState) = {
    def withCurrentPage(pf: PartialFunction[Location, FileUploadState]): FileUploadState = {
      pf.apply(s.currentPage)
    }

    c match {
      case EditItem => s.savedItem.map(item => s.copy(currentPage = Page1(item))).getOrElse(s)
      case CreateItem(name, wizard) => s.copy(currentPage = Page1(Item(None, name, wizard, Seq.empty)))

      case UploadInlineFile(tf, filename, control, id, failed) => withCurrentPage {
        case p1@Page1(item)  =>
          if (!failed) {
            val desc = tf.packageName.getOrElse(filename)
            val attach = Attachment(id, control, filename, desc, restricted = false, suppressThumb = control.defaultSuppressed, tf)
            s.copy(currentPage = p1.copy(editingItem = item.addAttachment(attach)))
          } else s.copy(failedUploadAttempts = s.failedUploadAttempts+1)
      }
      case StartEditingAttachment(attachUuid) => withCurrentPage {
        case p1@Page1(item) => val a = item.attachmentForId(attachUuid)
          s.copy(currentPage = AttachmentDetailsPage(a, a, a.control, p1), verified = s.verified + a.id)
      }
      case CloseEditDialog => withCurrentPage {
        case AttachmentDetailsPage(_, a, _, page1) => s.copy(currentPage = page1)
      }
      case EditAttachmentDetails(changes) => withCurrentPage {
        case adp: AttachmentDetailsPage => s.copy(currentPage = adp.copy(edited = changes.edit(adp.edited)),
          edited = s.edited + adp.edited.id, verified = s.verified - adp.edited.id)
      }
      case SaveAttachment => withCurrentPage {
        case AttachmentDetailsPage(_, a, _, page1) => s.copy(currentPage = page1.copy(page1.editingItem.updateAttachment(a)))
      }
      case SaveItem => withCurrentPage {
        case Page1(item) => s.copy(savedItem = Some(item), currentPage = SummaryPageLoc, verified = Set.empty, edited = Set.empty)
      }
    }
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

  override def runCommandInBrowser(c: FileUploadCommand, s: FileUploadState, b: SimpleSeleniumBrowser) = c match {
    case EditItem => b.runOnPage { case sp: SummaryPage => sp.edit() }
    case CreateItem(name, wizard) => b.run {
      new ContributePage(b.page.ctx).load().openWizard(wizard)
    }
    case UploadInlineFile(tf, filename, control, id, failed) => b.runOnPage {
      case page1: WizardPageTab =>
        val ctrl = page1.universalControl(control.num)
        val expectedDescription = tf.packageName.getOrElse(filename)
        val failure = control.illegalReason.lift(tf).map(_ (filename))
        val w = ExpectedConditions.and(ctrl.updatedExpectation(), failure.map(ctrl.errorExpectation)
          .getOrElse(ctrl.attachNameWaiter(expectedDescription, false)))
        ctrl.uploadInline(tf, filename, w)
        page1
    }
    case StartEditingAttachment(attachUuid) => b.verifyOnPageAndState(s.currentPage) {
      case (Page1(item), page1: WizardPageTab) =>
        val a = item.attachmentForId(attachUuid)
        val uc = page1.universalControl(a.control.num)
        val waitPage = if (a.ispackage) new PackageAttachmentEditPage(uc) else new FileAttachmentEditPage(uc)
        uc.editResource(a.nameInTable, waitPage.pageExpectation).map { edPage =>
          edPage -> compareDetails(AttachmentDetailsPage.collectDetails(edPage), a)
        }.getOrElse {
          page1 -> Prop.falsified.label(s"No attachment in table called '${a.nameInTable}'")
        }
    }
    case CloseEditDialog => b.runOnPage { case fae: AttachmentEditPage => fae.close() }
    case EditAttachmentDetails(changes) => b.runOnPage { case aep: AttachmentEditPage => changes.run(aep); aep }
    case SaveAttachment => b.runOnPage { case aep: AttachmentEditPage => aep.save() }
    case SaveItem => b.runOnPage { case page1: WizardPageTab => page1.save().publish() }
  }

  override def logon = autoTestLogon
}
