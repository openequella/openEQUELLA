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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import testng.annotation.OldUIOnly;

@TestInstitution("fiveo")
public class MultiLingualTest extends AbstractCleanupTest {

  private static final String NOT_ENGLISH = "This is not English...";
  private static final String ENGLISH = "This is English";

  @Override
  public HomePage logon() {
    return logon("LanguageUser", "``````");
  }

  // TODO: OEQ-2720 enable test in new UI.
  @Test
  @OldUIOnly
  public void portletLanguage() {
    logon();

    BrowsePortalEditPage edit =
        new HomePage(context).load().addPortal(new BrowsePortalEditPage(context));
    MultiLingualEditbox multiLang = edit.getTitleSection();
    multiLang.allMode();
    multiLang.editLangString("English", ENGLISH);
    multiLang.editLangString("Afar", NOT_ENGLISH);

    HomePage home = edit.save(new HomePage(context));
    assertTrue(home.portalExists(ENGLISH));
    assertFalse(home.portalExists(NOT_ENGLISH));

    UserProfilePage details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode("aa_DJ");
    details.saveSuccesful();

    logon();
    home = new MenuSection(context).get().home();

    assertFalse(home.portalExists(ENGLISH));
    assertTrue(home.portalExists(NOT_ENGLISH));

    details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode("en_AU");
    details.saveSuccesful();

    logon();
    home = new MenuSection(context).get().home();
    assertTrue(home.portalExists(ENGLISH));
    assertFalse(home.portalExists(NOT_ENGLISH));

    details = new TopbarMenuSection(context).get().editMyDetails();
    details.setLanguageByCode("");
    details.saveSuccesful();

    logon();
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
}
