package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FilterByKeywordPage extends AbstractQuerySection<FilterByKeywordPage> {

  private final String buttonId;

  public FilterByKeywordPage(PageContext context) {
    this(context, "fbakw_s");
  }

  public FilterByKeywordPage(PageContext context, String buttonId) {
    super(context);
    this.buttonId = buttonId;
  }

  protected WebElement getSearchButton() {
    return driver.findElement(By.id(buttonId));
  }

  public String getButtonId() {
    return buttonId;
  }
}
