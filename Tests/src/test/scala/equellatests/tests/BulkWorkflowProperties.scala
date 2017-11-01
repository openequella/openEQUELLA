package equellatests.tests

import com.tle.webtests.framework.PageContext
import equellatests._
import equellatests.domain.{Fairness, RandomWords, Uniqueify}
import equellatests.instgen.workflow._
import equellatests.pages.search._
import equellatests.pages.wizard.{ContributePage, EditBoxControl}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import org.scalacheck.Prop._
import org.scalacheck.{Arbitrary, Gen, Prop}

object BulkWorkflowProperties extends StatefulProperties("BulkWorkflowOps") with LogonTestCase {
  type Command = BulkCommand
  type State = BulkState

  val PageMax = 10
  def logon = adminLogon

  object BulkOpTypes extends Enumeration {
    val Approve, Reject, Reassign = Value
  }

  sealed abstract class BulkOp(val typ: BulkOpTypes.Value)

  case class Approve(message: String) extends BulkOp(BulkOpTypes.Approve)

  case class Reject(message: String) extends BulkOp(BulkOpTypes.Reject)

  case class Reassign(to: String) extends BulkOp(BulkOpTypes.Reassign)

  case class BulkItem(name: String, currentTask: Option[String], status: String, assignedTo: Option[String],
                      assignedAtStep: Map[String, Option[String]] = Map.empty) {
    def assignTo(user: Option[String]): BulkItem = currentTask.map {
      task => copy(assignedTo = user, assignedAtStep = assignedAtStep.updated(task, user))
    }.getOrElse(this)
  }

  case class BulkState(items: Seq[BulkItem] = Seq.empty, selected: Seq[String] = Seq.empty,
                       scenarios: BulkOpTypes.ValueSet = BulkOpTypes.ValueSet.empty) {
    lazy val itemsInModeration = items.filter(_.currentTask.isDefined)
    lazy val itemNames = items.map(_.name).toSet
  }

  sealed trait BulkCommand

  case class BulkCreateItem(name: String) extends BulkCommand

  case class SelectItems(names: Seq[String]) extends BulkCommand

  case class PerformOp(op: BulkOp) extends BulkCommand

  case object VerifyItems extends BulkCommand

  val testCaseDecoder = deriveDecoder
  val testCaseEncoder = deriveEncoder

  def makeCommands(requiredScenarios: BulkOpTypes.ValueSet): Gen[Seq[BulkCommand]] = for {
    numItems <- Gen.choose(1, 5)
    com <- generateCommands {
      case s if requiredScenarios.subsetOf(s.scenarios) => List()
      case s if s.itemsInModeration.size < numItems && s.items.size < PageMax => for {
        name <- RandomWords.someWords
      } yield List(BulkCreateItem(Uniqueify.uniquelyNumbered(s.itemNames, name.asString)))
      case s if s.itemsInModeration.size > 1 => for {
        numPicks <- Gen.choose(1, s.itemsInModeration.size)
        selections <- Gen.pick(numPicks, s.itemsInModeration.map(_.name))
        msg <- Arbitrary.arbitrary[RandomWords].map(_.asString)
        randomUser <- Gen.oneOf("admin", "SimpleModerator")
        op <- Fairness.favour3to1[BulkOp](Seq(Approve(msg), Reject(msg), Reassign(randomUser)), b => s.scenarios.contains(b.typ))
      } yield List(SelectItems(selections), PerformOp(op), VerifyItems)
      case _ => List()
    }
  } yield com


  val initialState = BulkState()

  statefulProp("all ops work") {
    makeCommands(BulkOpTypes.values)
  }

  def processAutoAssign(item: BulkItem, task: Option[String]): Option[String] = {
    task.flatMap {
      curTask => workflow3StepBefore(curTask).collectFirst(item.assignedAtStep).flatten
    }
  }

