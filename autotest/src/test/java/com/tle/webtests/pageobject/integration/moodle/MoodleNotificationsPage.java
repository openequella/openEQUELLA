package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodleNotificationsPage extends AbstractPage<MoodleNotificationsPage> {
  @FindBy(linkText = "Log out")
  private WebElement logoutLink;

  public MoodleNotificationsPage(PageContext context) {
    super(context, By.xpath("//li/a[text()='Notifications']"));
  }

  public void logout() {
    logoutLink.click();
  }
}
