package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.DiagnosticsPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class DiagnosticsTest extends AbstractSessionTest {

  private DiagnosticsPage logonToDiagnosticsPage() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.diagnosticsPage();
  }

  @Test
  public void testSelectGroup() {
    DiagnosticsPage dp = logonToDiagnosticsPage();
    dp.searchGivenGroupName("admin", "Users with Administrator Role");
    assertTrue(dp.memberExists("AutoTest"));
    dp.searchGivenGroupName(null, null);
    assertTrue(!dp.memberExists("AutoTest"));
  }

  @Test
  public void testSelectUser() {
    DiagnosticsPage dp = logonToDiagnosticsPage();
    dp.searchGivenUserName("auto", "AutoTest");
    assertTrue(dp.groupExists("Users with Administrator Role"));
    dp.searchGivenUserName(null, null);
    assertFalse(dp.groupExists("Users with Administrator Role"));
  }
}
