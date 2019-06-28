package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class WizardErrorPage extends AbstractPage<WizardErrorPage> {

  public WizardErrorPage(PageContext context) {
    super(context);
    loadedBy =
        isNewUI()
            ? byForPageTitle("Internal Server Error")
            : By.xpath("//h2[text()='An error occurred in the wizard']");
  }

  public String getError() {
    return driver
        .findElement(isNewUI() ? By.xpath("id('errorPage')//h5") : By.cssSelector("div.error p"))
        .getText();
  }
}
