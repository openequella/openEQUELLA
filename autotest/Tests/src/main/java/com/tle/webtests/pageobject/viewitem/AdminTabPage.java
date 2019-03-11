package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AdminTabPage extends ItemPage<AdminTabPage> {
  @FindBy(xpath = "//h3[text()='Actions']")
  private WebElement actionsElem;

  public AdminTabPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return actionsElem;
  }
}
