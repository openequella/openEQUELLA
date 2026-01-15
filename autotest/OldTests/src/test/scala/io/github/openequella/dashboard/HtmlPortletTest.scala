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
import com.tle.webtests.pageobject.portal.HtmlPortalEditPage
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.PortletFactory
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("vanilla")
class HtmlPortletTest extends AbstractPortletTest {
  @Test(description = "Should be able to see expected content")
  def htmlPortlet(): Unit = {
    val portletName = context.getFullName("HTML Portal")
    val portalText  = "A test portal"

    dashboardPage.openCreatePortletPage(PortletType.Html)
    val createPortletPage = new HtmlPortalEditPage(context).get()
    createPortletPage.setTitle(portletName)
    createPortletPage.setText(portalText)
    createPortletPage.save(dashboardPage)

    loadDashboardPage()
    val htmlPortlet = dashboardPage.getPortlet(PortletFactory.Html, portletName)

    assertEquals(htmlPortlet.getHtmlText, portalText)
  }
}
