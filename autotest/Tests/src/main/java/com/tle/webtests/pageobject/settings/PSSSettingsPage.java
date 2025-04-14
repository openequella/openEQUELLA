package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PSSSettingsPage extends AbstractPage<PSSSettingsPage> {
  @FindBy(id = "scenbk")
  private WebElement enablePSS;

  @FindBy(id = "_baseUrl")
  private WebElement baseUrl;

  @FindBy(id = "_consumerKey")
  private WebElement consumerKey;

  @FindBy(id = "_consumerSecret")
  private WebElement consumerSecret;

  @FindBy(id = "_accountNamespace")
  private WebElement namespace;

  @FindBy(id = "_testButton")
  private WebElement testButton;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  @FindBy(className = "success")
  private WebElement successDiv;

  @FindBy(id = "overallajaxdiv")
  private WebElement ajaxDiv;

  @FindBy(id = "controls")
  private WebElement controlsAjaxDiv;

  @FindBy(id = "connectionstatus")
  private WebElement connectionStatusAjaxDiv;

  public PSSSettingsPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return saveButton;
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/scormicodeonsettings.do");
  }

  public PSSSettingsPage enablePSS(boolean enable) {
    if (enablePSS.isSelected() != enable) {
      WaitingPageObject<PSSSettingsPage> waiter;
      if (enable) {
        waiter = ajaxUpdateExpect(ajaxDiv, baseUrl);
      } else {
        waiter = removalWaiter(baseUrl);
      }
      enablePSS.click();
      return waiter.get();
    }
    return this;
  }

  public void setBaseUrl(String url) {
    baseUrl.clear();
    baseUrl.sendKeys(url);
  }

  public void setConsumerKey(String key) {
    consumerKey.clear();
    consumerKey.sendKeys(key);
  }

  public void setConsumerSecret(String secret) {
    consumerSecret.clear();
    consumerSecret.sendKeys(secret);
  }

  public void setNamespace(String ns) {
    namespace.clear();
    namespace.sendKeys(ns);
  }

  public boolean hasError(String errorText) {
    return isPresent(
        By.xpath("//p[@class='ctrlinvalidmessage' and text()=" + quoteXPath(errorText) + "]"));
  }

  public boolean hasSuccess(String successText) {
    return isPresent(
        By.xpath("//div[@class='success']/span[text()=" + quoteXPath(successText) + "]"));
  }

  public PSSSettingsPage testFailValidation(String errorText) {
    WaitingPageObject<PSSSettingsPage> waiter;
    waiter =
        visibilityWaiter(
            driver,
            By.xpath("//p[@class='ctrlinvalidmessage' and text()=" + quoteXPath(errorText) + "]"));
    testButton.click();
    return waiter.get();
  }

  public PSSSettingsPage testSuccess() {
    WaitingPageObject<PSSSettingsPage> ajaxUpdate =
        ajaxUpdateExpect(connectionStatusAjaxDiv, successDiv);
    testButton.click();
    return ajaxUpdate.get();
  }

  public PSSSettingsPage save() {
    saveButton.click();
    return this.get();
  }

  public PSSSettingsPage saveWithoutTest() {
    WaitingPageObject<PSSSettingsPage> waiter;
    waiter =
        visibilityWaiter(
            driver,
            By.xpath(
                "//p[@class='ctrlinvalidmessage' and text()='You must successfully test the Pearson"
                    + " SCORM Services settings before saving.']"));
    saveButton.click();
    return waiter.get();
  }

  public boolean isEnabled() {
    return enablePSS.isSelected();
  }
}
