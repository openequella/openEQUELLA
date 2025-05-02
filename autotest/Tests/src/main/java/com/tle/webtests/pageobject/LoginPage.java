package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

  /**
   * Retrieves the errors displayed on the login page. This static method is typically used in tests
   * that do not require a {@code LoginPage} instance.
   *
   * @param waiter A {@link WebDriverWait} used to wait until the error message is present
   */
  public static String getLoginErrorDetails(WebDriverWait waiter) {
    WebElement errorMessage =
        waiter.until(
            ExpectedConditions.presenceOfElementLocated(By.name("login_error_description")));
    return errorMessage.getText();
  }

  /**
   * Retrieves the login error message using the {@link WebDriverWait} configured for this {@code
   * LoginPage} instance.
   */
  public String getLoginErrorDetails() {
    return LoginPage.getLoginErrorDetails(waiter);
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
    } else {
      throw new IllegalStateException("OIDC configuration is not available.");
    }
  }
}
