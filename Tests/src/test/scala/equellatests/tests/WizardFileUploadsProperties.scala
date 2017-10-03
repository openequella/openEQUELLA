package equellatests.tests

import java.util.UUID

import equellatests._
import equellatests.domain._
import equellatests.instgen.fiveo._
import equellatests.models.wizardfileupload._
import io.circe.{Decoder, Encoder}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Prop, Test}


object WizardFileUploadsProperties extends StatefulProperties("Wizard file uploads") {
  type TC = FileUploadTestCase

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

  def genAllCommands(finished: (TestState, List[FileUploadCommand]) => Boolean,
                     fileGen: Item => Gen[(FileUniversalControl, TestFile, Boolean)],
                     canAddNewAttachment: Item => Boolean,
                     edits: AttachmentEditGen*): Gen[FileUploadTestCase] = {

    def editForAttachment(a: Attachment, uc: FileUniversalControl): Gen[AttachmentEdit] =
      Gen.oneOf(edits.flatMap(_.apply(a, uc))).flatMap(identity)


    def genInitialState: Gen[TestState] = for {
      tl <- arbitrary[TestLogon]
      numAttach <- Gen.choose(1, 3)
    } yield TestState(tl, None, numAttach, Set.empty, Set.empty, LoginPageLoc)

    def genCommand(state: TestState): Gen[FileUploadCommand] = state.currentPage match {
      case LoginPageLoc =>
        for {
          name <- arbitrary[UniqueRandomWord]
          wizard <- Gen.oneOf(wizards)
        } yield CreateItem(name.word, wizard)

      case SummaryPageLoc => EditItem
      case Page1(item) if item.attachments.size < state.fileCount && canAddNewAttachment(item) => for {
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

    def genNext(s: TestState, curList: List[FileUploadCommand]): Gen[List[FileUploadCommand]] = {
      if (curList.size > 200) sys.error("Maximum commands reached:" + curList)
      if (finished(s, curList)) curList else
        genCommand(s).flatMap(com => genNext(com.nextState(s), com :: curList))
    }

    for {
      is <- genInitialState
      cl <- genNext(is, Nil)
    } yield FileUploadTestCase(is, cl.reverse)
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

  def savedAndVerified(s: TestState, cl: List[FileUploadCommand]): Boolean = (s.savedItem, s.currentPage) match {
    case (Some(i), Page1(item)) if item.attachments.map(_.id).forall(s.verified) &&
      item.attachments.map(_.id).forall(s.edited)=> true
    case _ => false
  }

  property("edit description") = statefulProp(genAllCommands(savedAndVerified, anyValidAttachment, canAddMore, editDescription))

  property("edit viewer") = statefulProp(genAllCommands(savedAndVerified, testFileGen(false,
    fuc => tf => StandardMimeTypes.viewersForFile(tf).size > 1 && !fuc.illegalReason.isDefinedAt(tf)), canAddMore, editViewer))

  property("edit restriction") = statefulProp(genAllCommands(savedAndVerified, anyValidAttachment, canAddMore, editRestriction))

  //  property("edit thumb setting") = statefulProp(genAllCommands(false, editThumbSetting))

  property("test invalid uploads") = statefulProp(genAllCommands((_, cl) => cl.size > 10, testFileGen(true,
    fuc => tf => fuc.illegalReason.isDefinedAt(tf)), canAddMore))


}
