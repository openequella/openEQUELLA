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

  def genAllCommands(testFailures: Boolean): Gen[FileUploadTestCase] = {
    def genNext(left: Int, s: TestState, curList: List[FileUploadCommand]): Gen[List[FileUploadCommand]] = {
      if (left == 0) curList else genCommand(s).flatMap(com => genNext(left - 1, com.nextState(s), com :: curList))
    }

    for {
      is <- genInitialState(testFailures)
      commandsLeft <- Gen.size
      cl <- genNext(commandsLeft, is, Nil)
    } yield FileUploadTestCase(is, cl.reverse)
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


  def genInitialState(testFailures: Boolean): Gen[TestState] = for {
    tl <- arbitrary[TestLogon]
  } yield TestState(tl, Seq.empty, LoginPageLoc, testFailures)

  def genCommand(state: TestState): Gen[FileUploadCommand] = {
    state.currentPage match {
      case LoginPageLoc =>
        for {
          name <- arbitrary[UniqueRandomWord]
          wizard <- Gen.oneOf(TestState.wizards)
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

        if (item.attachments.isEmpty) newAttachment else if (addableCtrls.isEmpty) editAttachment else {
          Gen.frequency(1 -> newAttachment, 5 -> editAttachment)
        }
      case adp@AttachmentDetailsPage(a, edited, uc, page1) => if (a != edited) Gen.oneOf(CloseEditDialog, SaveAttachment) else {
        editForAttachment(edited, uc).map(ae => EditAttachmentDetails(ae))
      }
    }
  }

  override def overrideParameters(p: Test.Parameters): Test.Parameters = p.withMinSize(5).withMaxSize(10)

  property("test valid uploads") = statefulProp(genAllCommands(false))

  property("test invalid uploads") = statefulProp(genAllCommands(true))


}
