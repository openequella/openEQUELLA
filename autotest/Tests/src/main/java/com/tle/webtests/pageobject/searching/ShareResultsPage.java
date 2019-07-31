package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class ShareResultsPage extends AbstractPage<ShareResultsPage> {
  public ShareResultsPage(PageContext context, AbstractSearchPage<?, ?, ?> from) {
    super(context, By.id("sharesave_nameField"));
  }
}
