package com.tle.webtests.test.admin.settings;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.settings.GoogleSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("flakey")
public class GoogleAnalyticsTest extends AbstractSessionTest {

  @Test
  public void testSaveSettings() {
    logon("AutoTest", "automated");
    GoogleSettingsPage googlePage = new GoogleSettingsPage(context).load();
    googlePage.setAccountId("test");
    googlePage.save();
    Assert.assertTrue(googlePage.isTrackingEnabled());

    googlePage.load();
    Assert.assertTrue(googlePage.isTrackingTagPresent());

    googlePage = new GoogleSettingsPage(context).load();
    googlePage.clearAccountId();
    googlePage.save();
    Assert.assertFalse(googlePage.isTrackingEnabled());

    googlePage.load();
    Assert.assertFalse(googlePage.isTrackingTagPresent());
  }
}
