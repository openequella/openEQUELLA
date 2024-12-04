package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.recipientseletor.RecipientSelectorPage;
import com.tle.webtests.pageobject.recipientseletor.RecipientSelectorPage.TypeValue;
import com.tle.webtests.pageobject.settings.AddUserQuotasPage;
import com.tle.webtests.pageobject.settings.ContentRestrictionsPage;
import com.tle.webtests.test.AbstractSessionTest;
import java.util.List;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class ContentRestrictionsTest extends AbstractSessionTest {
  public static final String DEMO_EXT = ContentRestrictionsTest.class.getSimpleName().toUpperCase();

  @Test
  public void testAddBannedExt() {
    ContentRestrictionsPage crp = logonToContentResrictionsPage();
    assertFalse(
        crp.isExtPresent(DEMO_EXT), "Test assumes extname '" + DEMO_EXT + "' not already in table");
    crp = crp.addBannedExtOk(DEMO_EXT);
    assertTrue(crp.isExtPresent(DEMO_EXT), "Banned ext not added");

    int numRowsPrior = crp.countRowsBannedExt();
    assertTrue(
        crp.isExtPresent(DEMO_EXT),
        "Test assumes extname '" + DEMO_EXT + "' already in table, but not found.");
    crp.deleteBannedExt(DEMO_EXT);
    assertFalse(crp.isExtPresent(DEMO_EXT), "Failed to delete successfully");

    int numRowsPost = crp.countRowsBannedExt();
    assertTrue(
        (numRowsPrior - 1) == numRowsPost,
        "Expected " + (numRowsPrior - 1) + " rows after deletion, but found " + numRowsPost);

    logout();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    ContentRestrictionsPage crp = logonToContentResrictionsPage();

    if (crp.isExtPresent(DEMO_EXT)) {
      crp.deleteBannedExt(DEMO_EXT);
    }

    super.cleanupAfterClass();
  }

  private ContentRestrictionsPage logonToContentResrictionsPage() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.contentRestrictionsSettings();
  }

  @Test
  public void testAddUserQuotas() {
    ContentRestrictionsPage crp = logonToContentResrictionsPage();
    AddUserQuotasPage userQuotaPage = crp.openUserQuotaPage();
    userQuotaPage.setUserQuota("450");

    RecipientSelectorPage selector =
        userQuotaPage.openRecipientSelector(new RecipientSelectorPage(context));

    String autoTest = "Auto Test";
    selector.searchUsersOrGroupsOrRoles(autoTest, TypeValue.USER);
    List<String> results = selector.getResults(TypeValue.USER);
    assertEquals(results.get(0), autoTest);
    selector.clickSingleAddIcon(autoTest);

    String loggedInUserRole = "Logged In User Role";
    selector.searchUsersOrGroupsOrRoles(loggedInUserRole, TypeValue.ROLE);
    results = selector.getResults(TypeValue.ROLE);
    assertEquals(results.get(0), loggedInUserRole);
    selector.clickSingleAddIcon(loggedInUserRole);

    userQuotaPage = selector.okButtonClick(userQuotaPage);
    assertEquals(
        userQuotaPage.getSelectedExpression(), "Auto Test [AutoTest] OR Logged In User Role");

    selector = userQuotaPage.openRecipientSelector(new RecipientSelectorPage(context));
    selector.removeSelectedByName(loggedInUserRole);
    userQuotaPage = selector.okButtonClick(userQuotaPage);
    assertEquals(userQuotaPage.getSelectedExpression(), "Auto Test [AutoTest]");

    selector = userQuotaPage.openRecipientSelector(new RecipientSelectorPage(context));
    selector.addGrouping("grouping0");
    selector.clickOtherLink();
    selector.selectGrouping("grouping1");
    selector.chooseGroupingOption("grouping1", "Match None");
    selector.addOtherOptionByName("Guest users");
    userQuotaPage = selector.okButtonClick(userQuotaPage);
    assertEquals(
        userQuotaPage.getSelectedExpression(), "Auto Test [AutoTest] OR NOT Guest User Role");

    crp = userQuotaPage.saveUserQuota(new ContentRestrictionsPage(context));
    String userQuota =
        crp.getUserQuotasInfoByExpression("Auto Test [AutoTest] OR NOT Guest User Role");
    assertEquals(userQuota, "450 MB");

    userQuotaPage =
        crp.editUserQuotasByExpression(
            "Auto Test [AutoTest] OR NOT Guest User Role", userQuotaPage);
    selector = userQuotaPage.openRecipientSelector(new RecipientSelectorPage(context));
    selector.searchUsersOrGroupsOrRoles("Role", TypeValue.ROLE);
    selector.selectGrouping("grouping1");
    selector.selectAllResults();
    selector.addSelectedResults();
    userQuotaPage = selector.okButtonClick(userQuotaPage);
    assertEquals(
        userQuotaPage.getSelectedExpression(),
        "Auto Test [AutoTest] OR NOT (Guest User Role OR Logged In User Role)");

    crp = userQuotaPage.saveUserQuota(crp);
    crp.deleteUserQuotasByExpression(
        "Auto Test [AutoTest] OR NOT (Guest User Role OR Logged In User Role)");
    logout();
  }
}
