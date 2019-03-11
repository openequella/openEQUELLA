package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BlackboardEquellaProxyPage extends AbstractPage<BlackboardEquellaProxyPage> {
  @FindBy(id = "password")
  private WebElement password;

  @FindBy(id = "availabilityAV")
  private WebElement available;

  @FindBy(name = "bottom_Submit")
  private WebElement save;

  public BlackboardEquellaProxyPage(PageContext context) {
    super(context, By.id("pageTitleText"));
  }

  public void setAvailable() {
    available.click();
  }

  public String getPassword() {
    return password.getAttribute("value");
  }

  public void save() {
    save.click();
  }
}
