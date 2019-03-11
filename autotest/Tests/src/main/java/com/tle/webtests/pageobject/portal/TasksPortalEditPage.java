package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class TasksPortalEditPage extends AbstractPortalEditPage<TasksPortalEditPage> {
  public TasksPortalEditPage(PageContext context) {
    super(context, By.id("tpe_t"));
  }

  @Override
  public String getType() {
    return "Tasks";
  }

  @Override
  public String getId() {
    return "tpe";
  }
}
