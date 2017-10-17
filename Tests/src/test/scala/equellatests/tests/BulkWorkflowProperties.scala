package equellatests.tests

import equellatests._
import equellatests.domain.{RandomWords, TestLogon}
import equellatests.instgen.workflow._
import equellatests.pages.HomePage
import equellatests.pages.search._
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import io.circe.generic.semiauto._
import io.circe.generic.auto._
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalacheck.Prop._

object BulkWorkflowProperties extends StatefulProperties("BulkWorkflowOps") with LogonTestCase {
  type Command = BulkCommand
  type State = BulkState

  def logon = adminLogon

  sealed trait BulkOp

  case class Approve(message: String) extends BulkOp

  case class Reject(message: String) extends BulkOp

  case class BulkItem(name: String, currentTask: Option[String], status: String)

  case class BulkState(items: Seq[BulkItem], selected: Seq[String]) {
    lazy val itemsInModeration = items.filter(_.currentTask.isDefined)
  }

  sealed trait BulkCommand

  case class BulkCreateItem(name: String) extends BulkCommand

  case class SelectItems(names: Seq[String]) extends BulkCommand

  case class PerformOp(op: BulkOp) extends BulkCommand

  case object VerifyItems extends BulkCommand

  val testCaseDecoder = deriveDecoder
  val testCaseEncoder = deriveEncoder

  def makeCommands(numItems: Int, opf: String => Gen[BulkOp])(state: BulkState): Gen[List[BulkCommand]] = state match {
    case _ if numItems > state.items.size => for {
      name <- RandomWords.someWords
    } yield List(BulkCreateItem(name.asString))
    case s if s.selected.isEmpty && s.itemsInModeration.nonEmpty => for {
      numPicks <- Gen.choose(1, s.itemsInModeration.size)
      selections <- Gen.pick(numPicks, s.itemsInModeration.map(_.name))
      msg <- Arbitrary.arbitrary[RandomWords].map(_.asString)
      op <- opf(msg)
    } yield List(SelectItems(selections), PerformOp(op), VerifyItems)
    case _ => List()
  }

  val initialState = BulkState(Seq.empty, Seq.empty)

  statefulProp("approve mostly") {
    for {
      numItems <- Gen.choose(1, 5)
      tc <- generateCommands(makeCommands(numItems, m => Gen.frequency(5 -> Approve(m), 1 -> Reject(m))))
    } yield tc
  }

  statefulProp("reject") {
    for {
      numItems <- Gen.choose(1, 5)
      tc <- generateCommands(makeCommands(numItems, m => Gen.const(Reject(m))))
    } yield tc
  }

  override def runCommand(c: BulkCommand, s: BulkState) = c match {
    case BulkCreateItem(name) =>
      val newItem = BulkItem(name, Some("Step 1"), status = "Moderating")
      s.copy(items = s.items :+ newItem)
    case SelectItems(names) => s.copy(selected = names)
    case PerformOp(op) => s.copy(selected = Seq.empty, items = s.items.map { i =>
      if (s.selected.contains(i.name)) {
        op match {
          case Approve(message) =>
            val nextTask = i.currentTask.flatMap(nextTask3Step)
            i.copy(currentTask = nextTask, status = if (nextTask.isDefined) "Moderating" else "Live")
          case Reject(message) =>
            val nextTask = i.currentTask.flatMap(t => rejectionTasks3Step(t).headOption)
            i.copy(currentTask = nextTask, status = if (nextTask.isDefined) "Moderating" else "Rejected")
        }
      } else i
    })
    case VerifyItems => s
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
    case PerformOp(op) => b.runOnPage {
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
    case VerifyItems => b.verify {
      val correctTasks = if (s.itemsInModeration.nonEmpty) {
        val mrp = new ManageTasksPage(b.page.ctx).load()
        mrp.query = s"+${b.unique}"
        mrp.search()
        s.items.collect {
          case BulkItem(n, Some(task), _) => mrp.resultForName(b.uniquePrefix(n)).taskOn ?= task
        }
      } else Seq.empty
      val mrsp = new ManageResourcesPage(b.page.ctx).load()
      mrsp.query = s"+${b.unique}"
      mrsp.search()
      val allStatuses = s.items.map {
        case BulkItem(n, _, status) => mrsp.resultForName(b.uniquePrefix(n)).status ?= status
      }
      mrsp -> Prop.all(correctTasks ++ allStatuses: _*)
    }
  }

}


