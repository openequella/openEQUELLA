package io.github.openequella.pages.search;

import com.tle.webtests.framework.PageContext;

public class NewSearchPage extends AbstractSearchPage<NewSearchPage> {
  public NewSearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "page/search");
  }
}
