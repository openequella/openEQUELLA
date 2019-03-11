package com.tle.webtests.pageobject.oauth;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OAuthLogonPage extends AbstractPage<OAuthLogonPage> {
  @FindBy(id = "oal_denyButton")
  private WebElement denyButton;

  @FindBy(id = "oal_authButton")
  private WebElement authoriseButton;

  @FindBy(id = "oal_allowButton")
  private WebElement allowButton;

  @FindBy(id = "username")
  private WebElement usernameField;

  @FindBy(id = "password")
  private WebElement passwordField;

  @FindBy(xpath = "//p[@class='oauth_error']")
  private WebElement logonError;

  public OAuthLogonPage(PageContext context) {
    super(context, By.id("oal_denyButton"));
  }

  public <T extends AbstractPage<T>> T logon(
      String username, String password, AbstractPage<T> redirTo) {
    if (!Check.isEmpty(username)) {
      setUsername(username);
    }
    setPassword(password);
    authoriseButton.click();
    return redirTo.get();
  }

  public <T extends AbstractPage<T>> T logon(String password, AbstractPage<T> redirTo) {
    return logon(null, password, redirTo);
  }

  public void setUsername(String username) {
    usernameField.clear();
    usernameField.sendKeys(username);
  }

  public void setPassword(String password) {
    passwordField.clear();
    passwordField.sendKeys(password);
  }

  public <T extends AbstractPage<T>> T allowAccess(AbstractPage<T> redirTo) {
    allowButton.click();
    return redirTo.get();
  }

  public <T extends AbstractPage<T>> T denyAccess(AbstractPage<T> redirTo) {
    denyButton.click();
    return redirTo.get();
  }

  public static OAuthLogonPage authorise(
      PageContext context,
      String clientId,
      String redirectUri,
      String responseType,
      String... otherParams) {
    List<BasicNameValuePair> params = Lists.newArrayList();
    params.add(new BasicNameValuePair("client_id", clientId));
    params.add(new BasicNameValuePair("redirect_uri", redirectUri));
    params.add(new BasicNameValuePair("response_type", responseType));
    for (int i = 0; i < otherParams.length; i++) {
      params.add(new BasicNameValuePair(otherParams[i], otherParams[++i]));
    }
    context
        .getDriver()
        .get(context.getBaseUrl() + "oauth/authorise?" + URLEncodedUtils.format(params, "UTF-8"));
    return new OAuthLogonPage(context).get();
  }

  public boolean isAlreadyLoggedIn() {
    return isPresent(allowButton);
  }

  public OAuthLogonPage logonError(String username, String password) {
    if (!Check.isEmpty(username)) {
      setUsername(username);
    }
    setPassword(password);
    authoriseButton.click();
    waitForElement(logonError);
    return get();
  }

  public OAuthLogonPage logonError(String password) {
    return logonError(null, password);
  }

  public String getLogonErrorText() {
    return logonError.getText();
  }
}
