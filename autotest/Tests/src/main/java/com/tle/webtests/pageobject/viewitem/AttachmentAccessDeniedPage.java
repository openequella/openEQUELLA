package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ErrorPage;

public class AttachmentAccessDeniedPage extends ErrorPage {
  String URL;

  public AttachmentAccessDeniedPage(PageContext context, String url) {
    super(context);
    this.URL = url;
  }

  @Override
  protected void loadUrl() {
    driver.get(URL);
  }

  @Override
  protected boolean isNewUI() {
    return false;
  }
}
