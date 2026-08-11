package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DateFormatSettingPage extends AbstractPage<DateFormatSettingPage> {
  @FindBy(id = "_dateFormats")
  private WebElement ul;

  @FindBy(id = "_dateFormats_0")
  private WebElement radioApprox;

  @FindBy(id = "_dateFormats_1")
  private WebElement radioExact;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  public DateFormatSettingPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return ul;
  }

  public void setApproxDateFormat() {
    radioApprox.click();
  }

  public void setExactDateFormat() {
    radioExact.click();
  }

  public void saveSettings() {
    saveButton.click();
  }

  public boolean isApproxDateFormat() {
    return radioApprox.isSelected();
  }

  public boolean isExactDateFormat() {
    return radioExact.isSelected();
  }
}
