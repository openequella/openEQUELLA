package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import java.text.MessageFormat;
import org.openqa.selenium.By;

public class TaskStatisticsPortalSection
    extends AbstractPortalSection<TaskStatisticsPortalSection> {
  public TaskStatisticsPortalSection(PageContext context, String title) {
    super(context, title);
  }

  public boolean isTrendSelected(String trend) {
    return isPresent(
        By.xpath(
            MessageFormat.format(
                "//div[@id=\"ptsprtrendselector\"]//strong[text()={0}]", quoteXPath(trend))));
  }
}
