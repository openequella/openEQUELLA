package io.github.openequella.pages.oidc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.OidcSettingsPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class OidcSettingsPageTest extends AbstractCleanupAutoTest {

  @Test(description = "User should be able to see OIDC settings in settings page.")
  public void testNavigation() {
    SettingsPage sp = new SettingsPage(context).load();
    OidcSettingsPage oidcSettingsPage = sp.oidcSettingsPage();
    assertTrue(oidcSettingsPage.isLoaded());
  }

  @Test(description = "Do not display OIDC settings for users without permission.")
  public void testUnauthorisedAccess() {
    logon(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD);
    SettingsPage sp = new SettingsPage(context).load();
    assertFalse(sp.isSettingVisible("OIDC"));
  }

  @Test(description = "User without permission can't access OIDC configuration page through URL.")
  public void permissionCheck() {
    logon(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD);

    context.getDriver().get(context.getBaseUrl() + OidcSettingsPage.getUrl());
    ErrorPage errorPage = new ErrorPage(context);

    // set New UI parameter to true since it's rendered under settings page.
    assertEquals(errorPage.getSubErrorMessage(true), "403 : Access Denied");
    assertEquals(
        errorPage.getDetail(true),
        "No permission to access /page/oidc - missing ACL(s): EDIT_SYSTEM_SETTINGS");
  }
}
