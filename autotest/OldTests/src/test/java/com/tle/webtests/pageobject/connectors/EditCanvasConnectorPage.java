package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class EditCanvasConnectorPage extends AbstractConnectorEditPage<EditCanvasConnectorPage> {
  @FindBy(id = "cce_u")
  private WebElement urlField;

  @FindBy(id = "cce_testUrlButton")
  private WebElement testUrlButton;

  @FindBy(id = "cce_manualTokenEntry")
  private WebElement accessTokenField;

  @FindBy(id = "cce_testTokenButton")
  private WebElement testTokenButton;

  public EditCanvasConnectorPage(ShowConnectorsPage connectorsPage) {
    super(connectorsPage);
  }

  public void createConnector(PrefixedName name, String token) {
    setName(name);
    urlField.sendKeys(context.getIntegUrl());
    WaitingPageObject<EditCanvasConnectorPage> updateWaiter = updateWaiter(testUrlButton);
    testUrlButton.click();
    updateWaiter.get();

    setAccessToken(token);
    updateWaiter = updateWaiter(testTokenButton);
    testTokenButton.click();
    updateWaiter.get();
  }

  public void setAccessToken(String token) {
    accessTokenField.clear();
    accessTokenField.sendKeys(token);
  }

  public boolean testAccessToken() {
    WaitingPageObject<EditCanvasConnectorPage> updateWaiter = updateWaiter(testTokenButton);
    testTokenButton.click();
    updateWaiter.get();
    if (isPresent(By.xpath("//span[normalize-space(text()) = 'Access token OK']"))) {
      return true;
    } else if (isPresent(
        By.xpath(
            "//span[normalize-space(text()) = 'Unauthorised, check token and server URL and try"
                + " again']"))) {
      return false;
    }
    return false;
  }

  @Override
  public WebElement getUsernameField() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WebElement getTestButton() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getId() {
    return "cce";
  }

  @Override
  public WebElement getAllowSummaryCheckbox() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getEditorSectionId() {
    return "cce";
  }
}
