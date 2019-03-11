package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.WebElement;

public abstract class AbstractWizardControl<T extends AbstractWizardControl<T>>
    extends NewAbstractWizardControl<T> {
  private WebElement getInvalidMessageWE() {
    return byWizIdXPath("//div/p[@class='ctrlinvalidmessage']");
  }

  public AbstractWizardControl(
      PageContext context, int ctrlnum, AbstractWizardControlPage<?> page) {
    super(context, ctrlnum, page);
  }

  @Override
  protected abstract WebElement findLoadedElement();

  public boolean isShowingAnyError() {
    return getInvalidMessageWE().isDisplayed();
  }

  public String getInvalidMessage() {
    return (isShowingAnyError() ? getInvalidMessageWE().getText() : "");
  }
}
