package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BlackboardEquellaWebServicePage extends AbstractPage<BlackboardEquellaWebServicePage> {

  @FindBy(id = "availablet")
  private WebElement available;

  @FindBy(id = "discoverablet")
  private WebElement discoverable;

  @FindBy(id = "sslRequiredf")
  private WebElement noSslRequired;

  @FindBy(name = "bottom_Submit")
  private WebElement save;

  public BlackboardEquellaWebServicePage(PageContext context) {
    super(context, BlackboardPageUtils.pageTitleBy("Edit: EQUELLA.WS"));
  }

  public void setAvailable() {
    available.click();
  }

  public void setDiscoverable() {
    discoverable.click();
  }

  public void setNoSslRequired() {
    noSslRequired.click();
  }

  public void save() {
    save.click();
  }
}
