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

  /** Shared password for all test users in the "workflow" institution fixture. */
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
    uuid = Some("43313ddd-5d4e-97bb-689d-7e149b252129"),
    fullName = Some("AutoAssignTarget AutoAssignTarget")
  )

  val moderatorB: TestActor = TestActor(
    "AutoAssignTarget2",
    query = Some("autoassigntarget2"),
    uuid = Some("b6c0f662-0c32-438b-b5a4-38e979212087")
  )
}

object AutoAssignWizardControls {

  /** Title field on the Data wizard page (first text input control, index 1). */
  val itemNameEditBoxIndex = 1

  /** Moderator user-selector on the Data wizard page (third control after title and description).
    */
  val moderatorSelectUserIndex = 3
}

/** Test user with optional wizard search text and user UUID for metadata moderator selection. */
case class TestActor(
    username: String,
    password: String = AutoAssignTestData.password,
    query: Option[String] = None,
    uuid: Option[String] = None,
    fullName: Option[String] = None
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

  /** Best-effort logout after every test method so a failed test cannot leave a stale session that
    * would break the next one. Any logout failure (e.g. already logged out) is ignored.
    */
  @AfterMethod(alwaysRun = true)
  def ensureLoggedOut(): Unit = {
    try {
      logout()
    } catch {
      case _: Exception =>
    }
  }

  /** Run `function` while logged in as `actor`, guaranteeing a logout afterwards even on failure.
    */
  protected def withLoggedInUser[T](actor: TestActor)(function: => T): T =
    try {
      logon(actor.username, actor.password)
      function
    } finally {
      logout()
    }

  /** Run `function` while logged in as the system super user, guaranteeing a logout afterwards. */
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

  /** Contribute (and submit for moderation) a new item as the contributor, selecting
    * `flow.moderator` in the metadata moderator control. Returns the contributed item's name and
    * ID.
    */
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

  /** Edit the item as the contributor, removing `flow.from` and selecting `flow.to` in the metadata
    * moderator control (i.e. swap the metadata-selected moderator).
    */
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

  /** Edit the item as the contributor, adding `flow.add` to the metadata moderator control without
    * removing the existing moderator.
    */
  protected def addModerator(flow: AddModeratorFlow): Unit = {
    val newModeratorQuery = requiredQuery(flow.add)

    withLoggedInUser(contributor) {
      val wizard = openItemWizardForEdit(flow.itemName)
      moderatorControl(wizard).queryAndSelect(newModeratorQuery, flow.add.username)
      wizard.saveNoConfirm()
    }
  }

  /** Search (including non-live results) for the item by exact name and open its admin edit wizard.
    */
  protected def openItemWizardForEdit(itemName: String): WizardPageTab = {
    val searchPage = new SearchPage(context).load()
    searchPage.setIncludeNonLive(true)
    searchPage
      .search(s""""$itemName"""")
      .viewFromTitle(itemName)
      .adminTab()
      .edit()
  }

  /** Returns the actor's wizard search text, or fail if the actor was defined without one. */
  protected def requiredQuery(actor: TestActor): String =
    actor.query.getOrElse(
      throw new IllegalArgumentException(s"Moderator query required: ${actor.username}")
    )

  /** Returns the actor's user UUID, or fail if the actor was defined without one. */
  protected def requiredUuid(actor: TestActor): String =
    actor.uuid.getOrElse(
      throw new IllegalArgumentException(s"User UUID required: ${actor.username}")
    )

  /** Set the item title in the wizard's name edit box. */
  protected def setItemName(wizard: WizardPageTab, itemName: String): Unit =
    wizard.editbox(AutoAssignWizardControls.itemNameEditBoxIndex, itemName)

  /** The metadata moderator user-selector control on the wizard's Data page. */
  protected def moderatorControl(wizard: WizardPageTab): SelectUserControl =
    wizard.selectUser(AutoAssignWizardControls.moderatorSelectUserIndex)

  /** Search the current user's task list for tasks whose item exactly matches `itemName`. */
  protected def searchExactTask(itemName: String): ModerateListSearchResults =
    new TaskListPage(context).load().exactQuery(itemName)

  /** Open the moderation view for `itemName` from the current user's task list. */
  protected def openModerationViewForCurrentUser(itemName: String): ModerationView =
    searchExactTask(itemName).moderate(itemName)

  /** Assert the task shown in moderation view is assigned to the current user. */
  protected def assertAssignedToMe(view: ModerationView): Unit =
    assertTrue(view.isAssignedToMe, "Expected task to be assigned to the current user")

  /** Assert the task-list search returned no matching moderation tasks. */
  protected def assertNoTaskResults(results: ModerateListSearchResults): Unit =
    assertFalse(results.isResultsAvailable, "Expected no matching moderation tasks")
}
