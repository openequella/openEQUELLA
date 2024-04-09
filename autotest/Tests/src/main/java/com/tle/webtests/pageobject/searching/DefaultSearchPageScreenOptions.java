package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;

public class DefaultSearchPageScreenOptions
    extends AbstractSearchPageScreenOptions<DefaultSearchPageScreenOptions> {

  public DefaultSearchPageScreenOptions(PageContext context) {
    super(context);
  }

  // TODO: Remove me in OEQ-1702.
  public DefaultSearchPageScreenOptions(PageContext context, Boolean forceOldUI) {
    super(context, forceOldUI);
  }
}
