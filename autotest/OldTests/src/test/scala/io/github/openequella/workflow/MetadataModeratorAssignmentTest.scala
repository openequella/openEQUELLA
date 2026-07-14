/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.workflow

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.tasklist.TaskListPage
import org.testng.Assert.{assertEquals, assertFalse}
import org.testng.annotations.Test

@TestInstitution("workflow")
class MetadataModeratorAssignmentTest extends AbstractAutoAssignTest {

  import AutoAssignTestData._

  private val ASSIGNED_TO_LABEL = "Assigned to"
  private val ME_LABEL          = "Me"

  @Test
  def testReassignsWhenModeratorChanges(): Unit = {
    val item =
      contribute(ContributeItemFlow(singleModeratorCollection, moderatorA))

    assertAssignedToViaUi(item, moderatorA)
    replaceModerator(ReplaceModeratorFlow(item.name, moderatorA, moderatorB))
    assertNoTaskVisible(item, moderatorA)
    assertAssignedToViaRest(item, moderatorB)
    assertModeratorCanModerateAndAcceptTask(item, moderatorB)
  }

  @Test
  def testKeepsSystemAdminManualAssignee(): Unit = {
    val item =
      contribute(ContributeItemFlow(singleModeratorCollection, moderatorA))

    withAdmin {
      val view = openModerationViewForCurrentUser(item.name)
      assertFalse(
        view.isAssignedToMe,
        s"item ${item.name} should not be assigned to the admin before they manually assign it to themselves"
      )
      view.assignToMe()
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }

    replaceModerator(ReplaceModeratorFlow(item.name, moderatorA, moderatorB))
    withAdmin {
      val view = openModerationViewForCurrentUser(item.name)
      assertAssignedToMe(view)
    }
  }

  @Test
  def testKeepsAssigneeWhenModeratorAdded(): Unit = {
    val item =
      contribute(ContributeItemFlow(multiModeratorCollection, moderatorA))

    assertAssignedToViaUi(item, moderatorA)

    addModerator(AddModeratorFlow(item.name, moderatorB))

    withLoggedInUser(moderatorB) {
      val view = openModerationViewForCurrentUser(item.name)
      assertFalse(
        view.isAssignedToMe,
        s"item ${item.name} should stay with ${moderatorA.username} after adding ${moderatorB.username}"
      )
      assertEquals(view.getAssignedTo, moderatorA.fullName.get)
    }
  }

  @Test
  def testReassignsAfterRestEdit(): Unit = {
    val item =
      contribute(ContributeItemFlow(singleModeratorCollection, moderatorA))

    assertAssignedToViaUi(item, moderatorA)

    withRestClient(contributor) { rest =>
      rest.editMetadata(item.id)(_.setNode(moderatorMetadataPath, requiredUuid(moderatorB)))
    }

    assertAssignedToViaRest(item, moderatorB)
    assertNoTaskVisible(item, moderatorA)
    assertAssignedToViaUi(item, moderatorB)
  }

  /** Log in as `moderator` and assert the item's task is assigned to them. */
  private def assertAssignedToViaUi(item: ContributedItem, moderator: TestActor): Unit =
    withLoggedInUser(moderator) {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }

  /** Log in as `moderator` and assert that no moderation task for the item is visible to them. */
  private def assertNoTaskVisible(item: ContributedItem, moderator: TestActor): Unit =
    withLoggedInUser(moderator) {
      val taskList = new TaskListPage(context).load()
      assertNoTaskResults(taskList.exactQuery(item.name))
    }

  /** Assert, via the REST API, that the item's review task is assigned to `moderator`. */
  private def assertAssignedToViaRest(item: ContributedItem, moderator: TestActor): Unit =
    withRestClient(contributor) { rest =>
      assertEquals(
        rest.getTaskAssignee(item.id, reviewDecisionsTaskUuid),
        Some(requiredUuid(moderator))
      )
    }

  /** Log in as `moderator` and verify they can take ownership of the item's task, moderate it, and
    * that it leaves their task list once accepted.
    */
  private def assertModeratorCanModerateAndAcceptTask(
      item: ContributedItem,
      moderator: TestActor
  ): Unit =
    withLoggedInUser(moderator) {
      val results = searchExactTask(item.name)
      assertEquals(
        results.getResultForTitle(item.name).getDetailText(ASSIGNED_TO_LABEL),
        ME_LABEL
      )

      val view = results.moderate(item.name)
      assertAssignedToMe(view)
      assertFalse(
        view.moderationDisabled(),
        s"${moderator.username} should be able to moderate item ${item.name}"
      )
      view.accept()
      assertNoTaskResults(searchExactTask(item.name))
    }
}
