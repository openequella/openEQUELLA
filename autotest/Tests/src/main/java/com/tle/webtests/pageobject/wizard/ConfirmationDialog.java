package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ConfirmationDialog extends AbstractPage<ConfirmationDialog> {
  public static enum ConfirmButton {
    COMPLETE("1_complete"),
    PUBLISH("1_publish"),
    SUBMIT_FOR_MOD("1_submit"),
    CANCEL("1_cancel"),
    DRAFT("1_draft");

    private final String id;

    private ConfirmButton(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  public ConfirmationDialog(PageContext context) {
    super(context, By.id("savePrompt"), 60);
  }

  public <T extends AbstractPage<T>> T finishInvalid(WaitingPageObject<T> page) {
    confirm(ConfirmButton.COMPLETE);
    return page.get();
  }

  public <T extends AbstractPage<T>> T publishInvalid(WaitingPageObject<T> page) {
    confirm(ConfirmButton.PUBLISH);
    return page.get();
  }

  public WizardErrorPage publishErrorPage(WizardErrorPage page) {
    confirm(ConfirmButton.PUBLISH);
    // Wait for the error page is completed loaded
    waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.error")));
    return page.get();
  }

  public SummaryPage publish() {
    confirm(ConfirmButton.PUBLISH);
    return new SummaryPage(context).get();
  }

  private void confirm(ConfirmButton cb) {
    WebElement confirmButton =
        getWaiter().until(ExpectedConditions.elementToBeClickable(By.id(cb.getId())));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmButton);
  }

  public SummaryPage submit() {
    confirm(ConfirmButton.SUBMIT_FOR_MOD);
    return new SummaryPage(context).get();
  }

  public SummaryPage draft() {
    confirm(ConfirmButton.DRAFT);
    return new SummaryPage(context).get();
  }

  public <T extends AbstractPage<T>> T cancel(T page) {
    confirm(ConfirmButton.CANCEL);
    return page.get();
  }

  public void addModerationComment(String comment) {
    driver.findElement(By.id("1_m")).sendKeys(comment);
  }

  public boolean containsButton(ConfirmButton cb) {
    try {
      driver.findElement(By.id(cb.getId()));
      return true;
    } catch (NoSuchElementException thrown) {
      return false;
    }
  }
}
