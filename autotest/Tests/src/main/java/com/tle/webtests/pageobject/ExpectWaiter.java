package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ExpectWaiter<T extends PageObject> implements WaitingPageObject<T> {
  private ExpectedCondition<?> condition;
  private WaitingPageObject<? extends T> other;
  private WebDriverWait waiter;

  protected ExpectWaiter(
      ExpectedCondition<?> condition, WebDriverWait waiter, WaitingPageObject<? extends T> other) {
    this.condition = condition;
    this.other = other;
    this.waiter = waiter;
  }

  @Override
  public WebDriverWait getWaiter() {
    return waiter;
  }

  @Override
  public T get() {
    waiter.until(condition);
    return other.get();
  }

  @Override
  public PageContext getContext() {
    return other.getContext();
  }

  public static <T extends PageObject> ExpectWaiter<T> waiter(
      ExpectedCondition<?> condition, WaitingPageObject<? extends T> other) {
    return new ExpectWaiter<T>(condition, other.getWaiter(), other);
  }
}
