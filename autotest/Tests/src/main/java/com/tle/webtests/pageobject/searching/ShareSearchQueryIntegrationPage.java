package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ShareSearchQueryIntegrationPage extends AbstractPage<ShareSearchQueryIntegrationPage> {
  @FindBy(className = "sharefield")
  private WebElement urlFiled;

  public ShareSearchQueryIntegrationPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return urlFiled;
  }

  public String getSharedUrl() {
    waitForElement(urlFiled);
    return urlFiled.getAttribute("value");
  }
}
