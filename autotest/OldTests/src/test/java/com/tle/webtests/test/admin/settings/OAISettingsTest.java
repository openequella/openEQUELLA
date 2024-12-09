/** */
package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;

import com.tle.common.Check;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.OAISettingsPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class OAISettingsTest extends AbstractSessionTest {
  public static final String OAI_LINK_TITLE = OAISettingsPage.OAI_SETTINGS_HEADER;

  @Test
  public void testOaiInputs() {
    OAISettingsPage oaip = logonToOaiSettings();
    oaip.setOAIScheme("nufn");
    oaip.setNamespace("");
    oaip.setEmail("This is not an email");
    oaip.save();
    logout();
    oaip = logonToOaiSettings();
    assertTrue(
        "nufn".equals(oaip.getOAIScheme()), "Expected 'nufn', got " + oaip.getOAIScheme() + '.');
    assertTrue(
        Check.isEmpty(oaip.getNamespace()), "Expected null, got " + oaip.getNamespace() + '.');
    assertTrue(
        "This is not an email".equals(oaip.getEmail()),
        "Expected 'This is not an email', got " + oaip.getEmail() + '.');
    logout();
  }

  private OAISettingsPage logonToOaiSettings() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.oaiSettingsPage();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    OAISettingsPage oaip = logonToOaiSettings();
    oaip.setOAIScheme("oai");
    oaip.setNamespace("");
    oaip.setEmail("");
    oaip.save();
  }
}
