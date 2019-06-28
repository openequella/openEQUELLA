package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.MUIHelper;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class TopbarMenuSection extends AbstractPage<TopbarMenuSection> {

  public TopbarMenuSection(PageContext context) {
    super(context);
    loadedBy = isNewUI() ? By.tagName("header") : By.id("topmenu");
  }

  private WebElement getTopBarElement() {
    return driver.findElement(loadedBy);
  }

  public int getNumberOfNotifications() {
    return getCountForElement(
        getTopBarElement().findElement(By.xpath("//*[@title = 'Notifications']")));
  }

  public int getNumberOfTasks() {
    return getCountForElement(getTopBarElement().findElement(By.xpath("//*[@title = 'Tasks']")));
  }

  private int getCountForElement(WebElement base) {
    String countString = isNewUI() ? MUIHelper.getBadgeText(base) : base.getText();
    if (countString == null) {
      return 0;
    }
    return Integer.parseInt(countString);
  }

  public UserProfilePage editMyDetails() {

    UserProfilePage upp = new UserProfilePage(context);
    if (isNewUI()) {
      return upp.load();
    }
    getTopBarElement().findElement(By.xpath("//a[@href = 'access/user.do']")).click();
    return upp.get();
  }
}
