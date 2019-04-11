package com.tle.webtests.pageobject.cal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.function.Function;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CALOverridePage extends AbstractPage<CALOverridePage> {
  @FindBy(id = "reason")
  private WebElement reasonTextField;

  private final CALSummaryPage returnTo;

  public CALOverridePage(PageContext context, CALSummaryPage returnTo) {
    super(context, By.id("reason"));
    this.returnTo = returnTo;
  }

  private WebElement getContinueButton() {
    return find(driver, By.id("calpo_continueButton"));
  }

  public void setReason(String reason) {
    reasonTextField.sendKeys(reason);
    getWaiter()
        .until(
            (Function<WebDriver, Object>)
                webDriver -> {
                  try {
                    new WebDriverWait(webDriver, 1)
                        .until(ExpectedConditions.stalenessOf(getContinueButton()));
                    return false;
                  } catch (TimeoutException toe) {
                    return true;
                  }
                });
  }

  public CALSummaryPage clickContinue() {
    getContinueButton().click();
    return returnTo.get();
  }
}
