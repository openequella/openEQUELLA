package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ErrorPage extends AbstractPage<ErrorPage> {
  private final boolean forceOld;

  public ErrorPage(PageContext context) {
    this(context, false);
  }

  public ErrorPage(PageContext context, boolean forceOld) {
    super(context); //  /h2[normalize-space(text())='An error occurred']
    this.forceOld = forceOld;
    loadedBy = getErrorBy(isNewUI());
  }

  protected boolean isNewUI() {
    return !forceOld && super.isNewUI();
  }

  @Override
  protected void isError() {
    // don't blow up when we expect an error
  }

  public static By getErrorBy(boolean newUi) {
    return newUi ? By.id("errorPage") : By.xpath("//div[@class='area error']");
  }

  // Existing tests have the old UI error screen appear for new UI flows.  Looks like
  // tech debt as oEQ is converted to the new UI.  Eventually, it would be good to key
  // off of testConfig.isNewUI()
  public String getMainErrorMessage(boolean newUi) {
    By mainErrorBy =
        getErrorBy(newUi ? "id('mainDiv')//h5" : "//div[contains(@class, 'error')]/h2");
    return driver.findElement(mainErrorBy).getText();
  }

  // See note above on getMainErrorMessage(boolean)
  public String getSubErrorMessage(boolean newUi) {
    return driver
        .findElement(
            getErrorBy(newUi ? "id('errorPage')//h3" : "//div[contains(@class, 'error')]/h3[1]"))
        .getText();
  }

  public String getDetail(boolean newUi) {
    return driver
        .findElement(
            getErrorBy(
                newUi ? "id('errorPage')//h5" : "//div[@class='area error']/p[@id='description']"))
        .getText();
  }

  public String getDetail() {
    return getDetail(isNewUI());
  }

  public String getDenied() {
    return driver
        .findElement(
            getErrorBy(
                isNewUI() ? "id('errorPage')//h5" : "//div[@class='area error']/p[@id='denied']"))
        .getText();
  }

  public <T extends PageObject> T goBack(WaitingPageObject<T> backTo) {
    if (isNewUI()) {
      driver.navigate().refresh();
    } else {
      context.getDriver().navigate().back();
    }
    return backTo.get();
  }

  private By getErrorBy(String xpath) {
    By errorElement = By.xpath(xpath);
    waiter.until(ExpectedConditions.visibilityOfElementLocated(errorElement));
    return errorElement;
  }
}
