package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;

public class RejectMessagePage extends ModerationMessagePage<RejectMessagePage> {
  public RejectMessagePage(PageContext context) {
    super(context);
  }

  @Override
  public String getPfx() {
    return "_tasksrejectDialog";
  }
}
