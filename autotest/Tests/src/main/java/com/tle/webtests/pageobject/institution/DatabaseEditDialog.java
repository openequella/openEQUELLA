package com.tle.webtests.pageobject.institution;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DatabaseEditDialog extends AbstractPage<DatabaseEditDialog> {
  @FindBy(id = "isdt_ded_url")
  private WebElement jdbcUrlField;

  @FindBy(id = "isdt_ded_username")
  private WebElement usernameField;

  @FindBy(id = "isdt_ded_password")
  private WebElement passwordField;

  @FindBy(id = "isdt_ded_reportingUrl")
  private WebElement reportingJdbcUrlField;

  @FindBy(id = "isdt_ded_reportingUsername")
  private WebElement reportingUsernameField;

  @FindBy(id = "isdt_ded_reoirtungPassword")
  private WebElement reportingPasswordField;

  @FindBy(id = "isdt_ded_ok")
  private WebElement okButton;

  @FindBy(id = "isdt_ded_addOnline")
  private WebElement addOnlineButton;

  private DatabasesPage dbPage;

  public DatabaseEditDialog(DatabasesPage dbPage) {
    super(dbPage.getContext());
    this.dbPage = dbPage;
  }

  @Override
  protected WebElement findLoadedElement() {
    return jdbcUrlField;
  }

  public void setJdbcUrl(String jdbcUrl) {
    jdbcUrlField.clear();
    jdbcUrlField.sendKeys(jdbcUrl);
  }

  public void setUsername(String username) {
    usernameField.clear();
    usernameField.sendKeys(username);
  }

  public void setPassword(String password) {
    passwordField.clear();
    passwordField.sendKeys(password);
  }

  public void setReportingJdbcUrl(String jdbcUrl) {
    reportingJdbcUrlField.clear();
    reportingJdbcUrlField.sendKeys(jdbcUrl);
  }

  public void setReportingUsername(String username) {
    reportingUsernameField.clear();
    reportingUsernameField.sendKeys(username);
  }

  public void setReportingPassword(String password) {
    reportingPasswordField.clear();
    reportingPasswordField.sendKeys(password);
  }

  public DatabasesPage finish() {
    WaitingPageObject<DatabasesPage> updateWaiter = dbPage.updateWaiter();
    okButton.click();
    return updateWaiter.get();
  }

  public DatabasesPage finishOnline() {
    WaitingPageObject<DatabasesPage> updateWaiter = dbPage.updateWaiter();
    addOnlineButton.click();
    acceptConfirmation();
    return updateWaiter.get();
  }
}
