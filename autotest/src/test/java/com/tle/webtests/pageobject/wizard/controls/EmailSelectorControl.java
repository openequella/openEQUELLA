package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EmailSelectorControl extends AbstractWizardControl<EmailSelectorControl> {
  private By getRootBy() {
    return wizIdBy("emailControl");
  }

  private WebElement getEmailField() {
    return byWizId("_e");
  }

  private WebElement getDialogOpener() {
    return byWizId("_s_opener");
  }

  private WebElement getAddButton() {
    return byWizId("_a");
  }

  private By getEmailError() {
    return wizIdIdXPath(
        "emailControl", "/div[@class='noemailwarning' and text() = 'Invalid email address']");
  }

  public EmailSelectorControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page) {
    super(context, ctrlnum, page);
  }

  @Override
  protected WebElement findLoadedElement() {
    return driver.findElement(getRootBy());
  }

  public void addEmail(String email) {
    addEmail(email, false);
  }

  public void addEmail(String email, boolean invalid) {
    WaitingPageObject<EmailSelectorControl> waiter;
    if (!invalid) {
      waiter = selectedWaiter(email);
    } else {
      waiter =
          ExpectWaiter.waiter(ExpectedConditions.presenceOfElementLocated(getEmailError()), this);
    }
    getEmailField().clear();
    getEmailField().sendKeys(email);
    getAddButton().click();
    waiter.get();
  }

  private String xpathForEmail(String email) {
    return "//tr[td[contains(@class,'name') and normalize-space(text()) = "
        + quoteXPath(email)
        + "]]";
  }

  public SelectUserDialog openDialog() {
    getDialogOpener().click();
    return new SelectUserDialog(context, page.subComponentId(ctrlnum, "s")).get();
  }

  public AbstractWizardControlPage<?> queryAndSelect(String query, String username) {
    openDialog().search(query).selectAndFinish(username, selectedWaiter(username));
    return page;
  }

  public boolean isAddAvailable() {
    try {
      return getAddButton().isDisplayed();
    } catch (NotFoundException nfe) {
      return false;
    }
  }

  public EmailSelectorControl removeEmail(String email) {
    WaitingPageObject<EmailSelectorControl> waiter = removedWaiter(email);
    String xpathExpression = xpathForEmail(email) + "/td/a";
    driver.findElement(new ByChained(getRootBy(), By.xpath(xpathExpression))).click();
    acceptConfirmation();
    return waiter.get();
  }

  public boolean isInvalidEmail() {
    return isPresent(getEmailError());
  }

  public WaitingPageObject<EmailSelectorControl> selectedWaiter(String newlySelected) {
    return ExpectWaiter.waiter(
        ExpectedConditions.and(
            updatedCondition(),
            ExpectedConditions.visibilityOfElementLocated(
                new ByChained(getRootBy(), By.xpath(xpathForEmail(newlySelected))))),
        this);
  }

  public WaitingPageObject<EmailSelectorControl> removedWaiter(String removed) {
    return ExpectWaiter.waiter(
        ExpectedConditions.invisibilityOfElementLocated(
            new ByChained(getRootBy(), By.xpath(xpathForEmail(removed)))),
        this);
  }
}
