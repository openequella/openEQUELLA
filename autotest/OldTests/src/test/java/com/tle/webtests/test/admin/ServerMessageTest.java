package com.tle.webtests.test.admin;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.ServerAdminLogonPage;
import com.tle.webtests.pageobject.institution.ServerSettingsTab;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class ServerMessageTest extends AbstractSessionTest {

  private static final String SERVER_MESSAGE = "This is the server message";

  @Test
  public void testServerMessage() {
    PageContext adminContext = new PageContext(this.context, testConfig.getAdminUrl());
    InstitutionListTab listTab =
        new ServerAdminLogonPage(adminContext)
            .load()
            .logon(testConfig.getAdminPassword(), new InstitutionListTab(adminContext));
    ServerSettingsTab settingsTab = listTab.serverSettingsTab();
    settingsTab.setServerMessage(SERVER_MESSAGE);

    if (!testConfig.isNewUI()) {
      assertTrue(isServerMessagePresent());
      logout();
    }

    settingsTab = listTab.load().serverSettingsTab();
    settingsTab.disableServerMessage();

    assertFalse(isServerMessagePresent());
  }

  private boolean isServerMessagePresent() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    new ContributePage(context).load();
    return isTextPresent(SERVER_MESSAGE);
  }
}
