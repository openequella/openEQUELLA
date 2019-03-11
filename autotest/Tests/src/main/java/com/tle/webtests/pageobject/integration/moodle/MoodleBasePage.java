package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class MoodleBasePage<T extends MoodleBasePage<T>> extends AbstractPage<T> {
  protected int moodleVersion;

  public int getMoodleVersion() {
    return moodleVersion;
  }

  public void setMoodleVersion(int moodleVersion) {
    this.moodleVersion = moodleVersion;
  }

  public MoodleBasePage(MoodleBasePage<?> parent) {
    this(parent.getContext());
  }

  public MoodleBasePage(
      PageContext context, SearchContext relativeTo, By loadedBy, WebDriverWait waiter) {
    super(context, relativeTo, loadedBy, waiter);
    this.moodleVersion = context.getAttribute("moodle_version");
  }

  public MoodleBasePage(PageContext context, By loadedBy, int timeOut) {
    this(context, context.getDriver(), loadedBy, timeOut);
  }

  public MoodleBasePage(PageContext context, SearchContext searchContext, By loadedBy) {
    this(context, searchContext, loadedBy, -1);
  }

  public MoodleBasePage(
      PageContext context, SearchContext searchContext, By loadedBy, int timeOut) {
    this(
        context,
        searchContext,
        loadedBy,
        new WebDriverWait(
            context.getDriver(),
            timeOut == -1 ? context.getTestConfig().getStandardTimeout() : timeOut,
            50));
  }

  public MoodleBasePage(PageContext context, By loadedBy) {
    this(context, loadedBy, -1);
  }

  public MoodleBasePage(PageContext context) {
    this(context, null, -1);
  }

  public MoodleBasePage(PageContext context, WebDriverWait waiter) {
    this(context, context.getDriver(), null, waiter);
  }

  public void scrollIntoViewAndClick(WebElement toClick) {
    Point loc = toClick.getLocation();
    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + (loc.y - 80) + ")", "");
    toClick.click();
  }
}
