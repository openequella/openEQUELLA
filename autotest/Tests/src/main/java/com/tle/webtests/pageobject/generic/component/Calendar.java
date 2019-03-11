package com.tle.webtests.pageobject.generic.component;

import com.google.common.base.Throwables;
import com.tle.common.Check;
import com.tle.webtests.framework.Assert;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.text.MessageFormat;
import java.util.Date;
import java.util.TimeZone;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class Calendar extends AbstractPage<Calendar> {
  private final String baseId;

  private WebElement getField() {
    return driver.findElement(By.id(baseId + "vis"));
  }

  private WebElement getHiddenField() {
    return driver.findElement(By.id(baseId));
  }

  public Calendar(PageContext context, String baseId) {
    super(context);
    this.baseId = baseId;
  }

  @Override
  protected WebElement findLoadedElement() {
    return getField();
  }

  public void setDateHidden(Date date) {
    ((JavascriptExecutor) driver)
        .executeScript("arguments[0].value = arguments[1];", getHiddenField(), date.getTime());
  }

  // As far as I can tell, this is only used with 'conceptual' dates, which is
  // good.
  public <T extends PageObject> T setDate(Date date, WaitingPageObject<T> returnTo) {
    // Ummmm, this has to be UTC, not the timezone of the machine running
    // the auto test suite!!!
    java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
    cal.setTime(date);
    return setDate(cal, returnTo);
  }

  private void clickMonthYear(String type, int value) {
    WebElement sel =
        driver.findElement(
            By.xpath(
                "id('ui-datepicker-div')/div/div/select[@class="
                    + quoteXPath("ui-datepicker-" + type)
                    + "]"));
    Select select = new Select(sel);
    select.selectByValue(Integer.toString(value));
  }

  public <T extends PageObject> T setDate(java.util.Calendar cal, WaitingPageObject<T> returnTo) {
    WebElement imgElem = getField().findElement(By.xpath("following-sibling::img"));
    imgElem.click();
    waitForElement(By.id("ui-datepicker-div"));
    clickMonthYear("year", cal.get(java.util.Calendar.YEAR));
    clickMonthYear("month", cal.get(java.util.Calendar.MONTH));

    String dayXpath =
        "id(''ui-datepicker-div'')/table[@class=''ui-datepicker-calendar'']/tbody/tr/td/a[text()=''{0}'']";
    String day =
        MessageFormat.format(dayXpath, Integer.toString(cal.get(java.util.Calendar.DAY_OF_MONTH)));

    driver.findElement(By.xpath(day)).click();
    return returnTo.get();
  }

  /**
   * Utility method to get a pair of dates, from henceforth into the future, or the past, or
   * reversed, as specified by parameters.
   *
   * @param reverse
   * @param future
   * @return
   */
  public static java.util.Calendar[] getDateRange(TimeZone zone, boolean reverse, boolean future) {
    java.util.Calendar fromDate = java.util.Calendar.getInstance(zone);
    fromDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
    fromDate.set(java.util.Calendar.MINUTE, 0);
    fromDate.set(java.util.Calendar.SECOND, 0);
    fromDate.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar untilDate = (java.util.Calendar) fromDate.clone();
    int offset = future ? 1 : 0;
    fromDate.add(java.util.Calendar.DAY_OF_YEAR, offset);
    untilDate.add(java.util.Calendar.DAY_OF_YEAR, offset + 2);
    if (reverse) return new java.util.Calendar[] {untilDate, fromDate};
    return new java.util.Calendar[] {fromDate, untilDate};
  }

  public String getTextValue() {
    return getField().getAttribute("value");
  }

  public String getRawDateValue() {
    return getHiddenField().getAttribute("value");
  }

  public Date getHiddenDateValue() {
    final String rawDate = getRawDateValue();
    if (Check.isEmpty(rawDate)) {
      return null;
    }
    try {
      long ll = Long.parseLong(rawDate);
      return new Date(ll);
    } catch (NumberFormatException nfe) {
      throw Throwables.propagate(nfe);
    }
  }

  /**
   * Checks both the hidden and displayed date
   *
   * @param date
   * @return
   */
  public boolean dateEquals(java.util.Calendar date) {
    if (date == null && !isVisible()) {
      return true;
    }
    Date hiddenDate = getHiddenDateValue();
    if (date == null) {
      if (hiddenDate != null) {
        return false;
      }
    } else {
      if (hiddenDate == null) {
        return false;
      }

      java.util.Calendar c =
          java.util.Calendar.getInstance(context.getTestConfig().getBrowserTimeZone());
      c.setTime(hiddenDate);
      fieldMatches("Year", java.util.Calendar.YEAR, c, date);
      fieldMatches("Month", java.util.Calendar.MONTH, c, date);
      fieldMatches("Day", java.util.Calendar.DAY_OF_MONTH, c, date);
      return true;
    }
    return false;
  }

  private void fieldMatches(
      String fieldName, int field, java.util.Calendar c1, java.util.Calendar c2) {
    int c1v = c1.get(field);
    int c2v = c2.get(field);
    Assert.assertEquals(c1v, c2v, "Field " + fieldName + " didn't match");
  }

  public boolean isDisabled() {
    return !getField().isEnabled();
  }

  public String getBaseId() {
    return baseId;
  }
}
