package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.support.ui.WebDriverWait;

public interface WaitingPageObject<T extends PageObject> {
  T get();

  WebDriverWait getWaiter();

  PageContext getContext();
}
