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

import java.util
import scala.jdk.CollectionConverters._

object TaskStatusAssignmentRefreshTest {

  /** Inputs for a refresh scenario: starting assignee and post-edit moderators. */
  final case class AssignmentRefreshFixture(
      assignee: String,
      currentModerators: Set[String],
      taskId: String = "task-id"
  )

  /** Outcome of calling reassignIfStale. */
  final case class AssignmentRefreshResult(changed: Boolean, assignee: String)
}

class TaskStatusAssignmentRefreshTest extends AnyFunSpec with Matchers {
  import TaskStatusAssignmentRefreshTest.{AssignmentRefreshFixture, AssignmentRefreshResult}

  private val originalModerator = "original-moderator"
  private val currentModerator  = "current-moderator"
  private val manualAssignee    = "manual-assignee"

  describe("reassignIfStale") {
    it("reassigns a stale original moderator to the single current moderator") {
      val result = refreshAssignment(
        fixture = AssignmentRefreshFixture(
          assignee = originalModerator,
          currentModerators = Set(currentModerator)
        ),
        originalModerators = Some(Set(originalModerator))
      )
      assertRefreshResult(result, expectedChanged = true, expectedAssignee = currentModerator)
    }

    it("keeps an assignee that is still a current moderator") {
      val result = refreshAssignment(
        fixture = AssignmentRefreshFixture(
          assignee = currentModerator,
          currentModerators = Set(currentModerator, "another-current-moderator")
        ),
        originalModerators = Some(Set(originalModerator, currentModerator))
      )
      assertRefreshResult(result, expectedChanged = false, expectedAssignee = currentModerator)
    }

    it("keeps a manual assignee outside the original metadata moderators") {
      val result = refreshAssignment(
        fixture = AssignmentRefreshFixture(
          assignee = manualAssignee,
          currentModerators = Set(currentModerator)
        ),
        originalModerators = Some(Set(originalModerator))
      )
      assertRefreshResult(result, expectedChanged = false, expectedAssignee = manualAssignee)
    }

    it("does not treat null original moderators as manual assignment evidence") {
      val result = refreshAssignment(
        fixture = AssignmentRefreshFixture(
          assignee = originalModerator,
          currentModerators = Set(currentModerator)
        ),
        originalModerators = None
      )
      assertRefreshResult(result, expectedChanged = true, expectedAssignee = currentModerator)
    }

    it("preserves non-empty assignees when original moderators are empty") {
      val result = refreshAssignment(
        fixture = AssignmentRefreshFixture(
          assignee = manualAssignee,
          currentModerators = Set(currentModerator)
        ),
        originalModerators = Some(Set.empty)
      )
      assertRefreshResult(result, expectedChanged = false, expectedAssignee = manualAssignee)
    }
  }

  private def refreshAssignment(
      fixture: AssignmentRefreshFixture,
      originalModerators: Option[Set[String]]
  ): AssignmentRefreshResult = {
    val task                   = buildTaskStatus(fixture)
    val originalModeratorsJava = originalModerators.map(_.asJava).orNull
    val isAssigneeChanged      = task.reassignIfStale(originalModeratorsJava)
    AssignmentRefreshResult(isAssigneeChanged, task.getAssignedTo)
  }

  private def assertRefreshResult(
      result: AssignmentRefreshResult,
      expectedChanged: Boolean,
      expectedAssignee: String
  ): Unit = {
    result.changed shouldBe expectedChanged
    result.assignee shouldBe expectedAssignee
  }

  private def buildTaskStatus(fixture: AssignmentRefreshFixture): TaskStatus = {
    val workflowItem = buildWorkflowItem(fixture.taskId)
    val statusBean   = buildStatusBean(workflowItem, fixture.assignee)
    val operation    = buildOperation(fixture.currentModerators)
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
