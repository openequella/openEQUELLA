package equellatests.tests

import equellatests.domain._
import equellatests.models.wizardfileupload.FileUploadTestCase
import equellatests._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.scalacheck.Arbitrary._
import equellatests.instgen.workflow._
import equellatests.pages.HomePage
import equellatests.pages.moderate.{ModerationComment, ModerationView}
import equellatests.pages.search.TaskListPage
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import org.scalacheck.Gen
import org.scalacheck.Prop._

case class WorkflowComment(message: String, files: Set[String])

case class CommentItem(name: String, comments: Seq[WorkflowComment])

case class WorkflowCommentState(logon: TestLogon, item: Option[CommentItem] = None)


case class WorkflowCommentTestCase(initialState: WorkflowCommentState, commands: List[WorkflowCommentCommand]) extends LogonTestCase {
  override type Browser = SimpleSeleniumBrowser
  override type State = WorkflowCommentState

  override def logon = initialState.logon

  override def createInital = SimpleSeleniumBrowser
}

sealed trait WorkflowCommentCommand extends Command {
  type Browser = SimpleSeleniumBrowser
  type State = WorkflowCommentState
}

case class CreateItemCommand(name: String) extends UnitCommand with WorkflowCommentCommand {
  override def run(b: Browser, state: State): Unit = b.page = b.page match {
    case h: HomePage =>
      val page = new ContributePage(h.ctx).load().openWizard("Simple 3 Step")
      page.ctrl(EditBoxControl, 1).value = name
      page.save().submitForModeration()
  }

  override def nextState(state: State): State = state.copy(item = Some(CommentItem(name, Seq.empty)))
}

case class ModerateItemCommand(name: String) extends UnitCommand with WorkflowCommentCommand {
  override def run(b: Browser, state: State): Unit = {
    b.page = {
      val tlp = new TaskListPage(b.page.ctx).load()
      tlp.query = name
      tlp.search().resultForName(name).moderate()
    }
  }

  override def nextState(state: State): State = state
}


case class PostCommentCommand(msg: String, files: Seq[(TestFile, String)]) extends UnitCommand with WorkflowCommentCommand {
  override def run(b: SimpleSeleniumBrowser, state: WorkflowCommentState): Unit = b.page = {
    b.page match {
      case mv: ModerationView =>
        val md = mv.postComment()
        md.message = msg
        files.foreach {
          case (tf, fn) => md.uploadFile(tf, fn)
        }
        md.submit()
    }
  }

  override def nextState(state: WorkflowCommentState): WorkflowCommentState = state.item match {
    case Some(item) => state.copy(item = Some(item.copy(comments = WorkflowComment(msg, files.map(_._2).toSet) +: item.comments)))
  }
}


case object VerifyComments extends VerifyCommand[Seq[WorkflowComment]] with WorkflowCommentCommand
{
  override def postCondition(state: WorkflowCommentState, result: Seq[WorkflowComment]) = {
    Option(result) ?= state.item.map(_.comments)
  }

  override def run(sut: SimpleSeleniumBrowser, state: WorkflowCommentState) = sut.page match {
    case mv: ModerationView => mv.allComments().map(mc => WorkflowComment(mc.message, mc.fileNames))
  }

  override def nextState(state: WorkflowCommentState) = state
}

object WorkflowCommentProperties extends StatefulProperties("Workflow comments"){
  override type TC = WorkflowCommentTestCase
  val testCaseDecoder = Decoder.apply
  val testCaseEncoder = Encoder.apply

  val fileAndName = for {
    vfn <- arbitrary[ValidFilename]
    tf <- Gen.oneOf(TestFile.testFiles.filterNot(tf => TestFile.bannedExt(tf.extension)))
  } yield (tf, s"${vfn.filename}.${tf.extension}")

  def doComment = for {
    comment <- arbitrary[ValidDescription]
    numFiles <- Gen.choose(0, 3)
    files <- Gen.listOfN(numFiles, fileAndName)
  } yield PostCommentCommand(comment.desc, files)

  val genBlankTestCase = for {
    logon <- Gen.const(adminLogon)
    itemName <- arbitrary[UniqueRandomWord].map(_.word)
    numComments <- Gen.choose(0, 3)
    comments <- Gen.listOfN(numComments, doComment)
  } yield WorkflowCommentTestCase(WorkflowCommentState(logon),
    List(CreateItemCommand(itemName),
      ModerateItemCommand(itemName)) ++ comments ++ List(VerifyComments)
  )

  property("login") = statefulProp(genBlankTestCase)
}
