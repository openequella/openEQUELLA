package com.tle.webtests.test.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.reporting.AbstractReportWindow;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MultiValueReportOptionsPage
    extends AbstractReportWindow<MultiValueReportPage, MultiValueReportOptionsPage> {
  @FindBy(id = "c1r")
  private WebElement singleRightArrowButton;

  @FindBy(id = "c1_left")
  private WebElement leftSelect;

  public MultiValueReportOptionsPage(PageContext context) {
    super(context, new MultiValueReportPage(context));
  }

  public void selectValueFromLeft(String value) {
    leftSelect.findElement(By.xpath(".//option[@value=" + quoteXPath(value) + "]")).click();
    singleRightArrowButton.click();
  }
}