  override def runCommand(c: BulkCommand, s: BulkState) = c match {
    case BulkCreateItem(name) =>
      val newItem = BulkItem(name, Some("Step 1"), status = "Moderating", assignedTo = None)
      s.copy(items = s.items :+ newItem)
    case SelectItems(names) => s.copy(selected = names)
    case PerformOp(op) => s.copy(selected = Seq.empty, scenarios = s.scenarios + op.typ, items = s.items.map { i =>
      if (s.selected.contains(i.name)) {
        op match {
          case Approve(message) =>
            val nextTask = i.currentTask.flatMap(nextTask3Step)
            val assignedTo = processAutoAssign(i, nextTask)
            i.copy(currentTask = nextTask, status = if (nextTask.isDefined) "Moderating" else "Live")
              .assignTo(assignedTo)
          case Reject(message) =>
            val nextTask = i.currentTask.flatMap(t => rejectionTasks3Step(t).headOption)
            val assignedTo = processAutoAssign(i, nextTask)
            i.copy(currentTask = nextTask, status = if (nextTask.isDefined) "Moderating" else "Rejected")
              .assignTo(assignedTo)
          case Reassign(to) => i.assignTo(Some(to))
        }
      } else i
    })
    case VerifyItems => s
  }

  override def runCommandInBrowser(c: BulkCommand, s: BulkState, b: SimpleSeleniumBrowser) = c match {
    case BulkCreateItem(name) => b.run {
      val page = new ContributePage(b.page.ctx).load().openWizard("Simple 3 Step with multiple")
      page.ctrl(EditBoxControl, 1).value = b.uniquePrefix(name)
      page.save().submitForModeration()
    }
    case SelectItems(names) => b.run {
      val mrp = new ManageTasksPage(b.page.ctx).load()
      mrp.query = b.allUniqueQuery
      mrp.search()
      names.foreach { n =>
        mrp.resultForName(b.uniquePrefix(n)).select()
      }
      mrp
    }
    case PerformOp(op) => b.runOnPage {
      case mtp: ManageTasksPage =>
        val bd = mtp.performOperation()
        def commentOn(bmd: PageContext => BulkModerateMessage, msg: String)(bd: BulkOperationDialog): Unit = {
          val msgPage = bd.next(bmd)
          msgPage.comment = msg
        }
        def reassignTo(to: String)(bd: BulkOperationDialog): Unit = {
          val up = bd.next(BulkAssignUserPage)
          up.search(to)
          up.selectByUsername(to)
        }
        val (opName, opConfig) = op match {
          case Approve(m) => "Approve tasks..." -> commentOn(BulkApproveMessage, m) _
          case Reject(m) => "Reject tasks..." -> commentOn(BulkRejectMessage, m) _
          case Reassign(to) => "Assign/reassign to moderator..." -> reassignTo(to) _
        }
        bd.selectAction(opName)
        opConfig(bd)
        bd.execute()
        bd.cancel()
        mtp
    }
    case VerifyItems => b.verify {
      val correctTasks = if (s.itemsInModeration.nonEmpty) {
        val mrp = new ManageTasksPage(b.page.ctx).load()
        mrp.query = b.allUniqueQuery
        mrp.search()
        s.items.collect {
          case BulkItem(n, Some(task), _, ato, _) =>
            val res = mrp.resultForName(b.uniquePrefix(n))
            val taskOn = res.taskOn
            val assignedTo = res.assignedTo(adminLogon.fullName).map(nameToUsername)
            ((taskOn ?= task) && (assignedTo ?= ato)).label(s"Item: $n")
        }
      } else Seq.empty
      val mrsp = new ManageResourcesPage(b.page.ctx).load()
      mrsp.query = b.allUniqueQuery
      mrsp.search()
      val allStatuses = s.items.map {
        case BulkItem(n, _, status, _, _) => mrsp.resultForName(b.uniquePrefix(n)).status ?= status
      }
      mrsp -> Prop.all(correctTasks ++ allStatuses: _*)
    }
  }

}


