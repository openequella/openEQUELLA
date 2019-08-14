package com.tle.webtests.pageobject.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.AbstractReport;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class ReportingPage extends AbstractPage<ReportingPage> {
  @FindBy(xpath = "id('_r')//th[contains(@class,'sortedasc')]")
  private WebElement columnSort;

  public ReportingPage(PageContext context) {
    super(context);
    loadedBy = byForPageTitle("Reports");
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/reports.do");
  }

  public boolean isReportExisting(String reportName) {
    return isPresent(getReportLinkBy(reportName));
  }

  private By getReportLinkBy(String reportName) {
    return By.xpath("//tr/td[1]/a[text()=" + quoteXPath(reportName) + "]");
  }

  public <R extends AbstractReport<R>> NoParamsReportWindow<R> getReport(
      String reportName, R report) {
    return getReport(reportName, new NoParamsReportWindow<R>(context, report));
  }

  public <R extends AbstractReport<R>, T extends AbstractReportWindow<R, T>> T getReport(
      String reportName, T page) {
    final String currentWindowHandle = driver.getWindowHandle();
    page.setWindowHandle(currentWindowHandle);

    waitForElement(columnSort);
    ExpectedCondition<String> newWindowExpectation =
        ExpectedConditions2.newWindowOpenedAndSwitchedTo(driver);
    driver.findElement(getReportLinkBy(reportName)).click();
    acceptConfirmation("Are you sure you want to run '" + reportName + "'?");
    return ExpectWaiter.waiter(newWindowExpectation, page).get();
  }

  public <R extends AbstractReport<R>> NoParamsReportWindow<R> getSubReport(R report) {
    return new NoParamsReportWindow<R>(context, report).get();
  }

  public boolean isNoReportsAvailable() {
    return isPresent(By.xpath("//h2[text()='There are no reports that you can generate']"));
  }
}
