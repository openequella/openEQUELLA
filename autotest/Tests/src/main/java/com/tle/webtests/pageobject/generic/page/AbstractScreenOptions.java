package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AbstractScreenOptions<T extends AbstractScreenOptions<T>> extends AbstractPage<T> {
  @FindBy(id = "bluebar_screenoptions_button")
  private WebElement openOptions;

  public AbstractScreenOptions(PageContext context) {
    super(context, By.id("bluebar_screenoptions"));
  }

  public T open() {
    if (!isPresent(By.id("bluebar_screenoptions"))) {
      openOptions.click();
    }
    return get();
  }

  public void close() {
    if (isPresent(By.id("bluebar_screenoptions"))) {
      openOptions.click();
    }
  }
}
