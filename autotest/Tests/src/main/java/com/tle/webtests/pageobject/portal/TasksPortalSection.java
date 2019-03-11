package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class TasksPortalSection extends AbstractPortalSection<TasksPortalSection> {

  public TasksPortalSection(PageContext context, String title) {
    super(context, title);
  }

  public int getNumberNotifications() {
    WebElement notifications =
        getBoxContent().findElement(By.xpath("div[@class='alt-links']/a[@class='odd folder']"));

    String notificationsText = notifications.getText();
    int firstIndex = notificationsText.indexOf("(");
    int secondIndex = notificationsText.indexOf(")");
    String notificationNumber = notificationsText.substring(firstIndex + 1, secondIndex);
    return Integer.parseInt(notificationNumber);
  }

  public NotificationsPage ownerNotified() {
    getBoxContent()
        .findElement(By.xpath("div[@class='alt-links']/a[@class='even level2 document'][3]"))
        .click();
    return new NotificationsPage(context).get();
  }

  public NotificationsPage rejected() {
    getBoxContent()
        .findElement(By.xpath("div[@class='alt-links']/a[@class='odd level2 document'][3]"))
        .click();
    return new NotificationsPage(context).get();
  }

  public NotificationsPage badURLs() {
    getBoxContent()
        .findElement(By.xpath("div[@class='alt-links']/a[@class='even level2 document'][4]"))
        .click();
    return new NotificationsPage(context).get();
  }

  public NotificationsPage watchedLive() {
    getBoxContent()
        .findElement(By.xpath("div[@class='alt-links']/a[@class='odd level2 document'][4]"))
        .click();
    return new NotificationsPage(context).get();
  }

  public NotificationsPage overdue() {
    getBoxContent()
        .findElement(By.xpath("div[@class='alt-links']/a[@class='even level2 document'][5]"))
        .click();
    return new NotificationsPage(context).get();
  }
}
