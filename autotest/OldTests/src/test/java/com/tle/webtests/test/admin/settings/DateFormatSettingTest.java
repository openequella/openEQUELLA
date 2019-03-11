package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.DateFormatSettingPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class DateFormatSettingTest extends AbstractSessionTest {
  @Test
  public void testSaveDateFormatSetting() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    DateFormatSettingPage dateSettingPage = sp.dateFormatSettingPage();
    dateSettingPage.setExactDateFormat();
    dateSettingPage.saveSettings();
    ReceiptPage.waiter("Date format settings saved successfully", dateSettingPage).get();
    sp = new SettingsPage(context).load();
    dateSettingPage = sp.dateFormatSettingPage();
    assertTrue(dateSettingPage.isExactDateFormat());
    dateSettingPage.setApproxDateFormat();
    dateSettingPage.saveSettings();
    assertTrue(dateSettingPage.isApproxDateFormat());
    logout();
  }
}
