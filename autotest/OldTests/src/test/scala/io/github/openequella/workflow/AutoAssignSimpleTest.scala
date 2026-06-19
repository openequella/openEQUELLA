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
import com.tle.webtests.pageobject.searching.{ModerateListSearchResults, SearchPage}
import com.tle.webtests.pageobject.tasklist.{ModerationView, TaskListPage}
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl
import com.tle.webtests.pageobject.wizard.{ContributePage, WizardPageTab}
import com.tle.webtests.test.AbstractCleanupTest
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.{AfterMethod, Test}

object AutoAssignTestData {
  val password                       = "``````"
  val autoAssignByMetadataCollection = "Auto Assign By Metadata Collection"
  val autoAssignRefreshCollection    = "Auto Assign Refresh Collection"

  val contributor: TestActor = TestActor("AutoAssignContributor")
  val moderatorA: TestActor  = TestActor("AutoAssignTarget", query = Some("autoassigntarget"))
  val moderatorB: TestActor  = TestActor("AutoAssignTarget2", query = Some("autoassigntarget2"))
}

object AutoAssignWizardControls {

  /** Title field on the Data wizard page. */
  val itemNameEditBoxIndex = 1

  /** Moderator user-selector on the Data wizard page (third control). */
  val moderatorSelectUserIndex = 3
}

/** Test user with optional wizard search text for metadata moderator selection. */
case class TestActor(
    username: String,
    password: String = AutoAssignTestData.password,
    query: Option[String] = None
)

/** Collection and metadata moderator used when contributing a new moderated item. */
case class ContributeItemFlow(collection: String, moderator: TestActor)

/** Inputs for changing a metadata-selected moderator on an existing item. */
case class ReplaceModeratorFlow(itemName: String, from: TestActor, to: TestActor)

@TestInstitution("workflow")
class AutoAssignSimpleTest extends AbstractCleanupTest {

  import AutoAssignTestData._

  setDeleteCredentials(contributor.username, contributor.password)

  @AfterMethod(alwaysRun = true)
  def ensureLoggedOut(): Unit =
    logout()

  @Test
  def testAutoAssign(): Unit = {
    val itemName = contribute(ContributeItemFlow(autoAssignByMetadataCollection, moderatorA))
    withLoggedInUser(moderatorA) {
      val view = openModerationViewForCurrentUser(itemName)
      assertAssignedToMe(view)
    }
  }

  @Test
  def testAutoAssignRefreshAfterModeratorChange(): Unit = {
    val itemName = contribute(ContributeItemFlow(autoAssignRefreshCollection, moderatorA))

    withLoggedInUser(moderatorA) {
      assertAssignedToMe(openModerationViewForCurrentUser(itemName))
    }

    replaceModerator(ReplaceModeratorFlow(itemName, moderatorA, moderatorB))

    withLoggedInUser(moderatorA) {
      assertNoTaskResults(searchExactTask(itemName))
    }

    withLoggedInUser(moderatorB) {
      val view = openModerationViewForCurrentUser(itemName)
      assertAssignedToMe(view)
      view.accept()
    }
  }

  private def withLoggedInUser[T](actor: TestActor)(function: => T): T =
    try {
      logon(actor.username, actor.password)
      function
    } finally {
      logout()
    }

  private def contribute(flow: ContributeItemFlow): String = {
    val moderatorQuery = flow.moderator.query.getOrElse(
      throw new IllegalArgumentException(s"Moderator query required: ${flow.moderator.username}")
    )

    withLoggedInUser(contributor) {
      val wizard   = new ContributePage(context).load().openWizard(flow.collection)
      val itemName = context.getFullName("item")
      setItemName(wizard, itemName)
      moderatorControl(wizard).queryAndSelect(moderatorQuery, flow.moderator.username)
      wizard.save().submit()
      itemName
    }
  }

  private def replaceModerator(flow: ReplaceModeratorFlow): Unit = {
    val newModeratorQuery = flow.to.query.getOrElse(
      throw new IllegalArgumentException(s"Moderator query required: ${flow.to.username}")
    )

    withLoggedInUser(contributor) {
      val searchPage = new SearchPage(context).load()
      searchPage.setIncludeNonLive(true)
      val wizard: WizardPageTab =
        searchPage
          .search(s""""${flow.itemName}"""")
          .viewFromTitle(flow.itemName)
          .adminTab()
          .edit()
      val moderatorList = moderatorControl(wizard)
      moderatorList.removeUser(flow.from.username)
      moderatorList.queryAndSelect(newModeratorQuery, flow.to.username)
      wizard.saveNoConfirm()
    }
  }

  private def setItemName(wizard: WizardPageTab, itemName: String): Unit =
    wizard.editbox(AutoAssignWizardControls.itemNameEditBoxIndex, itemName)

  private def moderatorControl(wizard: WizardPageTab): SelectUserControl =
    wizard.selectUser(AutoAssignWizardControls.moderatorSelectUserIndex)

  private def searchExactTask(itemName: String): ModerateListSearchResults =
    new TaskListPage(context).load().exactQuery(itemName)

  private def openModerationViewForCurrentUser(itemName: String): ModerationView =
    searchExactTask(itemName).moderate(itemName)

  private def assertAssignedToMe(view: ModerationView): Unit =
    assertTrue(view.isAssignedToMe)

  private def assertNoTaskResults(results: ModerateListSearchResults): Unit =
    assertFalse(results.isResultsAvailable)
}
