package equellatests.tests

import equellatests.domain._
import equellatests._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.scalacheck.Arbitrary._
import equellatests.instgen.workflow._
import equellatests.pages.HomePage
import equellatests.pages.moderate.ModerationView
import equellatests.pages.search.TaskListPage
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import org.scalacheck.{Gen, Prop}
import org.scalacheck.Prop._

case class WorkflowComment(message: String, files: Set[String], commentClass: String)

case class CommentItem(name: String, currentTask: Option[String], comments: Seq[WorkflowComment])

case class WorkflowCommentState(logon: TestLogon, item: Option[CommentItem] = None, moderating: Boolean = false)

sealed trait MessageType

object MessageType {
  def moderates(msg: MessageType): Boolean = msg match {
    case CommentMessage => false
    case _ => true
  }
}

case object ApproveMessage extends MessageType

case object CommentMessage extends MessageType

case class RejectMessage(step: Option[String]) extends MessageType

sealed trait InvalidReason

case object NoMessage extends InvalidReason

case object BannedFile extends InvalidReason

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

  override def nextState(state: State): State = state.copy(item = Some(CommentItem(name, Some("Step 1"), Seq.empty)))
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


case class InvalidCommentCommand(msg: String, files: Seq[(TestFile, String)], msgType: MessageType,
                                 invalidReason: InvalidReason) extends VerifyCommand[String] with WorkflowCommentCommand {

  override def run(b: SimpleSeleniumBrowser, state: WorkflowCommentState) = {
    val md = b.page match {
      case mv: ModerationView =>
        val md = msgType match {
          case CommentMessage => mv.postComment()
          case ApproveMessage => mv.approve()
          case RejectMessage(_) => mv.reject()
        }
        invalidReason match {
          case NoMessage => msgType match {
            case ApproveMessage =>
              TestFile.testFiles.find(TestFile.bannedTestFile.andThen(!_)).foreach { tf =>
                md.uploadFile(tf, tf.realFilename)
                md.submitError()
              }
            case _ => md.submitError()
          }
          case BannedFile => TestFile.testFiles.find(TestFile.bannedTestFile).foreach {
            tf => md.uploadFileError(tf, tf.realFilename)
          }
        }
        md
    }
    val errMsg = md.errorMessage
    b.page = md.cancel()
    errMsg
  }

  override def postCondition(state: WorkflowCommentState, result: String) = collect(s"invalid_$invalidReason") {
    result ?= ((invalidReason,msgType) match {
      case (NoMessage, ApproveMessage) => "Please enter a message when files attached"
      case (NoMessage,_) => "You must enter a message"
      case (BannedFile, _) => "File upload cancelled. File extension has been banned"
    })
  }

  override def nextState(state: WorkflowCommentState) = state
}

case class PostCommentCommand(msg: String, files: Seq[(TestFile, String)], msgType: MessageType, cancel: Boolean) extends UnitCommand with WorkflowCommentCommand {

  val moderates = MessageType.moderates(msgType)

  override def run(b: SimpleSeleniumBrowser, state: WorkflowCommentState): Unit = b.page = {
    b.page match {
      case mv: ModerationView =>
        val md = msgType match {
          case CommentMessage => mv.postComment()
          case ApproveMessage => mv.approve()
          case RejectMessage(step) => {
            val rd = mv.reject()
            val stepName = step.getOrElse("Original Contributor")
            rd.rejectStep = stepName
            rd
          }
        }
        md.message = msg
        files.foreach {
          case (tf, fn) => md.uploadFile(tf, fn)
        }
        if (cancel) md.cancel()
        else if (moderates) md.submitModeration() else md.submit()
    }
  }

  override def successProp: Prop = classify(cancel, "cancelled") {
    collect(msgType)(true)
  }

  override def nextState(state: WorkflowCommentState): WorkflowCommentState = {
    if (cancel) state
    else state.item match {
      case Some(item) =>
        val commentClass = msgType match {
          case ApproveMessage => "approval"
          case RejectMessage(_) => "rejection"
          case _ => ""
        }
        val newComments = WorkflowComment(msg, files.map(_._2).toSet, commentClass) +: item.comments
        state.copy(item = Some(item.copy(comments = newComments,
          currentTask = item.currentTask.flatMap(nextTask))), moderating = !moderates)
    }
  }

  def nextTask(current: String) = msgType match {
    case ApproveMessage => current match {
      case "Step 1" => Some("Step 2")
      case "Step 2" => Some("Step 3")
      case "Step 3" => None
    }
    case CommentMessage => Some(current)
    case RejectMessage(task) => task
  }
}


