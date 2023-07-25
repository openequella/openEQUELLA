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

package io.github.openequella.pages.lti13;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.LTI13PlatformsSettingsPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class LTI13PlatformsSettingsTest extends AbstractCleanupAutoTest {
  @Test(description = "User should be able to navigate to LTI 1.3 settings page.")
  public void testNavigation() {
    SettingsPage sp = new SettingsPage(context).load();

    // Load LTI1.3PlatformsSettings Page by clicking settings link
    LTI13PlatformsSettingsPage lti13PlatformsSettingsPage = sp.lti13PlatformsSettingsPage();

    assertTrue(lti13PlatformsSettingsPage.isLoaded());
  }

  @Test(description = "User without permission can't access LTI 1.3 settings page.")
  public void testUnauthorisedAccess() {
    logon(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD);

    SettingsPage sp = new SettingsPage(context).load();

    assertFalse(sp.isSettingVisible(LTI13PlatformsSettingsPage.TITLE));
  }
}
