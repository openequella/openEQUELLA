package com.tle.webtests.pageobject.integration.canvas;

import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.SkipException;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CanvasLoginPage extends AbstractPage<CanvasLoginPage> {
  @FindBy(id = "pseudonym_session_unique_id")
  private WebElement user;

  @FindBy(id = "pseudonym_session_password")
  private WebElement pass;

  @FindBy(xpath = "id('login_form')//button[@type='submit']")
  private WebElement login;

  public CanvasLoginPage(PageContext context) {
    super(context, By.id("login_form"));
  }

  @Override
  protected void loadUrl() {
    if (Check.isEmpty(context.getIntegUrl())) {
      throw new SkipException("Canvas url not set");
    }
    driver.get(PathUtils.urlPath(context.getIntegUrl(), "login"));
  }

  public CanvasHomePage logon(String username, String password) {
    user.clear();
    user.sendKeys(username);
    pass.clear();
    pass.sendKeys(password);
    login.click();
    return new CanvasHomePage(context).get();
  }
}
