package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ScheduledTasksPage extends AbstractPage<ScheduledTasksPage> {
  @FindBy(linkText = "Check Review")
  WebElement checkReviewLink;

  @FindBy(linkText = "Notify Of New Items")
  WebElement notifyNewTasksLink;

  @FindBy(linkText = "com.tle.core.payment.storefront.task.CheckUpdatedPurchasedItemsTask")
  WebElement checkUpdatedPurchasedItemsLink;

  @FindBy(linkText = "com.tle.core.payment.storefront.task.CheckCurrentOrdersTask")
  WebElement checkOrdersTaskLink;

  @FindBy(linkText = "Check Moderation")
  WebElement checkModerationLink;

  @FindBy(linkText = "Blackboard Connector Synchronisation")
  WebElement blackboardSyncLink;

  @FindBy(linkText = "Check-URLs")
  WebElement checkUrlsLink;

  @FindBy(linkText = "Run Harvester Profiles")
  WebElement runHarvesterProfilesLink;

  public ScheduledTasksPage(PageContext context) {
    super(context);
    loadedBy = byForPageTitle("Task debug page");
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/scheduledtasksdebug.do");
  }

  public void runCheckReview() {
    clickLink(checkReviewLink);
  }

  public void runNotifyNewTasks() {
    clickLink(notifyNewTasksLink);
  }

  public void runCheckModeration() {
    clickLink(checkModerationLink);
  }

  public ScheduledTasksPage runCheckUrls() {
    return clickAndUpdate(checkUrlsLink);
  }

  public ScheduledTasksPage runCheckOrdersTask(boolean really) {
    return clickAndUpdate(checkOrdersTaskLink);
  }

  public ScheduledTasksPage runCheckUpdatedPurchasedItemsTask(boolean really) {
    return clickAndUpdate(checkUpdatedPurchasedItemsLink);
  }

  public ScheduledTasksPage runSyncEquellaContent() {
    return clickAndUpdate(blackboardSyncLink);
  }

  public ScheduledTasksPage runHarvesterProfiles() {
    return clickAndUpdate(runHarvesterProfilesLink);
  }

  /**
   * Used for clicking the simply links which simply trigger a server action, and then the page is
   * (historically) re-rendered. However in New UI react detects there's nothing new in the DOM so
   * doesn't change anything. So in that case we have to resort to sleepTime().
   *
   * @param element the link to click
   */
  private void clickLink(WebElement element) {
    if (context.getTestConfig().isNewUI()) {
      // Due to react DOM rendering optimisations, there is nothing for us to detect so we have
      // to use sleepy time.
      element.click();
      sleepyTime(500);
    } else {
      clickAndUpdate(element);
    }
  }
}
