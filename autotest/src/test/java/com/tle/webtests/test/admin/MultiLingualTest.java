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

package com.tle.webtests.test.admin;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.CustomLinksEditPage;
import com.tle.webtests.pageobject.CustomLinksPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;
import com.tle.webtests.pageobject.portal.BrowsePortalEditPage;
import com.tle.webtests.pageobject.portal.BrowsePortalSection;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.portal.TopbarMenuSection;
import com.tle.webtests.test.AbstractCleanupTest;
import io.github.openequella.pages.dashboard.DashboardPage;
import io.github.openequella.pages.dashboard.PortletType$;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import testng.annotation.NewUIOnly;
import testng.annotation.OldUIOnly;

@TestInstitution("fiveo")
public class MultiLingualTest extends AbstractCleanupTest {

  private static final String NOT_ENGLISH = "This is not English...";
  private static final String ENGLISH = "This is English";

  @Override
  public HomePage logon() {
    return logon("LanguageUser", "``````");
  }

  @Test
  @OldUIOnly
  public void portletLanguage() {
    logon();

    // Create a portlet with multi-language title.
    new HomePage(context).load().addPortal(new BrowsePortalEditPage(context));
    HomePage home = setupMultiLanguageTitleOldUI();

    assertTrue(home.portalExists(ENGLISH));
    assertFalse(home.portalExists(NOT_ENGLISH));

    setupUserLanguage("aa_DJ");

    logon();
    home = new MenuSection(context).get().home();

    assertFalse(home.portalExists(ENGLISH));
    assertTrue(home.portalExists(NOT_ENGLISH));

    setupUserLanguage("en_AU");

    logon();
    home = new MenuSection(context).get().home();
    assertTrue(home.portalExists(ENGLISH));
    assertFalse(home.portalExists(NOT_ENGLISH));

    setupUserLanguage("");

    logon();
  }

  @Test
  @NewUIOnly
  public void portletLanguageNewUi() {
    logon();

    DashboardPage page = new DashboardPage(context).get();
    // Create a portlet with multi-language title.
    page.openCreatePortletPage(PortletType$.MODULE$.Browse());
    DashboardPage dashboardPage = setupMultiLanguageTitleNewUI();
    dashboardPage.get();

    assertTrue(dashboardPage.hasPortlet(ENGLISH));
    assertFalse(dashboardPage.hasPortlet(NOT_ENGLISH));

    setupUserLanguage("aa_DJ");

    dashboardPage = new DashboardPage(context).load();
    assertFalse(dashboardPage.hasPortlet(ENGLISH));
    assertTrue(dashboardPage.hasPortlet(NOT_ENGLISH));

    setupUserLanguage("en_AU");

    dashboardPage = new DashboardPage(context).load();
    assertTrue(dashboardPage.hasPortlet(ENGLISH));
    assertFalse(dashboardPage.hasPortlet(NOT_ENGLISH));

    setupUserLanguage("");
  }

  @Test
  public void customLinksLanguage() {
    logon();
    CustomLinksPage linksPage = new CustomLinksPage(context).load();
    CustomLinksEditPage editLinkPage = linksPage.newLink();
    MultiLingualEditbox multiLang = editLinkPage.getTitleSection();
    multiLang.allMode();
    multiLang.editLangString("English", ENGLISH);
    multiLang.editLangString("Afar", NOT_ENGLISH);
    editLinkPage.setUrl(context.getBaseUrl());
    linksPage = editLinkPage.save(ENGLISH);

    UserProfilePage details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode("en_AU");
    details.saveSuccesful();
    logon();
    MenuSection menu = new MenuSection(context).get();

    assertTrue(menu.hasMenuOption(ENGLISH));
    assertFalse(menu.hasMenuOption(NOT_ENGLISH));

    details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode("aa_DJ");
    details.saveSuccesful();
    logon();
    menu = new MenuSection(context).get();

    assertFalse(menu.hasMenuOption(ENGLISH));
    assertTrue(menu.hasMenuOption(NOT_ENGLISH));

    details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode("");
    details.saveSuccesful();

    logon();
    menu = new MenuSection(context).get();

    assertTrue(menu.hasMenuOption(ENGLISH));
    assertFalse(menu.hasMenuOption(NOT_ENGLISH));
  }

  @AfterMethod
  public void resetLanguage() throws Exception {
    logon();
    UserProfilePage myDetails = new TopbarMenuSection(context).get().editMyDetails();
    myDetails.setLanguageByCode("");
    myDetails.saveSuccesful();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon();
    HomePage home = new HomePage(context).load();
    if (home.portalExists(ENGLISH)) {
      new BrowsePortalSection(context, ENGLISH).get().delete();
    }
    if (home.portalExists(NOT_ENGLISH)) {
      new BrowsePortalSection(context, NOT_ENGLISH).get().delete();
    }

    CustomLinksPage linksPage = new CustomLinksPage(context).load();
    while (linksPage.linkExists(ENGLISH, context.getBaseUrl())) {
      linksPage.deleteLink(ENGLISH, context.getBaseUrl());
    }
    while (linksPage.linkExists(NOT_ENGLISH, context.getBaseUrl())) {
      linksPage.deleteLink(NOT_ENGLISH, context.getBaseUrl());
    }
    super.cleanupAfterClass();
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  private HomePage setupMultiLanguageTitleOldUI() {
    return new BrowsePortalEditPage(context)
        .get()
        .setMultiLanguageTitle(ENGLISH, NOT_ENGLISH)
        .save(new HomePage(context));
  }

  private DashboardPage setupMultiLanguageTitleNewUI() {
    return new BrowsePortalEditPage(context)
        .get()
        .setMultiLanguageTitle(ENGLISH, NOT_ENGLISH)
        .save(new DashboardPage(context));
  }

  private void setupUserLanguage(String languageCode) {
    UserProfilePage details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode(languageCode);
    details.saveSuccesful();
  }
}
