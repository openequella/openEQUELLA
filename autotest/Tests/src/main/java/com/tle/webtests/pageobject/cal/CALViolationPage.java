package com.tle.webtests.pageobject.cal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CALViolationPage extends AbstractPage<CALViolationPage> {
  @FindBy(id = "cancelButton")
  private WebElement cancelButton;

  public CALViolationPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return cancelButton;
  }

  public <T extends PageObject> T okViolation(WaitingPageObject<T> returnTo) {
    WebElement errorDiv = driver.findElement(By.xpath("//div[@class='area error']"));
    WaitingPageObject<T> waiter =
        ExpectWaiter.waiter(ExpectedConditions.stalenessOf(errorDiv), returnTo);
    cancelButton.click();
    return waiter.get();
  }

  @Override
  protected void isError() {
    // Probably isn't
    // FIXME: need to be more specific
  }
}
