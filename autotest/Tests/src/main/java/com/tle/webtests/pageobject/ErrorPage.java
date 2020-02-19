package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

/** @author Aaron */
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

  public String getMainErrorMessage() {
    return driver
        .findElement(
            isNewUI()
                ? By.xpath("id('mainDiv')//h5")
                : By.xpath("//div[contains(@class, 'error')]/h2"))
        .getText();
  }

  public String getSubErrorMessage() {
    return driver
        .findElement(
            isNewUI()
                ? By.xpath("id('errorPage')//h3")
                : By.xpath("//div[contains(@class, 'error')]/h3[1]"))
        .getText();
  }

  public String getDetail() {
    return driver
        .findElement(
            isNewUI()
                ? By.xpath("id('errorPage')//h5")
                : By.xpath("//div[@class='area error']/p[@id='description']"))
        .getText();
  }

  public String getDenied() {
    return driver
        .findElement(
            isNewUI()
                ? By.xpath("id('errorPage')//h5")
                : By.xpath("//div[@class='area error']/p[@id='denied']"))
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
}
