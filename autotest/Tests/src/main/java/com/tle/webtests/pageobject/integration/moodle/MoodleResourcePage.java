package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MoodleResourcePage extends AbstractPage<MoodleResourcePage> {
  private final MoodleCoursePage coursePage;

  public MoodleResourcePage(MoodleCoursePage coursePage, String type) {
    super(coursePage.getContext(), By.xpath("//body[contains(@id," + quoteXPath(type) + ")]"));
    this.coursePage = coursePage;
  }

  public String getContentUrl() {
    WebElement container = waitForElement(By.id("region-main"));
    return waitForHiddenElement(container.findElement(By.tagName("object"))).getAttribute("data");
  }

  public String getDescription() {

    return waitForElement(By.xpath("id('equellaintro')")).getText().trim();
  }

  public String getImageUrlFromDescription() {

    return waitForElement(By.xpath("id('equellaintro')//p/img")).getAttribute("src");
  }

  public boolean isUsingExternalUrlViewer() {
    switchToItem();
    boolean external =
        isPresent(By.id("elv_itemName")) && isPresent(By.xpath("//iframe[@id='external-content']"));
    returnToMoodle();

    return external;
  }

  public MoodleCoursePage backToCoursePage() {
    return new MoodleBreadcrumbs(context).get().clickCourse(coursePage);
  }

  public boolean isTokenError() {
    switchToItem();
    boolean denied =
        !driver
            .findElements(
                By.xpath("//p[contains(text(),'You do not have permissions to use this token')]"))
            .isEmpty();
    returnToMoodle();
    return denied;
  }

  public boolean isLtiAuthError() {
    switchToItem();
    boolean denied = isPresent(By.xpath("//h3[contains(text(), 'Authentication')]"));
    returnToMoodle();
    return denied;
  }

  protected void switchToItem() {
    if (context.getTestConfig().isChromeDriverSet()) {
      driver.switchTo().defaultContent();
      try {
        WebElement objectTag =
            driver.findElement(By.id("region-main")).findElement(By.tagName("object"));
        waiter.until(ChromeHacks.convertObjectToiFrame(context, objectTag));
      } catch (Exception e) {
        waiter.until(
            ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
                context.getDriver(), By.id("contentframe")));
      }
    } else {
      driver.switchTo().defaultContent();
      driver.switchTo().frame(0);
    }
  }

  public <T extends AbstractPage<T>> T switchToItem(T page) {
    driver.switchTo().defaultContent();
    if (context.getTestConfig().isChromeDriverSet()) {
      try {
        WebElement objectTag =
            driver.findElement(By.id("region-main")).findElement(By.tagName("object"));
        waiter.until(ChromeHacks.convertObjectToiFrame(context, objectTag));
      } catch (Exception e) {
        waiter.until(
            ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
                context.getDriver(), By.id("contentframe")));
      }

      return page.get();
    } else {
      driver.switchTo().frame(0);
      return page.get();
    }
  }

  public MoodleResourcePage returnToMoodle() {
    driver.switchTo().defaultContent();
    return get();
  }

  @Override
  public MoodleResourcePage get() {
    driver.switchTo().defaultContent();
    return super.get();
  }
}
