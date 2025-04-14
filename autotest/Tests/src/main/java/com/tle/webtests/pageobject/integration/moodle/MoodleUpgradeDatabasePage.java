package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodleUpgradeDatabasePage extends MoodleBasePage<MoodleUpgradeDatabasePage> {
  @FindBy(xpath = "//input[@value='Continue']")
  private WebElement continueButton;

  @FindBy(xpath = "//input[@value='Cancel']")
  private WebElement cancelButton;

  public MoodleUpgradeDatabasePage(PageContext context) {
    super(
        context,
        By.xpath(
            "//p[contains(text(), 'Your Moodle files have been changed, and you are about to"
                + " automatically upgrade your server to this version:')]"));
  }

  public MoodleServerStatusPage clickContinue() {
    continueButton.click();
    return new MoodleServerStatusPage(context);
  }
}
