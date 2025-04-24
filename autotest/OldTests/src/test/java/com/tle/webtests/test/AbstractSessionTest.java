package com.tle.webtests.test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AbstractSessionTest extends AbstractTest {
  public static String GENERIC_TESTING_COLLECTION = "Generic Testing Collection";
  public static final String AUTOTEST_LOGON = "AutoTest";
  public static final String AUTOTEST_LOW_PRIVILEGE_LOGON = "AutoTest_Low_Privileged";
  public static final String AUTOTEST_PASSWD = "automated";

  protected String lastUsername;
  protected String lastPassword;
  protected boolean notice;

  public HomePage logon(PageContext context, String username, String password) {
    HomePage login = new LoginPage(context).load().login(username, password);
    this.lastUsername = username;
    this.lastPassword = password;
    notice = false;
    return login;
  }

  public HomePage logon(String username, String password) {
    return logon(context, username, password);
  }

  public HomePage logon(String username, String password, PageContext context) {
    return logon(context, username, password);
  }

  public HomePage logon() {
    return logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
  }

  public void logonOnly(String username, String password) {
    LoginPage loginPage = new LoginPage(context).load();
    loginPage.loginWithRedirect(username, password);
    // If using new UI, ensure the home page is completed loaded.
    loginPage.getWaiter().until(ExpectedConditions.visibilityOfElementLocated(By.id("eqpageForm")));
  }

  public void logonWithNotice(String username, String password) {
    new LoginPage(context).load().loginWithNotice(username, password).acceptNotice();
    this.lastUsername = username;
    this.lastPassword = password;
    this.notice = true;
  }

  public void logonWithNotice(String username, String password, PageContext context) {
    new LoginPage(context).load().loginWithNotice(username, password).acceptNotice();
    this.lastUsername = username;
    this.lastPassword = password;
    this.notice = true;
  }

  public HomePage logonToHome(String username, String password) {
    logon(username, password);
    return new HomePage(context).load();
  }

  public LoginPage logout() {
    return new LoginPage(context).logout();
  }

  public LoginPage logout(PageContext context) {
    return new LoginPage(context).logout();
  }
}
