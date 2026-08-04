package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class EditMoodleConnectorPage extends AbstractConnectorEditPage<EditMoodleConnectorPage> {
  @FindBy(id = "mce_u")
  private WebElement urlField;

  @FindBy(id = "mce_testUrlButton")
  private WebElement testUrlButton;

  @FindBy(id = "mce_wt")
  private WebElement tokenField;

  @FindBy(id = "mce_testServiceButton")
  private WebElement testServiceButton;

  @FindBy(id = "mce_us")
  private WebElement usernameField;

  @FindBy(id = "mce_testServiceButton")
  private WebElement testButton;

  @FindBy(id = "mce_es")
  private WebElement allowSummary;

  @FindBy(id = "moodlesetup")
  private WebElement testAjaxDiv;

  public EditMoodleConnectorPage(ShowConnectorsPage showConnectorsPage) {
    super(showConnectorsPage);
  }

  public EditMoodleConnectorPage setUrl(String url) {
    urlField.clear();
    urlField.sendKeys(url);
    WaitingPageObject<EditMoodleConnectorPage> waiter = ajaxUpdateExpect(testAjaxDiv, urlField);
    testUrlButton.click();
    return waiter.get();
  }

  @Override
  public String getEditorSectionId() {
    return "mce";
  }

  public EditMoodleConnectorPage setToken(String token) {
    tokenField.clear();
    tokenField.sendKeys(token);
    return this;
  }

  @Override
  public WebElement getUsernameField() {
    return usernameField;
  }

  @Override
  public WebElement getTestButton() {
    return testButton;
  }

  @Override
  public String getId() {
    return "mce";
  }

  @Override
  public WebElement getAllowSummaryCheckbox() {
    return allowSummary;
  }
}
