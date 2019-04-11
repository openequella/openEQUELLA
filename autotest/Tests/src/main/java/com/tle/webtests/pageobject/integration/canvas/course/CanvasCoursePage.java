package com.tle.webtests.pageobject.integration.canvas.course;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;

public class CanvasCoursePage extends AbstractCanvasCoursePage<CanvasCoursePage> {
  public CanvasCoursePage(PageContext context, String courseName) {
    super(
        context,
        By.xpath(
            "id('section-tabs-header')[normalize-space(text())="
                + quoteXPath(truncCourseName(courseName))
                + "]"));
  }

  private static String truncCourseName(String courseName) {
    return courseName.split("\\s")[0];
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "courses/1");
  }
}
