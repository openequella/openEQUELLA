package equellatests.tests

import equellatests.TestCase.CommandT
import equellatests._
import equellatests.domain.{RandomWords, TestLogon, UniqueRandomWord}
import equellatests.pages.HomePage
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import equellatests.instgen.workflow._
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import equellatests.tests.WorkflowCommentProperties.{doComments, generateCommands, stdComment}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen


object BulkWorkflowProperties extends StatefulProperties("BulkWorkflowOps") {

  case class BulkItem(name:String, currentTask:Option[String], moderating:Boolean)

  case class BulkState(items:Seq[BulkItem])

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
    override def run(b: SimpleSeleniumBrowser, state: BulkState): Unit =  {
      val page = new ContributePage(b.page.ctx).load().openWizard("Simple 3 Step")
      page.ctrl(EditBoxControl, 1).value = b.unique + " " + name
      page.save().submitForModeration()
    }

    override def nextState(state: BulkState): BulkState = {
      val newItem = BulkItem(name, Some("step 1"), true)
      state.copy(items = state.items:+ newItem)

    }
  }

  override type TC = BulkTestCase
  val testCaseDecoder = deriveDecoder
  val testCaseEncoder = deriveEncoder
  def makeCommands(numItems: Int)(state: BulkState):Gen[List[BulkCommand]] = {
    if(numItems > state.items.size){
      for {
        name <- RandomWords.someWords
      } yield List(BulkCreateItem(name.asString))
    }
    else List()


  }
  property("login") = statefulProp {
    val initialState = BulkState(Seq.empty)
    for {
      numItems <- Gen.choose(1, 10)
      cmds <- generateCommands(initialState, makeCommands(numItems))
    } yield BulkTestCase(initialState, cmds)
  }

}


