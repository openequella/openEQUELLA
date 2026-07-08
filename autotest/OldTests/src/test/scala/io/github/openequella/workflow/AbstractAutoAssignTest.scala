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

import com.tle.webtests.pageobject.searching.{ModerateListSearchResults, SearchPage}
import com.tle.webtests.pageobject.tasklist.{ModerationView, TaskListPage}
import com.tle.webtests.pageobject.viewitem.ItemId
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl
import com.tle.webtests.pageobject.wizard.{ContributePage, WizardPageTab}
import com.tle.webtests.test.AbstractCleanupTest
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.AfterMethod

object AutoAssignTestData {
  val password = "``````"

  /** Collection whose workflow auto-assigns via the task's autoAssignNode metadata path. */
  val autoAssignByMetadataCollection = "Auto Assign By Metadata Collection"

  /** Collection whose workflow selects one moderator from the moderator metadata path. */
  val singleModeratorCollection =
    "Single Moderator Selection By Metadata Collection"

  /** Collection whose workflow selects multiple moderators from the moderator metadata path. */
  val multiModeratorCollection =
    "Multiple Moderators Selection By Metadata Collection"

  /** UUID of the single task ("Review Decisions") in the workflow used by
    * [[singleModeratorCollection]].
    */
  val reviewDecisionsTaskUuid = "d1e0a49a-86f4-4533-8ba7-a17f90129f01"

  /** Metadata path the moderator user-selectors write to. */
  val moderatorMetadataPath = "item/workflow/moderatorID"

  /** The system super user, who can moderate any task regardless of the moderator metadata. */
  val adminUsername = "TLE_ADMINISTRATOR"

  val contributor: TestActor = TestActor("AutoAssignContributor")

  val moderatorA: TestActor = TestActor(
    "AutoAssignTarget",
    query = Some("autoassigntarget"),
    uuid = Some("43313ddd-5d4e-97bb-689d-7e149b252129")
  )

  val moderatorB: TestActor = TestActor(
    "AutoAssignTarget2",
    query = Some("autoassigntarget2"),
    uuid = Some("b6c0f662-0c32-438b-b5a4-38e979212087")
  )
}

object AutoAssignWizardControls {

  /** Title field on the Data wizard page. */
  val itemNameEditBoxIndex = 1

  /** Moderator user-selector on the Data wizard page (third control). */
  val moderatorSelectUserIndex = 3
}

/** Test user with optional wizard search text and user UUID for metadata moderator selection. */
case class TestActor(
    username: String,
    password: String = AutoAssignTestData.password,
    query: Option[String] = None,
    uuid: Option[String] = None
)

/** Collection and metadata moderator used when contributing a new moderated item. */
case class ContributeItemFlow(collection: String, moderator: TestActor)

/** Inputs for changing a metadata-selected moderator on an existing item. */
case class ReplaceModeratorFlow(itemName: String, from: TestActor, to: TestActor)

/** Inputs for adding a metadata-selected moderator without removing the existing one. */
case class AddModeratorFlow(itemName: String, add: TestActor)

/** A contributed item: its (full) name and its ID for use with the REST API. */
case class ContributedItem(name: String, id: ItemId)

/** Common flows and assertions for the metadata-driven ("auto assign") workflow moderation tests.
  */
abstract class AbstractAutoAssignTest extends AbstractCleanupTest {

  import AutoAssignTestData._

  setDeleteCredentials(contributor.username, contributor.password)

  @AfterMethod(alwaysRun = true)
  def ensureLoggedOut(): Unit =
    logout()

  protected def withLoggedInUser[T](actor: TestActor)(function: => T): T =
    try {
      logon(actor.username, actor.password)
      function
    } finally {
      logout()
    }

  protected def withAdmin[T](function: => T): T =
    try {
      logon(adminUsername, testConfig.getAdminPassword)
      function
    } finally {
      logout()
    }

  /** Run `function` against a REST client authenticated (by session) as `actor`. */
  protected def withRestClient[T](actor: TestActor)(function: WorkflowRestClient => T): T = {
    val client = new WorkflowRestClient(context.getBaseUrl)
    client.login(actor.username, actor.password)
    try function(client)
    finally client.logout()
  }

  protected def contribute(flow: ContributeItemFlow): ContributedItem = {
    val moderatorQuery = requiredQuery(flow.moderator)

    withLoggedInUser(contributor) {
      val wizard   = new ContributePage(context).load().openWizard(flow.collection)
      val itemName = context.getFullName("item")
      setItemName(wizard, itemName)
      moderatorControl(wizard).queryAndSelect(moderatorQuery, flow.moderator.username)
      val summary = wizard.save().submit()
      ContributedItem(itemName, summary.getItemId)
    }
  }

  protected def replaceModerator(flow: ReplaceModeratorFlow): Unit = {
    val newModeratorQuery = requiredQuery(flow.to)

    withLoggedInUser(contributor) {
      val wizard        = openItemWizardForEdit(flow.itemName)
      val moderatorList = moderatorControl(wizard)
      moderatorList.removeUser(flow.from.username)
      moderatorList.queryAndSelect(newModeratorQuery, flow.to.username)
      wizard.saveNoConfirm()
    }
  }

  protected def addModerator(flow: AddModeratorFlow): Unit = {
    val newModeratorQuery = requiredQuery(flow.add)

    withLoggedInUser(contributor) {
      val wizard = openItemWizardForEdit(flow.itemName)
      moderatorControl(wizard).queryAndSelect(newModeratorQuery, flow.add.username)
      wizard.saveNoConfirm()
    }
  }

  protected def openItemWizardForEdit(itemName: String): WizardPageTab = {
    val searchPage = new SearchPage(context).load()
    searchPage.setIncludeNonLive(true)
    searchPage
      .search(s""""$itemName"""")
      .viewFromTitle(itemName)
      .adminTab()
      .edit()
  }

  protected def requiredQuery(actor: TestActor): String =
    actor.query.getOrElse(
      throw new IllegalArgumentException(s"Moderator query required: ${actor.username}")
    )

  protected def requiredUuid(actor: TestActor): String =
    actor.uuid.getOrElse(
      throw new IllegalArgumentException(s"User UUID required: ${actor.username}")
    )

  protected def setItemName(wizard: WizardPageTab, itemName: String): Unit =
    wizard.editbox(AutoAssignWizardControls.itemNameEditBoxIndex, itemName)

  protected def moderatorControl(wizard: WizardPageTab): SelectUserControl =
    wizard.selectUser(AutoAssignWizardControls.moderatorSelectUserIndex)

  protected def searchExactTask(itemName: String): ModerateListSearchResults =
    new TaskListPage(context).load().exactQuery(itemName)

  protected def openModerationViewForCurrentUser(itemName: String): ModerationView =
    searchExactTask(itemName).moderate(itemName)

  protected def assertAssignedToMe(view: ModerationView): Unit =
    assertTrue(view.isAssignedToMe)

  protected def assertNoTaskResults(results: ModerateListSearchResults): Unit =
    assertFalse(results.isResultsAvailable)
}
