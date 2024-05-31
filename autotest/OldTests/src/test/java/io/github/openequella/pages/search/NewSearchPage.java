package io.github.openequella.pages.search;

import com.tle.webtests.framework.PageContext;

public class NewSearchPage extends BaseSearchPage<NewSearchPage> {
  public NewSearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "page/search");
  }
}
