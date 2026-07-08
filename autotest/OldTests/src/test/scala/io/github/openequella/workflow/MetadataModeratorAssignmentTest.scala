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

  @Test
  def testReassignsWhenModeratorChanges(): Unit = {
    val item =
      contribute(ContributeItemFlow(singleModeratorCollection, moderatorA))

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }

    replaceModerator(ReplaceModeratorFlow(item.name, moderatorA, moderatorB))

    withLoggedInUser(moderatorA) {
      val taskList = new TaskListPage(context).load()
      assertNoTaskResults(taskList.exactQuery(item.name))
    }

    withRestClient(contributor) { rest =>
      assertEquals(
        rest.getTaskAssignee(item.id, reviewDecisionsTaskUuid),
        Some(requiredUuid(moderatorB))
      )
    }

    withLoggedInUser(moderatorB) {
      val results = searchExactTask(item.name)
      assertEquals(results.getResultForTitle(item.name).getDetailText("Assigned to"), "Me")

      val view = results.moderate(item.name)
      assertAssignedToMe(view)
      assertEquals(view.getAssignedTo, "Me")
      assertFalse(view.moderationDisabled())
      view.accept()
      assertNoTaskResults(searchExactTask(item.name))
    }
  }

  @Test
  def testKeepsSystemAdminManualAssignee(): Unit = {
    val item =
      contribute(ContributeItemFlow(singleModeratorCollection, moderatorA))

    withAdmin {
      val view = openModerationViewForCurrentUser(item.name)
      assertFalse(view.isAssignedToMe)
      view.assignToMe()
    }

    replaceModerator(ReplaceModeratorFlow(item.name, moderatorA, moderatorB))

    withAdmin {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }
  }

  @Test
  def testKeepsAssigneeWhenModeratorAdded(): Unit = {
    val item =
      contribute(ContributeItemFlow(multiModeratorCollection, moderatorA))

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }

    addModerator(AddModeratorFlow(item.name, moderatorB))

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }
    withLoggedInUser(moderatorB) {
      val view = openModerationViewForCurrentUser(item.name)
      assertFalse(view.isAssignedToMe)
    }
  }

  @Test
  def testReassignsAfterRestEdit(): Unit = {
    val item =
      contribute(ContributeItemFlow(singleModeratorCollection, moderatorA))

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }

    withRestClient(contributor) { rest =>
      rest.editMetadata(item.id)(_.setNode(moderatorMetadataPath, requiredUuid(moderatorB)))
      assertEquals(
        rest.getTaskAssignee(item.id, reviewDecisionsTaskUuid),
        Some(requiredUuid(moderatorB))
      )
    }

    withLoggedInUser(moderatorA) {
      assertNoTaskResults(searchExactTask(item.name))
    }
    withLoggedInUser(moderatorB) {
      assertAssignedToMe(openModerationViewForCurrentUser(item.name))
    }
  }
}
