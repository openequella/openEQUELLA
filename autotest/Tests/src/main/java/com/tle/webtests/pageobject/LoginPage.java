package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends AbstractPage<LoginPage> {
  @FindBy(className = "warning")
  private WebElement errorMessage;

  public LoginPage(PageContext context) {
    super(context, By.id("_logonButton"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "logon.do?logout=true");
  }

  public LoginPage logout() {
    return load();
  }

  public HomePage login(String username, String password) {
    loginWithRedirect(username, password);
    return new HomePage(context).get();
  }

  public LoginNoticePage loginWithNotice(String username, String password) {
    loginWithRedirect(username, password);
    return new LoginNoticePage(context).get();
  }

  public LoginPage loginWithError(String username, String password) {
    loginWithRedirect(username, password);
    return get();
  }

  public String getLoginError() {
    return errorMessage.getText();
  }

  public void loginWithRedirect(String username, String password) {
    WebElement user = driver.findElement(By.id("username"));
    user.clear();
    user.sendKeys(username);

    WebElement pass = driver.findElement(By.id("password"));
    pass.clear();
    pass.sendKeys(password);

    driver.findElement(By.id("_logonButton")).click();
  }

  public HomePage autoLogin() {
    driver.findElement(By.className("autologinlink")).click();
    return new HomePage(context).get();
  }

  public boolean hasAutoLogin() {
    return isPresent(By.className("autologinlink"));
  }

  public boolean hasOidcLoginButton() {
    return isPresent(By.name("_oidcLoginSection_loginButton"));
  }
}
