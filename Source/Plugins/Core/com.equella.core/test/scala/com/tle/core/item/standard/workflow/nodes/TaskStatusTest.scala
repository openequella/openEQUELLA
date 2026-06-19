package com.tle.core.item.standard.workflow.nodes

import com.dytech.devlib.PropBagEx
import com.tle.beans.item.{Item, ItemPack}
import com.tle.common.workflow.node.WorkflowItem
import com.tle.common.workflow.{WorkflowItemStatus, WorkflowNodeStatus}
import com.tle.core.item.NodeStatus
import com.tle.core.item.operations.{ItemOperationParams, ItemOperationParamsImpl}
import com.tle.core.item.standard.operations.workflow.TaskOperation
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import java.util
import scala.jdk.CollectionConverters._

object TaskStatusTest {

  /** Input values for one `reassignIfStale` scenario. */
  final case class ReassignIfStaleInput(
      assignee: String,
      currentModerators: Set[String],
      taskId: String = "task-id"
  )

  /** Expected output from one `reassignIfStale` scenario. */
  final case class ReassignIfStaleExpected(changed: Boolean, assignee: String)
}

class TaskStatusTest extends AnyFunSpec with Matchers with TableDrivenPropertyChecks {
  import TaskStatusTest.{ReassignIfStaleExpected, ReassignIfStaleInput}

  private val originalModerator = "original-moderator"
  private val currentModerator  = "current-moderator"
  private val manualAssignee    = "manual-assignee"

  describe("reassignIfStale") {
    it("applies assignment refresh rules for stale, current, and manual assignees") {
      val scenarios = Table(
        ("name", "input", "originalModerators", "expected"),
        (
          "reassigns stale original moderator to single current moderator",
          ReassignIfStaleInput(
            assignee = originalModerator,
            currentModerators = Set(currentModerator)
          ),
          Some(Set(originalModerator)),
          ReassignIfStaleExpected(changed = true, assignee = currentModerator)
        ),
        (
          "keeps assignee who remains a current moderator",
          ReassignIfStaleInput(
            assignee = currentModerator,
            currentModerators = Set(currentModerator, "another-current-moderator")
          ),
          Some(Set(originalModerator, currentModerator)),
          ReassignIfStaleExpected(changed = false, assignee = currentModerator)
        ),
        (
          "keeps manual assignee outside original metadata moderators",
          ReassignIfStaleInput(
            assignee = manualAssignee,
            currentModerators = Set(currentModerator)
          ),
          Some(Set(originalModerator)),
          ReassignIfStaleExpected(changed = false, assignee = manualAssignee)
        ),
        (
          "does not treat null original moderators as manual-assignment evidence",
          ReassignIfStaleInput(
            assignee = originalModerator,
            currentModerators = Set(currentModerator)
          ),
          Option.empty[Set[String]],
          ReassignIfStaleExpected(changed = true, assignee = currentModerator)
        ),
        (
          "preserves non-empty assignee when original moderators are empty",
          ReassignIfStaleInput(
            assignee = manualAssignee,
            currentModerators = Set(currentModerator)
          ),
          Some(Set.empty[String]),
          ReassignIfStaleExpected(changed = false, assignee = manualAssignee)
        )
      )

      forAll(scenarios) { (_, input, originalModerators, expected) =>
        val result = refreshAssignment(input, originalModerators)
        result shouldBe expected
      }
    }
  }

  private def refreshAssignment(
      input: ReassignIfStaleInput,
      originalModerators: Option[Set[String]]
  ): ReassignIfStaleExpected = {
    val task                   = buildTaskStatus(input)
    val originalModeratorsJava = originalModerators.map(_.asJava).orNull
    val isAssigneeChanged      = task.reassignIfStale(originalModeratorsJava)
    ReassignIfStaleExpected(isAssigneeChanged, task.getAssignedTo)
  }

  private def buildTaskStatus(input: ReassignIfStaleInput): TaskStatus = {
    val workflowItem = buildWorkflowItem(input.taskId)
    val statusBean   = buildStatusBean(workflowItem, input.assignee)
    val operation    = buildOperation(input.currentModerators)
    assembleTaskStatus(workflowItem, statusBean, operation)
  }

  private def buildWorkflowItem(taskId: String): WorkflowItem = {
    val workflowItem = new WorkflowItem()
    workflowItem.setUuid(taskId)
    workflowItem
  }

  private def buildStatusBean(
      workflowItem: WorkflowItem,
      assignee: String
  ): WorkflowItemStatus = {
    val statusBean = new WorkflowItemStatus(workflowItem, null)
    statusBean.setStatus(WorkflowNodeStatus.INCOMPLETE)
    statusBean.setAssignedTo(assignee)
    statusBean
  }

  private def buildOperation(currentModerators: Set[String]): TestTaskOperation = {
    val operation                   = new TestTaskOperation(currentModerators)
    val params: ItemOperationParams = new ItemOperationParamsImpl()
    params.setItemPack(new ItemPack[Item](new Item(), new PropBagEx(), null))
    operation.setParams(params)
    operation
  }

  private def assembleTaskStatus(
      workflowItem: WorkflowItem,
      statusBean: WorkflowItemStatus,
      operation: TestTaskOperation
  ): TaskStatus = {
    val status = new TaskStatus(statusBean, operation)
    status.setWorkflowNode(workflowItem)
    operation.attach(status)
    status
  }

  /** Minimal [[TaskOperation]] stub for assignment refresh tests. */
  private final class TestTaskOperation(currentModerators: Set[String]) extends TaskOperation {

    private var attachedStatus: Option[TaskStatus] = None

    /** Links this operation to the task returned by getNodeStatus. */
    def attach(status: TaskStatus): Unit =
      attachedStatus = Some(status)

    override def execute(): Boolean = false

    override def getUsersToModerate(item: WorkflowItem): util.Set[String] =
      currentModerators.asJava

    override def getNodeStatus(uuid: String): NodeStatus =
      attachedStatus.filter(_.getWorkflowNode.getUuid == uuid).orNull
  }
}
