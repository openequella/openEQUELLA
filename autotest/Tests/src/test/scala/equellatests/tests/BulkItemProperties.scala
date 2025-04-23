package equellatests.tests

import cats.instances.vector._
import cats.syntax.all._
import equellatests.domain.{ItemId, RandomWords, TestLogon, Uniqueify}
import equellatests.instgen.workflow
import equellatests.pages.search.ManageResourcesPage
import equellatests.restapi._
import equellatests.sections.search.{BulkOpConfirm, BulkOperationDialog, ResetToTaskConfigPage}
import equellatests.{SimpleSeleniumBrowser, SimpleTestCase, StatefulProperties}
import io.circe.generic.semiauto._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalacheck.Prop._

object BulkItemProperties extends StatefulProperties("BulkItemOps") with SimpleTestCase {

  object BulkItemOp extends Enumeration {
    type BulkItemOp = Value
    val removeWorkflow, resetToTask = Value
    implicit val encJson            = Encoder.encodeEnumeration(BulkItemOp)
    implicit val decJson            = Decoder.decodeEnumeration(BulkItemOp)
  }

  sealed trait BulkOp {
    def typ: BulkItemOp.Value
  }

  case class NoParamOp(typ: BulkItemOp.Value) extends BulkOp

  case class ResetToTask(task: String, message: String) extends BulkOp {
    def typ = BulkItemOp.resetToTask
  }

  case class RunBulkOp(names: Seq[String], op: BulkOp)

  case class BulkItemState(ops: BulkItemOp.ValueSet = BulkItemOp.ValueSet.empty)

  override type Command = RunBulkOp
  override type State   = BulkItemState

  override implicit val testCaseDecoder: Decoder[RunBulkOp] = deriveDecoder
  override implicit val testCaseEncoder: Encoder[RunBulkOp] = deriveEncoder

  override def initialState: BulkItemState = BulkItemState()

  override def runCommand(c: RunBulkOp, s: BulkItemState): BulkItemState = s.copy(s.ops + c.op.typ)

  def configOp(bog: BulkOperationDialog, op: BulkOp): Unit = op match {
    case NoParamOp(t) =>
      val title = t match {
        case BulkItemOp.removeWorkflow => "Removing from workflow"
      }
      bog.next(BulkOpConfirm(title))
    case ResetToTask(t, msg) =>
      val cp = bog.next(ResetToTaskConfigPage)
      cp.selectTask(t)
      cp.comment = msg
  }

  def opName(op: BulkItemOp.Value): String = op match {
    case BulkItemOp.removeWorkflow => "Remove from workflow..."
    case BulkItemOp.resetToTask    => "Reset to workflow task..."
  }

  def checkProp(op: BulkOp, itemIds: Vector[ItemId]): ERest[Prop] = op match {
    case NoParamOp(o) =>
      o match {
        case BulkItemOp.removeWorkflow =>
          for {
            items <- itemIds.traverse(RItems.get)
          } yield all(items.map(i => i.status ?= RStatus.live): _*)
      }
    case ResetToTask(task, msg) =>
      for {
        items <- itemIds.traverse(i => RItems.getModeration(i).product(RItems.getHistory(i)))
      } yield all(items.map { case (mod, history) =>
        all(
          mod.firstIncompleteTask
            .map(_.name ?= task)
            .getOrElse(Prop.falsified.label(s"Meant to be at task $task")),
          history.collectFirst {
            case he: RHistoryEvent
                if he.`type` == RHistoryEventType.taskMove &&
                  he.comment.isDefined && he.toStepName.contains(task) =>
              he.comment.get
          } ?= Some(msg)
        )
      }: _*)

  }

  override def runCommandInBrowser(c: RunBulkOp, s: BulkItemState, b: SimpleSeleniumBrowser): Prop =
    b.verify {
      b.resetUnique()
      val ctx    = b.page.ctx
      val opType = c.op.typ
      val itemIds = ERest.run(ctx) {
        c.names.toVector.traverse[ERest, ItemId] { n =>
          val item = RCreateItem(
            RCollectionRef(workflow.threeStepWMUuid),
            workflow.simpleMetadata(b.uniquePrefix(n))
          )
          RItems.create(item)
        }
      }

      val mrp = ManageResourcesPage(ctx).load()
      mrp.query = b.allUniqueQuery
      mrp.search()
      c.op match {
        case ResetToTask(_, _) =>
          val filters = mrp.openFilters()
          filters.onlyModeration(true)
          filters.filterByWorkflow(Some("3 Step with multiple users"))
        case NoParamOp(_) => mrp.clearFiltersIfSet()
      }
      c.names.foreach { n =>
        mrp.resultForName(b.uniquePrefix(n)).select()
      }

      val bog = mrp.performOperation()
      bog.selectAction(opName(opType))
      configOp(bog, c.op)
      bog.execute()
      mrp -> ERest.run(ctx)(checkProp(c.op, itemIds))
    }

  override def logon: TestLogon = workflow.adminLogon

  statefulProp("run bulk ops") {
    generateCommands { s =>
      val remainingOps = BulkItemOp.values -- s.ops
      if (remainingOps.isEmpty) List()
      else {
        for {
          opEnum   <- Gen.oneOf(remainingOps.toSeq)
          numItems <- Gen.chooseNum(3, 10)
          names <- Gen
            .listOfN(numItems, Arbitrary.arbitrary[RandomWords])
            .map(Uniqueify.uniqueSeq(RandomWords.withNumberAfter))
            .map(_.map(_.asString))
          op <- opEnum match {
            case BulkItemOp.resetToTask =>
              for {
                task <- Gen.oneOf(workflow.workflow3StepTasks)
                msg  <- Arbitrary.arbitrary[RandomWords]
              } yield ResetToTask(task, msg.asString)
            case o => Gen.const(NoParamOp(o))
          }
        } yield List(RunBulkOp(names, op))
      }
    }
  }
}
