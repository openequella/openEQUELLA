package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodleSettingsUpgradePage extends MoodleBasePage<MoodleSettingsUpgradePage> {
  @FindBy(className = "form-submit")
  private WebElement saveChanges;

  public MoodleSettingsUpgradePage(PageContext context) {
    super(context, By.id("page-admin-upgradesettings"));
  }

  public MoodleNotificationsPage saveChanges() {
    // Sad times... nothing else to wait for on this page
    sleepyTime(3000);
    saveChanges.click();
    return new MoodleNotificationsPage(context).get();
  }
}
