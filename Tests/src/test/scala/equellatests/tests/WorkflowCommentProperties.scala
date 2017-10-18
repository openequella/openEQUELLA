package equellatests.tests

import equellatests.domain._
import equellatests._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.scalacheck.Arbitrary._
import equellatests.instgen.workflow._
import equellatests.pages.HomePage
import equellatests.pages.moderate.ModerationView
import equellatests.pages.search.TaskListPage
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import org.scalacheck.{Gen, Prop}
import org.scalacheck.Prop._


object WorkflowCommentProperties extends StatefulProperties("Workflow comments") with LogonTestCase {

  type State = WorkflowCommentState
  type Command = WorkflowCommentCommand

  object MessageTypes extends Enumeration {
    val Approve, Comment, Reject = Value
  }

  case class WorkflowComment(message: String, files: Set[String], typ: MessageTypes.Value)

  case class CommentItem(name: String, currentTask: Option[String], comments: Seq[WorkflowComment])

  case class WorkflowCommentState(items: Seq[CommentItem] = Seq.empty, item: Option[CommentItem] = None,
                                  moderating: Boolean = false, attemptedInvalid: Boolean = false, commentTypes: MessageTypes.ValueSet =
                                  MessageTypes.ValueSet.empty) {
    lazy val itemNames = items.map(_.name).toSet
  }

  sealed abstract class MessageType(val typ: MessageTypes.Value)

  object MessageType {
    def moderates(msg: MessageType): Boolean = msg match {
      case CommentMessage => false
      case _ => true
    }
  }

  case object ApproveMessage extends MessageType(MessageTypes.Approve)

  case object CommentMessage extends MessageType(MessageTypes.Comment)

  case class RejectMessage(step: Option[String]) extends MessageType(MessageTypes.Reject)

  sealed trait InvalidReason

  case object NoMessage extends InvalidReason

  case object BannedFile extends InvalidReason

  sealed trait WorkflowCommentCommand

  case class CreateItemCommand(name: String) extends WorkflowCommentCommand

  case class ModerateItemCommand(name: String) extends WorkflowCommentCommand

  case class InvalidCommentCommand(msgType: MessageType, invalidReason: InvalidReason) extends WorkflowCommentCommand

  case class PostCommentCommand(msg: String, files: Seq[(TestFile, String)], msgType: MessageType, cancel: Boolean)
    extends WorkflowCommentCommand


  case object VerifyComments extends WorkflowCommentCommand


  val fileAndName = for {
    vfn <- arbitrary[ValidFilename]
    tf <- Gen.oneOf(TestFile.testFiles.filterNot(tf => TestFile.bannedExt(tf.extension)))
  } yield (tf, s"${vfn.filename}.${tf.extension}")

  def errorComment(inv: InvalidReason)(msgType: MessageType): Gen[(WorkflowCommentCommand, Boolean)] = {
    Gen.oneOf(stdComment(RejectMessage(None)), Gen.const((InvalidCommentCommand(msgType, inv), true)))
  }

  def stdComment(msgType: MessageType): Gen[(WorkflowCommentCommand, Boolean)] = for {
    comment <- arbitrary[RandomWords]
    numFiles <- Gen.choose(0, 3)
    files <- Gen.listOfN(numFiles, fileAndName)
    cancel <- Gen.frequency(1 -> true, 4 -> false)
    blankMessage <- Gen.frequency(2 -> false, 1 -> true).map(mb => mb && numFiles == 0 && msgType.typ == MessageTypes.Approve )
  } yield {
    val message = if (blankMessage) "" else comment.asString
    (PostCommentCommand(message, files, msgType, cancel), cancel || !MessageType.moderates(msgType))
  }


  def doComments(f: WorkflowCommentState => Boolean, doComment: MessageType => Gen[(WorkflowCommentCommand, Boolean)])
                (state: WorkflowCommentState): Gen[List[WorkflowCommentCommand]] = state.item match {
    case Some(CommentItem(itemName, Some(task), _)) =>
      for {
        rejectStep <- Gen.frequency(rejectionTasks3Step(task).map(s => 3 -> Gen.const(Option(s))) :+ (1, Gen.const(None)): _*)
        msgType <- Fairness.favour3to1[MessageType](Seq(CommentMessage, ApproveMessage, RejectMessage(rejectStep)),
          t => state.commentTypes.contains(t.typ))
        (pcc, staysModerating) <- doComment(msgType)
      } yield {
        if (!state.moderating) List(ModerateItemCommand(itemName), VerifyComments, pcc)
        else if (staysModerating) List(pcc, VerifyComments)
        else List(pcc)
      }
    case _ if f(state) =>
      arbitrary[RandomWords].map { urw => List(CreateItemCommand(Uniqueify.uniquelyNumbered(state.itemNames, urw.asString))) }
    case _ => Gen.const(List.empty)
  }

