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
import com.tle.webtests.pageobject.viewitem.ItemId
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
    val itemName = item.name

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(itemName)
    }

    replaceModerator(itemName, moderatorA, moderatorB)

    withLoggedInUser(moderatorA) {
      assertTaskNotPresent(itemName)
    }
    assertAssignedToViaRest(item.id, moderatorB)
    withLoggedInUser(moderatorB) {
      assertCanModerateAndAcceptTask(itemName)
    }
  }

  @Test
  def testKeepsSystemAdminManualAssignee(): Unit = {
    val item =
      contribute(singleModeratorCollection, moderatorA)
    val itemName = item.name

    withAdmin {
      assertNotAssignedToMe(
        itemName,
        s"item $itemName should not be assigned to the admin before they manually assign it to themselves"
      )
      assignToMe(itemName)
      assertAssignedToMe(itemName)
    }

    replaceModerator(itemName, moderatorA, moderatorB)

    withAdmin {
      assertAssignedToMe(itemName)
    }
  }

  @Test
  def testKeepsAssigneeWhenModeratorAdded(): Unit = {
    val item =
      contribute(multiModeratorCollection, moderatorA)
    val itemName = item.name

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(itemName)
    }

    addModerator(itemName, moderatorB)

    withLoggedInUser(moderatorB) {
      assertNotAssignedToMe(
        itemName,
        s"item $itemName should stay with ${moderatorA.username} after adding ${moderatorB.username}"
      )
      assertEquals(
        openModerationViewForCurrentUser(itemName).getAssignedTo,
        moderatorA.fullName.get
      )
    }
  }

  @Test
  def testReassignsAfterRestEdit(): Unit = {
    val item =
      contribute(singleModeratorCollection, moderatorA)
    val itemName = item.name

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(itemName)
    }

    withRestClient(contributor) { rest =>
      rest.editMetadata(item.id)(_.setNode(moderatorMetadataPath, requiredUuid(moderatorB)))
    }

    assertAssignedToViaRest(item.id, moderatorB)
    withLoggedInUser(moderatorA) {
      assertTaskNotPresent(itemName)
    }
    withLoggedInUser(moderatorB) {
      assertAssignedToMe(itemName)
    }
  }

  /** Assert, via the REST API, that the item's review task is assigned to `moderator`. */
  private def assertAssignedToViaRest(itemId: ItemId, moderator: TestActor): Unit =
    withRestClient(contributor) { rest =>
      assertEquals(
        rest.getTaskAssignee(itemId, reviewDecisionsTaskUuid),
        Some(requiredUuid(moderator))
      )
    }

  /** Verify the current user can take ownership of the item's task, moderate it, and that it leaves
    * their task list once accepted.
    */
  private def assertCanModerateAndAcceptTask(itemName: String): Unit = {
    val results = searchExactTask(itemName)
    assertTaskAssignedToCurrentUser(results, itemName)

    val view = results.moderate(itemName)
    assertTaskCanBeModerated(view, itemName)

    view.accept()
    assertTaskNotPresent(itemName)
  }

  /** Assert the task-list search result for `item` shows it assigned to the current user. */
  private def assertTaskAssignedToCurrentUser(
      results: ModerateListSearchResults,
      itemName: String
  ): Unit =
    assertEquals(
      results.getResultForTitle(itemName).getDetailText(ASSIGNED_TO_LABEL),
      ME_LABEL
    )

  /** Assert the current user can moderate `item`'s task in the given moderation `view`. */
  private def assertTaskCanBeModerated(view: ModerationView, itemName: String): Unit = {
    assertAssignedToMe(itemName)
    assertFalse(
      view.moderationDisabled(),
      s"Current user should be able to moderate item ${itemName}"
    )
  }

  /** Assert no moderation task remains for `item`. */
  private def assertTaskNotPresent(itemName: String): Unit =
    assertNoTaskResults(searchExactTask(itemName))
}
