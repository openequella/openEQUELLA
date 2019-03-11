package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class SearchUrlSupportedParamsPage extends AbstractPage<SearchUrlSupportedParamsPage> {

  public SearchUrlSupportedParamsPage(PageContext context) {
    super(context, By.id("supported"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "searching.do?$DEBUG$");
  }

  public boolean hasParameter(String param) {
    return isPresent(By.xpath("id('supported')/table/tbody/tr/td[text()='" + param + "']"));
  }
}
