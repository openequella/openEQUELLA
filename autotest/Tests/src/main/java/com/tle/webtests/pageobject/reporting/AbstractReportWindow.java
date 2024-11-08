package com.tle.webtests.pageobject.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.AbstractReport;
import com.tle.webtests.pageobject.ExpectWaiter;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AbstractReportWindow<R extends AbstractReport<R>, T extends AbstractReportWindow<R, T>>
    extends AbstractPage<T> {
  private String windowHandle;

  @FindBy(id = "_submitButton")
  private WebElement executeButton;

  @FindBy(id = "_paramsButton")
  private WebElement paramsButton;

  private R report;

  public AbstractReportWindow(PageContext context, R report) {
    super(context, By.id("report-button-bar"), 240);
    this.report = report;
    report.setReportWindow(this);
  }

  public boolean isEditParameterButtonEnabled() {
    return !driver.findElements(By.id("_paramsButton")).isEmpty();
  }

  public void close() {
    driver.close();
    driver.switchTo().window(windowHandle);
    driver.switchTo().defaultContent();
  }

  public R getReport() {
    report.getWaiter().withTimeout(5, TimeUnit.MINUTES);
    return ExpectWaiter.waiter(
            ExpectedConditions.frameToBeAvailableAndSwitchToIt("reportFrame"), report)
        .get();
  }

  public R execute() {
    executeButton.click();
    return getReport();
  }

  public void setWindowHandle(String windowHandle) {
    this.windowHandle = windowHandle;
  }

  public void select() {
    driver.switchTo().defaultContent();
  }
}
