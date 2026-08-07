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

package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.wizard.ContributePage
import com.tle.webtests.test.AbstractSessionTest
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.{PortletFactory, RecentPortlet}
import io.github.openequella.pages.search.ItemStatus
import io.github.openequella.pages.search.ItemStatus.ItemStatus
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.{BeforeClass, BeforeMethod, Test}
import testng.annotation.NewUIOnly

import scala.jdk.CollectionConverters.MapHasAsJava

@NewUIOnly
@TestInstitution("vanilla")
class RecentPortletTest extends AbstractPortletTest {
  var liveItemName: String      = _
  var draftItemName: String     = _
  var itemToQuery: String       = _
  var itemDescription: String   = _
  var recentPortletName: String = _

  var recentPortlet: RecentPortlet = _

  @BeforeClass
  def setupClass(): Unit = {
    liveItemName = context.getFullName("live item")
    draftItemName = context.getFullName("draft item")
    itemToQuery = context.getFullName("query item")
    itemDescription = context.getFullName("query item description")
    recentPortletName = context.getFullName("Recent")

    loginWithPortletAccount()
    loadDashboardPage()
    // Create Recent Contributions portlet
    dashboardPage.createPortlet(PortletType.Recent, recentPortletName)
    // Create items to appear in Recent Contributions portlet
    setupItems()
  }

  @BeforeMethod
  def setupRecentPortletTest(): Unit = loadPortlet()

  @Test(description = "Should be able to see live item")
  def showLiveItem(): Unit = editAndVerifyContribution(status = ItemStatus.LIVE) { portlet =>
    assertTrue(portlet.hasContribution(liveItemName))
  }

  @Test(description = "Should be able to see draft item")
  def showDraftItem(): Unit = editAndVerifyContribution(status = ItemStatus.DRAFT) { portlet =>
    assertTrue(portlet.hasContribution(draftItemName))
  }

  @Test(description = "Should be able to see item with description")
  def showItemWithDescription(): Unit =
    editAndVerifyContribution(status = ItemStatus.LIVE, displayTitleOnly = Some(false)) { portlet =>
      assertTrue(portlet.hasContribution(itemToQuery))
      assertTrue(portlet.hasDescription(itemDescription))
    }

  @Test(description = "Should be able to see item when query is set")
  def showQueryItem(): Unit =
    editAndVerifyContribution(status = ItemStatus.LIVE, query = Some("query item")) { portlet =>
      assertTrue(portlet.hasContribution(itemToQuery))
    }

  @Test(description = "Should hide description when hide description is set")
  def hideDescription(): Unit =
    editAndVerifyContribution(status = ItemStatus.LIVE, displayTitleOnly = Some(true)) { portlet =>
      assertTrue(portlet.hasContribution(itemToQuery))
      assertFalse(portlet.hasContribution(itemDescription))
    }

  // Contribute items.
  private def setupItems(): Unit = {
    createPublishItem(AbstractSessionTest.GENERIC_TESTING_COLLECTION, Map(1 -> liveItemName))

    createDraftItem("Simple Controls Collection", Map(1 -> draftItemName))

    createPublishItem(
      AbstractSessionTest.GENERIC_TESTING_COLLECTION,
      Map(1 -> itemToQuery, 2 -> itemDescription)
    )
  }

  private def toJavaFields(fields: Map[Int, String]): java.util.Map[Integer, String] =
    fields.map { case (k, v) => (Int.box(k), v) }.asJava

  private def createPublishItem(collection: String, fields: Map[Int, String]): Unit = {
    val contributePage = new ContributePage(context).load()
    contributePage.createAndPublishItem(
      collection,
      toJavaFields(fields)
    )
  }

  private def createDraftItem(collection: String = "", fields: Map[Int, String]): Unit = {
    val contributePage = new ContributePage(context).load()
    contributePage.createAndDraftItem(
      collection,
      toJavaFields(fields)
    )
  }

  private def loadPortlet(): Unit = {
    recentPortlet = dashboardPage.getPortlet(PortletFactory.Recent, recentPortletName)
  }

  private def editAndVerifyContribution(
      status: ItemStatus,
      query: Option[String] = None,
      displayTitleOnly: Option[Boolean] = None
  )(assertion: RecentPortlet => Unit): Unit = {
    recentPortlet.edit(Some(status), query, displayTitleOnly)
    loadDashboardPage()
    loadPortlet()
    assertion(recentPortlet)
  }
}
