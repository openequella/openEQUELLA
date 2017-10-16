package equellatests.tests

import equellatests._
import equellatests.domain.{RandomWords, TestLogon}
import equellatests.instgen.workflow._
import equellatests.pages.HomePage
import equellatests.pages.search.{BulkModerateMessage, ManageTasksPage}
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import io.circe.generic.semiauto._
import io.circe.generic.auto._
import org.scalacheck.{Arbitrary, Gen}


object BulkWorkflowProperties extends StatefulProperties("BulkWorkflowOps") {

  sealed trait BulkOp
  case class Approve(message: String) extends BulkOp
  case class Reject(message: String) extends BulkOp

  case class BulkItem(name: String, currentTask: Option[String], moderating: Boolean)

  case class BulkState(items: Seq[BulkItem], selected: Seq[String])

  case class BulkTestCase(initialState: BulkState, commands: List[BulkCommand]) extends LogonTestCase {
    override type State = BulkState
    override type Browser = SimpleSeleniumBrowser

    override def logon: TestLogon = adminLogon

    override def createInital: (HomePage) => SimpleSeleniumBrowser = SimpleSeleniumBrowser
  }

  sealed trait BulkCommand extends Command {
    override type State = BulkState
    override type Browser = SimpleSeleniumBrowser
  }

  case class BulkCreateItem(name: String) extends UnitCommand with BulkCommand {
    override def run(b: SimpleSeleniumBrowser, state: BulkState): Unit = {
      val page = new ContributePage(b.page.ctx).load().openWizard("Simple 3 Step")
      page.ctrl(EditBoxControl, 1).value = b.uniquePrefix(name)
      page.save().submitForModeration()
    }

    override def nextState(state: BulkState): BulkState = {
      val newItem = BulkItem(name, Some("step 1"), true)
      state.copy(items = state.items :+ newItem)
    }
  }

  case class SelectItems(names: Seq[String]) extends UnitCommand with BulkCommand {
    override def run(b: SimpleSeleniumBrowser, state: BulkState): Unit = b.page = {
      val mrp = new ManageTasksPage(b.page.ctx).load()
      mrp.query = s"+${b.unique}"
      mrp.search()
      names.foreach { n =>
        mrp.resultForName(b.uniquePrefix(n)).select()
      }
      mrp
    }

    override def nextState(state: BulkState): BulkState = state.copy(selected = names)
  }

  case class PerformOp(op: BulkOp) extends UnitCommand with BulkCommand {
    override def run(b: SimpleSeleniumBrowser, state: BulkState): Unit = b.page = {
      b.page match {
        case mtp: ManageTasksPage =>
          val bd = mtp.performOperation()
          val (opName, bmd, msg) = op match {
            case Approve(m) => ("Approve tasks...", BulkModerateMessage.approveMessage, m)
            case Reject(m) => ("Reject tasks...", BulkModerateMessage.rejectMessage, m)
          }
          bd.selectAction(opName)
          val msgPage = bd.next(bmd)
          msgPage.comment = msg
          bd.execute()
          bd.cancel()
          mtp
      }
    }

    override def nextState(state: BulkState): BulkState = state
  }

  override type TC = BulkTestCase
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

  property("login") = statefulProp {
    for {
      numItems <- Gen.choose(1, 10)
      tc <- generateTestCase(BulkTestCase, initialState, makeCommands(numItems))
    } yield tc
  }

}


