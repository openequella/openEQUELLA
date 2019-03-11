package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;

public class BrowsePortalEditPage extends AbstractPortalEditPage<BrowsePortalEditPage> {
  public BrowsePortalEditPage(PageContext context) {
    super(context);
  }

  @Override
  public String getType() {
    return "Browse";
  }

  @Override
  public String getId() {
    return "ebrs";
  }
}
