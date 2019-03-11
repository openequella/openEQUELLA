package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BlackboardEquellaSettingsPage extends AbstractPage<BlackboardEquellaSettingsPage> {
  @FindBy(name = "equellaurl")
  private WebElement instUrl;

  @FindBy(name = "secretid")
  private WebElement secretId;

  @FindBy(name = "secret")
  private WebElement secretPassword;

  @FindBy(name = "bottom_Submit")
  private WebElement submit;

  @FindBy(id = "oauth.clientid")
  private WebElement clientIdField;

  @FindBy(id = "oauth.clientsecret")
  private WebElement clientSecretField;

  @FindBy(id = "newwindow")
  private WebElement newWindow;

  public BlackboardEquellaSettingsPage(PageContext context) {
    super(context, BlackboardPageUtils.pageTitleBy("EQUELLA Server Configuration"));
  }

  public void setInstitutionUrl(String baseUrl) {
    instUrl.clear();
    instUrl.sendKeys(baseUrl);
  }

  public void setSharedSecretId(String id) {
    secretId.clear();
    secretId.sendKeys(id);
  }

  public void setSharedSecretPassword(String password) {
    secretPassword.clear();
    secretPassword.sendKeys(password);
  }

  public void setOauthDetails(String id, String secret) {
    clientIdField.clear();
    clientIdField.sendKeys(id);
    clientSecretField.clear();
    clientSecretField.sendKeys(secret);
  }

  public void setNewWindow(boolean newWindow) {
    if (this.newWindow.isSelected() != newWindow) {
      this.newWindow.click();
    }
  }

  public void save() {
    submit.click();
  }
}
