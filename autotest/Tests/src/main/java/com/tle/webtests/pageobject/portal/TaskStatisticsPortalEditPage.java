package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import java.text.MessageFormat;
import org.openqa.selenium.By;

public class TaskStatisticsPortalEditPage
    extends AbstractPortalEditPage<TaskStatisticsPortalEditPage> {
  public TaskStatisticsPortalEditPage(PageContext context) {
    super(context, By.id("tspe_t"));
  }

  @Override
  public String getType() {
    return "Task statistics";
  }

  @Override
  public String getId() {
    return "tspe";
  }

  public void setTrend(String trend) {
    driver
        .findElement(By.xpath(MessageFormat.format("//input[@value={0}]", quoteXPath(trend))))
        .click();
  }
}
