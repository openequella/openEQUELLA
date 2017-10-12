package equellatests.tests

import equellatests.TestCase.CommandT
import equellatests._
import equellatests.domain.{RandomWords, TestLogon, UniqueRandomWord}
import equellatests.pages.HomePage
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import equellatests.instgen.workflow._
import equellatests.pages.search.ManageResourcesPage
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import equellatests.tests.WorkflowCommentProperties.{doComments, generateCommands, stdComment}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen


object BulkWorkflowProperties extends StatefulProperties("BulkWorkflowOps") {

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
      val mrp = new ManageResourcesPage(b.page.ctx).load()
      mrp.query = s"+${b.unique}"
      mrp.search()
      names.foreach { n =>
        mrp.resultForName(b.uniquePrefix(n)).select()
      }
      mrp
    }

    override def nextState(state: BulkState): BulkState = state.copy(selected = names)
  }

  override type TC = BulkTestCase
  val testCaseDecoder = deriveDecoder
  val testCaseEncoder = deriveEncoder

  def makeCommands(numItems: Int)(state: BulkState): Gen[List[BulkCommand]] = state match {
    case _ if numItems > state.items.size => for {
      name <- RandomWords.someWords
    } yield List(BulkCreateItem(name.asString))
    case s if s.selected.isEmpty => for {
      selections <- Gen.someOf(s.items.map(_.name))
    } yield List(SelectItems(selections))
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


