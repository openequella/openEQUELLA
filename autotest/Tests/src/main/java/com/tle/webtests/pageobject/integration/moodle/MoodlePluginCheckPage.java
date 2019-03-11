package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodlePluginCheckPage extends MoodleBasePage<MoodlePluginCheckPage> {
  @FindBy(xpath = "//input[@value='Upgrade Moodle database now']")
  private WebElement upgradeNowButton;

  public MoodlePluginCheckPage(PageContext context) {
    super(context, By.id("plugins-check"));
  }

  public MoodleUpgradingPage clickUpgradeNow() {
    upgradeNowButton.click();
    return new MoodleUpgradingPage(context).get();
  }
}
