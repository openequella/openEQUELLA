package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;

public class MyResourcesPortalEditPage extends AbstractPortalEditPage<MyResourcesPortalEditPage> {
  public MyResourcesPortalEditPage(PageContext context) {
    super(context);
  }

  @Override
  public String getType() {
    return "My resources";
  }

  @Override
  public String getId() {
    return "mrpe";
  }
}
