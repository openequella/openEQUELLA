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
import com.tle.webtests.pageobject.searching.ModerateListSearchResults
import com.tle.webtests.pageobject.tasklist.ModerationView
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
      contribute(singleModeratorCollection, moderatorA)

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(item.name)
    }

    replaceModerator(item.name, moderatorA, moderatorB)

    withLoggedInUser(moderatorA) {
      assertTaskNotPresent(item)
    }
    assertAssignedToViaRest(item, moderatorB)
    assertModeratorCanAcceptTask(item, moderatorB)
  }

  @Test
  def testKeepsSystemAdminManualAssignee(): Unit = {
    val item =
      contribute(singleModeratorCollection, moderatorA)

    withAdmin {
      val view = openModerationViewForCurrentUser(item.name)
      assertNotAssignedToMe(
        view,
        s"item ${item.name} should not be assigned to the admin before they manually assign it to themselves"
      )
      view.assignToMe()
      assertAssignedToMe(item.name)
    }

    replaceModerator(item.name, moderatorA, moderatorB)

    withAdmin {
      assertAssignedToMe(item.name)
    }
  }

  @Test
  def testKeepsAssigneeWhenModeratorAdded(): Unit = {
    val item =
      contribute(multiModeratorCollection, moderatorA)

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(item.name)
    }

    addModerator(item.name, moderatorB)

    withLoggedInUser(moderatorB) {
      val view = openModerationViewForCurrentUser(item.name)
      assertNotAssignedToMe(
        view,
        s"item ${item.name} should stay with ${moderatorA.username} after adding ${moderatorB.username}"
      )
      assertEquals(view.getAssignedTo, moderatorA.fullName.get)
    }
  }

  @Test
  def testReassignsAfterRestEdit(): Unit = {
    val item =
      contribute(singleModeratorCollection, moderatorA)

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(item.name)
    }

    withRestClient(contributor) { rest =>
      rest.editMetadata(item.id)(_.setNode(moderatorMetadataPath, requiredUuid(moderatorB)))
    }

    assertAssignedToViaRest(item, moderatorB)
    withLoggedInUser(moderatorA) {
      assertTaskNotPresent(item)
    }
    withLoggedInUser(moderatorB) {
      assertAssignedToMe(item.name)
    }
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
  private def assertModeratorCanAcceptTask(item: ContributedItem, moderator: TestActor): Unit =
    withLoggedInUser(moderator) {
      val results = searchExactTask(item.name)
      assertTaskAssignedToCurrentUser(results, item)

      val view = results.moderate(item.name)
      assertTaskCanBeModerated(view, item, moderator)

      view.accept()
      assertTaskNotPresent(item)
    }

  /** Assert the task-list search result for `item` shows it assigned to the current user. */
  private def assertTaskAssignedToCurrentUser(
      results: ModerateListSearchResults,
      item: ContributedItem
  ): Unit =
    assertEquals(
      results.getResultForTitle(item.name).getDetailText(ASSIGNED_TO_LABEL),
      ME_LABEL
    )

  /** Assert `moderator` can moderate `item`'s task in the given moderation `view`. */
  private def assertTaskCanBeModerated(
      view: ModerationView,
      item: ContributedItem,
      moderator: TestActor
  ): Unit = {
    assertAssignedToMe(view)
    assertFalse(
      view.moderationDisabled(),
      s"${moderator.username} should be able to moderate item ${item.name}"
    )
  }

  /** Assert no moderation task remains for `item`. */
  private def assertTaskNotPresent(item: ContributedItem): Unit =
    assertNoTaskResults(searchExactTask(item.name))
}
