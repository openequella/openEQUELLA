package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public abstract class AbstractBlackboardCoursePage<T extends AbstractBlackboardCoursePage<T>>
    extends AbstractPage<T> {
  protected String courseName;
  protected String title;

  public AbstractBlackboardCoursePage(PageContext context, String courseName, String title) {
    super(
        context,
        By.xpath(
            "id('breadcrumbs')[//a[text()="
                + quoteXPath(courseName)
                + "] and //li[contains(text(), "
                + quoteXPath(title)
                + ")]]"));
    this.courseName = courseName;
    this.title = title;
  }
}
