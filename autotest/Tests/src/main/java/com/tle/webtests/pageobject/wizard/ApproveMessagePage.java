package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;

public class ApproveMessagePage extends ModerationMessagePage<ApproveMessagePage> {
  public ApproveMessagePage(PageContext context) {
    super(context);
  }

  @Override
  public String getPfx() {
    return "_tasksapproveDialog";
  }
}
