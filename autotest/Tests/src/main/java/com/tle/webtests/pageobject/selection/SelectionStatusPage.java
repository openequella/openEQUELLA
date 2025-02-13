package com.tle.webtests.pageobject.selection;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SelectionStatusPage extends AbstractPage<SelectionStatusPage> {
  @FindBy(id = "ss_finishedButton")
  private WebElement finishedButton;

  public SelectionStatusPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return finishedButton;
  }

  public SelectionCheckoutPage finishSelections() {
    scrollToElement(finishedButton);
    finishedButton.click();
    return new SelectionCheckoutPage(context).get();
  }
}
