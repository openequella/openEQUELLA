package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.Calendar;
import java.util.Date;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CourseDefaultsPage extends AbstractPage<CourseDefaultsPage> {
  public static String START_PREFIX_ID = "_sdt"; // full id is _sdtvis
  public static String END_PREFIX_ID = "_edt"; // full id is _edtvis
  public static final String COURSE_DEFAULTS_SETTINGS_SECTION_TITLE = "Course defaults";

  @FindBy(id = "_saveButton")
  private WebElement save;

  public CourseDefaultsPage(PageContext context) {
    super(
        context,
        By.xpath("//h2[text()=" + quoteXPath(COURSE_DEFAULTS_SETTINGS_SECTION_TITLE) + ']'));
  }

  public Calendar getStartDate(PageContext context) {
    return new Calendar(context, START_PREFIX_ID).get();
  }

  public Calendar getEndDate(PageContext context) {
    return new Calendar(context, END_PREFIX_ID).get();
  }

  public CourseDefaultsPage save() {
    save.click();
    return get();
  }

  public boolean isDateError() {
    try {
      return driver
          .findElement(By.className("mandatory"))
          .getText()
          .contains("'From' date must come before 'until'.");
    } catch (NotFoundException nfe) {
      return false;
    }
  }

  public void setStartDate(java.util.Calendar date) {
    getStartDate(context).setDate(date, this);
  }

  public void setEndDate(java.util.Calendar date) {
    getEndDate(context).setDate(date, this);
  }

  public Date getStartDate() {
    return getStartDate(context).getHiddenDateValue();
  }

  public Date getEndDate() {
    return getEndDate(context).getHiddenDateValue();
  }
}