case object VerifyComments extends VerifyCommand[Seq[WorkflowComment]] with WorkflowCommentCommand {
  override def postCondition(state: WorkflowCommentState, result: Seq[WorkflowComment]) = {
    Option(result) ?= state.item.map(_.comments)
  }

  override def run(sut: SimpleSeleniumBrowser, state: WorkflowCommentState) = sut.page match {
    case mv: ModerationView => mv.allComments().map(mc => WorkflowComment(mc.message, mc.fileNames, mc.commentClass))
  }

  override def nextState(state: WorkflowCommentState) = state
}

object WorkflowCommentProperties extends StatefulProperties("Workflow comments") {
  override type TC = WorkflowCommentTestCase
  val testCaseDecoder = Decoder.apply
  val testCaseEncoder = Encoder.apply

  val fileAndName = for {
    vfn <- arbitrary[ValidFilename]
    tf <- Gen.oneOf(TestFile.testFiles.filterNot(tf => TestFile.bannedExt(tf.extension)))
  } yield (tf, s"${vfn.filename}.${tf.extension}")

  def doComment(msgType: MessageType): Gen[(WorkflowCommentCommand, Boolean)] = for {
    comment <- arbitrary[ValidDescription]
    numFiles <- Gen.choose(0, 3)
    files <- Gen.listOfN(numFiles, fileAndName)
    cancel <- Gen.frequency(1 -> true, 4 -> false)
    failed <- Gen.frequency(3 -> None, 1 -> Gen.oneOf(NoMessage, BannedFile).map(Option.apply))
  } yield {
    failed.map(ft => (InvalidCommentCommand(comment.desc, files, msgType, ft).asInstanceOf[WorkflowCommentCommand], true)).getOrElse {
      (PostCommentCommand(comment.desc, files, msgType, cancel), cancel || !MessageType.moderates(msgType))
    }
  }


  def rejectStep(currentTask: String): Gen[Option[String]] = currentTask match {
    case "Step 2" => Gen.frequency(2 -> Some("Step 1"), 1 -> None)
    case "Step 3" => Gen.frequency(2 -> Some("Step 2"), 2 -> Some("Step 1"), 1 -> None)
    case _ => Gen.const(None)
  }

  def doComments(state: WorkflowCommentState): Gen[List[WorkflowCommentCommand]] = state.item match {
    case Some(CommentItem(itemName, Some(task), _)) => for {
      msgType <- Gen.frequency(2 -> CommentMessage, 1 -> ApproveMessage, 1 -> rejectStep(task).map(RejectMessage))
      (pcc, staysModerating) <- doComment(msgType)
    } yield {
      if (!state.moderating) List(ModerateItemCommand(itemName), VerifyComments, pcc)
      else if (staysModerating) List(pcc, VerifyComments)
      else List(pcc)
    }
    case None => arbitrary[UniqueRandomWord].map { urw =>
      val itemName = urw.word
      List(CreateItemCommand(itemName))
    }
    case _ => Gen.const(List.empty)
  }

  property("comment on workflow steps") = statefulProp {
    val initialState = WorkflowCommentState(adminLogon)
    generateCommands(initialState, doComments).map(WorkflowCommentTestCase(initialState, _))
  }
}
