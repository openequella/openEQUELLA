package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends AbstractPage<LoginPage> {
  private By oidcLoginButton = By.name("_oidcLoginSection_loginButton");

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

  public String getLoginFailure() {
    WebElement errorMessage =
        waiter.until(ExpectedConditions.presenceOfElementLocated(By.name("login_failure")));
    return errorMessage.getText();
  }

  public String getLoginErrorDetails() {
    WebElement errorMessage =
        waiter.until(
            ExpectedConditions.presenceOfElementLocated(By.name("login_error_description")));
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
    return isPresent(oidcLoginButton);
  }

  public void loginWithOidc() {
    if (hasOidcLoginButton()) {
      WebElement button =
          waiter.until(
              ExpectedConditions.elementToBeClickable(driver.findElement(oidcLoginButton)));
      button.click();
      // Above click triggers a page navigation, which means the button should be stale.
      waiter.until(ExpectedConditions.stalenessOf(button));
    } else {
      throw new IllegalStateException("OIDC configuration is not available.");
    }
  }
}
