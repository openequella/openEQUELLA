package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class ModerationMessagePage<M extends AbstractWizardTab<M>>
    extends AbstractWizardTab<M> {
  private WebElement getMessageField() {
    return findByPfx("_commentField");
  }

  private WebElement findByPfx(String postfix) {
    return findWithId(getPfx(), postfix);
  }

  private WebElement getStepList() {
    return findByPfx("_rejectSteps");
  }

  private WebElement getOkButton() {
    return findByPfx("_ok");
  }

  private WebElement getCancelButton() {
    return findByPfx("_c");
  }

  public ModerationMessagePage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getMessageField();
  }

  public abstract String getPfx();

  public TaskListPage rejectWithMessage(String message, String toStep) {
    getMessageField().clear();
    getMessageField().sendKeys(message);
    if (toStep != null) {
      new EquellaSelect(context, getStepList()).selectByVisibleText(toStep);
    }
    getOkButton().click();
    return ReceiptPage.waiter("Successfully rejected previous task", new TaskListPage(context))
        .get();
  }

  public TaskListPage acceptWithMessage(String message) {
    getMessageField().clear();
    getMessageField().sendKeys(message);
    getOkButton().click();
    return ReceiptPage.waiter("Successfully approved previous task", new TaskListPage(context))
        .get();
  }

  public ModerationView addModerationComment(String message) {
    getMessageField().clear();
    getMessageField().sendKeys(message);

    ModerationView moderationView = new ModerationView(context);
    moderationView.checkLoaded();
    WaitingPageObject<ModerationView> modViewWaiter = moderationView.updateWaiter();
    getOkButton().click();
    return modViewWaiter.get();
  }

  public ModerationView cancel() {
    By dialogById = By.id(getPfx());
    WebElement dialogElem = driver.findElement(dialogById);
    getCancelButton().click();
    WebDriverWait waiter = getWaiter();
    waiter.until(ExpectedConditions.stalenessOf(dialogElem));
    waiter.until(ExpectedConditions.invisibilityOfElementLocated(dialogById));
    return new ModerationView(context).get();
  }
}
