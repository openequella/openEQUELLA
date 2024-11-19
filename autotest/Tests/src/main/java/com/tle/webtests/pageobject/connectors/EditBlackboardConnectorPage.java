package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EditBlackboardConnectorPage
    extends AbstractConnectorEditPage<EditBlackboardConnectorPage> {
  @FindBy(id = "bce_u")
  private WebElement urlField;

  @FindBy(id = "bce_twsu")
  private WebElement userField;

  @FindBy(id = "bce_testUrlButton")
  private WebElement testUrlButton;

  @FindBy(id = "bce_registerButton")
  private WebElement registerButton;

  @FindBy(id = "bce_us")
  private WebElement usernameField;

  @FindBy(id = "bce_testWebServiceButton")
  private WebElement testButton;

  @FindBy(id = "bce_pts")
  private WebElement passField;

  @FindBy(id = "bce_es")
  private WebElement allowSummary;

  @FindBy(id = "registerdiv")
  private WebElement registerDiv;

  @FindBy(id = "blackboardsetup")
  private WebElement blackboardSetupDiv;

  private WebDriverWait bbWaiter;

  @Override
  public WebDriverWait getWaiter() {
    return bbWaiter;
  }

  public EditBlackboardConnectorPage(ShowConnectorsPage connectorsPage) {
    super(connectorsPage);
    this.bbWaiter = new WebDriverWait(context.getDriver(), 60, 50);
  }

  public EditBlackboardConnectorPage setUrl(String url, String user, String password) {
    urlField.clear();
    urlField.sendKeys(url);
    WaitingPageObject<EditBlackboardConnectorPage> updateWaiter = updateWaiter(testUrlButton);
    testUrlButton.click();
    updateWaiter.get();

    setPassword(password);
    updateWaiter = updateWaiter(registerButton);
    registerButton.click();
    updateWaiter.get();

    setPassword(password);
    userField.clear();
    userField.sendKeys(user);
    testButton.click();
    waitForElement(
        By.xpath("//span[text()='EQUELLA Blackboard web service contacted successfully']"));

    return this;
  }

  @Override
  public String getEditorSectionId() {
    return "bce";
  }

  public EditBlackboardConnectorPage registerProxy(String url) {
    urlField.clear();
    urlField.sendKeys(url);
    WaitingPageObject<EditBlackboardConnectorPage> ajaxUpdateExpect =
        ajaxUpdateExpect(blackboardSetupDiv, registerButton);
    testUrlButton.click();
    ajaxUpdateExpect.get();

    ajaxUpdateExpect = ajaxUpdateExpect(registerDiv, registerButton);
    registerButton.click();
    return ajaxUpdateExpect.get();
  }

  public void setPassword(String password) {
    passField.clear();
    passField.sendKeys(password);
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
    return "bce";
  }

  @Override
  public WebElement getAllowSummaryCheckbox() {
    return allowSummary;
  }
}
