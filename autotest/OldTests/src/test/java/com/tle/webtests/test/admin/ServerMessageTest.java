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
        new ServerAdminLogonPage(context)
            .load()
            .logon(testConfig.getAdminPassword(), new InstitutionListTab(adminContext));
    ServerSettingsTab settingsTab = listTab.serverSettingsTab();
    settingsTab.setServerMessage(SERVER_MESSAGE);

    logon("AutoTest", "automated");
    ContributePage page = new ContributePage(context).load();
    if (!page.usingNewUI()) {
      assertTrue(isTextPresent(SERVER_MESSAGE));
    } else {
      // won't be present as it is not currently implemented in the new UI
      assertFalse(isTextPresent(SERVER_MESSAGE));
    }
    logout();

    settingsTab = listTab.load().serverSettingsTab();
    settingsTab.disableServerMessage();

    logon("AutoTest", "automated");
    new ContributePage(context).load();
    assertFalse(isTextPresent(SERVER_MESSAGE));
  }
}
