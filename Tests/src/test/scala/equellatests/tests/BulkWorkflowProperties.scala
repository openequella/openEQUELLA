package equellatests.tests

import equellatests._
import equellatests.domain.{RandomWords, TestLogon}
import equellatests.instgen.workflow._
import equellatests.pages.HomePage
import equellatests.pages.search.{BulkApproveMessage, BulkModerateMessage, BulkRejectMessage, ManageTasksPage}
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import io.circe.generic.semiauto._
import io.circe.generic.auto._
import org.scalacheck.{Arbitrary, Gen}


object BulkWorkflowProperties extends StatefulProperties("BulkWorkflowOps") with LogonTestCase {
  type Command = BulkCommand
  type State = BulkState

  def logon = adminLogon
  override def createInital = SimpleSeleniumBrowser

  sealed trait BulkOp
  case class Approve(message: String) extends BulkOp
  case class Reject(message: String) extends BulkOp

  case class BulkItem(name: String, currentTask: Option[String], moderating: Boolean)
  case class BulkState(items: Seq[BulkItem], selected: Seq[String])

  sealed trait BulkCommand
  case class BulkCreateItem(name: String) extends BulkCommand
  case class SelectItems(names: Seq[String]) extends BulkCommand
  case class PerformOp(op: BulkOp) extends BulkCommand

  val testCaseDecoder = deriveDecoder
  val testCaseEncoder = deriveEncoder

  def makeCommands(numItems: Int)(state: BulkState): Gen[List[BulkCommand]] = state match {
    case _ if numItems > state.items.size => for {
      name <- RandomWords.someWords
    } yield List(BulkCreateItem(name.asString))
    case s if s.selected.isEmpty => for {
      selections <- Gen.someOf(s.items.map(_.name)).suchThat(_.nonEmpty)
      msg <- Arbitrary.arbitrary[RandomWords].map(_.asString)
      op <- Gen.oneOf(Approve(msg), Reject(msg))
    } yield List(SelectItems(selections), PerformOp(op))
    case _ => List()
  }

  val initialState = BulkState(Seq.empty, Seq.empty)

  statefulProp("login") {
    for {
      numItems <- Gen.choose(1, 10)
      tc <- generateCommands(initialState, makeCommands(numItems))
    } yield tc
  }

  override def runCommand(c: BulkCommand, s: BulkState) = c match {
    case BulkCreateItem(name) =>
      val newItem = BulkItem(name, Some("step 1"), true)
      s.copy(items = s.items :+ newItem)
    case SelectItems(names) => s.copy(selected = names)
    case PerformOp(op) => s
  }

  override def runCommandInBrowser(c: BulkCommand, s: BulkState, b: SimpleSeleniumBrowser) = c match {
    case BulkCreateItem(name) => b.run {
      val page = new ContributePage(b.page.ctx).load().openWizard("Simple 3 Step")
      page.ctrl(EditBoxControl, 1).value = b.uniquePrefix(name)
      page.save().submitForModeration()
    }
    case SelectItems(names) => b.run {
      val mrp = new ManageTasksPage(b.page.ctx).load()
      mrp.query = s"+${b.unique}"
      mrp.search()
      names.foreach { n =>
        mrp.resultForName(b.uniquePrefix(n)).select()
      }
      mrp
    }
    case PerformOp(op) => b.run {
      b.page match {
        case mtp: ManageTasksPage =>
          val bd = mtp.performOperation()
          val (opName, bmd, msg) = op match {
            case Approve(m) => ("Approve tasks...", BulkApproveMessage, m)
            case Reject(m) => ("Reject tasks...", BulkRejectMessage, m)
          }
          bd.selectAction(opName)
          val msgPage = bd.next(bmd)
          msgPage.comment = msg
          bd.execute()
          bd.cancel()
          mtp
      }
    }
  }

}