  val testCaseDecoder = deriveDecoder
  val testCaseEncoder = deriveEncoder

  override def initialState: WorkflowCommentState = WorkflowCommentState()


  override def runCommand(c: WorkflowCommentCommand, s: WorkflowCommentState) = c match {
    case CreateItemCommand(name) =>
      val newItem = CommentItem(name, Some("Step 1"), Seq.empty)
      s.copy(item = Some(newItem), items = s.items :+ newItem)
    case InvalidCommentCommand(msgType, invalidReason) => s.copy(attemptedInvalid = true)
    case PostCommentCommand(msg, files, msgType, cancel) =>
      def nextTask(current: String) = msgType match {
        case ApproveMessage => nextTask3Step(current)
        case CommentMessage => Some(current)
        case RejectMessage(task) => task
      }

      if (cancel) s
      else s.item.map { item =>
        val newComments = WorkflowComment(msg, files.map(_._2).toSet, msgType.typ) +: item.comments
        s.copy(item = Some(item.copy(comments = newComments,
          currentTask = item.currentTask.flatMap(nextTask))),
          moderating = !MessageType.moderates(msgType),
          commentTypes = s.commentTypes + msgType.typ)
      }.getOrElse(s)
    case ModerateItemCommand(name) => s
    case VerifyComments => s
  }

  override def runCommandInBrowser(c: WorkflowCommentCommand, s: WorkflowCommentState, b: SimpleSeleniumBrowser) = c match {
    case CreateItemCommand(name) => b.run {
      val page = new ContributePage(b.page.ctx).load().openWizard("Simple 3 Step")
      page.ctrl(EditBoxControl, 1).value = b.uniquePrefix(name)
      page.save().submitForModeration()
    }
    case ModerateItemCommand(n) => b.run {
      val tlp = new TaskListPage(b.page.ctx).load()
      val realName = b.uniquePrefix(n)
      tlp.query = s"+$realName"
      tlp.search().resultForName(realName).moderate()
    }
    case InvalidCommentCommand(msgType, invalidReason) => b.verifyOnPage {
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
        val errMsg = md.errorMessage
        md.cancel() -> collect(s"invalid_$invalidReason") {
          errMsg ?= ((invalidReason, msgType) match {
            case (NoMessage, ApproveMessage) => "Please enter a message when files attached"
            case (NoMessage, _) => "You must enter a message"
            case (BannedFile, _) => "File upload cancelled. File extension has been banned"
          })
        }
    }
    case PostCommentCommand(msg, files, msgType, cancel) => b.verifyOnPage {
      case mv: ModerationView =>
        val md = msgType match {
          case CommentMessage => mv.postComment()
          case ApproveMessage => mv.approve()
          case RejectMessage(step) =>
            val rd = mv.reject()
            val stepName = step.getOrElse("Original Contributor")
            rd.rejectStep = stepName
            rd
        }
        md.message = msg
        files.foreach {
          case (tf, fn) => md.uploadFile(tf, fn)
        }
        val newpage = if (cancel) md.cancel()
        else if (MessageType.moderates(msgType)) md.submitModeration() else md.submit()
        newpage -> classify(cancel, "cancelled") {
          collect(msgType)(true)
        }
    }
    case VerifyComments => b.verifyOnPage {
      case mv: ModerationView =>
        val result = mv.allComments().map(mc => WorkflowComment(mc.message, mc.fileNames, mc.commentClass match {
          case "approval" => MessageTypes.Approve
          case "rejection" => MessageTypes.Reject
          case _ => MessageTypes.Comment
        }))
        (mv, Option(result) ?= s.item.map(_.comments.filter(_.message.nonEmpty)))
    }
  }

  override def logon = adminLogon

  statefulProp("comment on workflow steps") {
    generateCommands(doComments(_.commentTypes != MessageTypes.values, stdComment))
  }

  statefulProp("comment with no message") {
    generateCommands(doComments(!_.attemptedInvalid, errorComment(NoMessage)))
  }

  statefulProp("upload banned file") {
    generateCommands(doComments(!_.attemptedInvalid, errorComment(BannedFile)))
  }

}
