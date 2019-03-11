package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.ui.WebDriverWait;

public interface PageObject {
  void checkLoaded() throws NotFoundException;

  PageContext getContext();

  SearchContext getSearchContext();

  long getRefreshTime();

  WebDriverWait getWaiter();
}
