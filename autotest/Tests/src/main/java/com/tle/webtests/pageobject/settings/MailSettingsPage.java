package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MailSettingsPage extends AbstractPage<MailSettingsPage> {
  @FindBy(id = "_fe")
  private WebElement emailAddressTextBox;

  @FindBy(id = "_s")
  private WebElement serverTextBox;

  @FindBy(id = "_dn")
  private WebElement displayNameTextBox;

  @FindBy(id = "_u")
  private WebElement userNameTextBox;

  @FindBy(id = "password")
  private WebElement passwordTextBox;

  @FindBy(id = "_te")
  private WebElement testEmailAddressTextBox;

  @FindBy(id = "_testButton")
  private WebElement testButton;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  public MailSettingsPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return saveButton;
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/mailsettings.do");
  }

  public void setEmailAddress(String address) {
    emailAddressTextBox.clear();
    emailAddressTextBox.sendKeys(address);
    get();
  }

  public void setServer(String server) {
    serverTextBox.clear();
    serverTextBox.sendKeys(server);
    get();
  }

  public void setDisplayName(String displayName) {
    displayNameTextBox.clear();
    displayNameTextBox.sendKeys(displayName);
    get();
  }

  public void setUserName(String userName) {
    userNameTextBox.clear();
    userNameTextBox.sendKeys(userName);
    get();
  }

  public void setPassword(String password) {
    passwordTextBox.clear();
    passwordTextBox.sendKeys(password);
    get();
  }

  public void setTestEmailAddress(String address) {
    testEmailAddressTextBox.clear();
    testEmailAddressTextBox.sendKeys(address);
    get();
  }

  public MailSettingsPage testButtonClick() {
    testButton.click();
    return new MailSettingsPage(context).get();
  }

  public MailSettingsPage save() {
    saveButton.click();
    return new MailSettingsPage(context).get();
  }

  public String getEmailStatus() {
    By xpath = By.xpath("//div[@id='emailstatus']/div[text()]");
    // Wait a bit longer than normal
    WebDriverWait waiter = new WebDriverWait(context.getDriver(), 60, 50);
    waiter.until(ExpectedConditions.visibilityOfElementLocated(xpath));
    return driver.findElement(xpath).getText();
  }
}
