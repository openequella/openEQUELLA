package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BlackboardWebServicesListPage extends AbstractPage<BlackboardWebServicesListPage> {
  public BlackboardWebServicesListPage(PageContext context) {
    super(context, BlackboardPageUtils.pageTitleBy("Web Services"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "webapps/ws/wsadmin/wsadmin");
  }

  public BlackboardWebServicesListPage activateEquella() {

    equellaRow().findElement(By.xpath(".//a[contains(@title, 'EQUELLA.WS properties')]")).click();
    waitForElement(By.linkText("Edit"));
    driver.findElement(By.linkText("Edit")).click();
    BlackboardEquellaWebServicePage settings = new BlackboardEquellaWebServicePage(context).get();
    settings.setAvailable();
    settings.setDiscoverable();
    settings.save();
    waitForMsg();
    return get();
  }

  public BlackboardWebServicesListPage deleteEquella() {
    if (isEquellaInstalled()) {
      equellaRow().findElement(By.xpath(".//a[contains(@title, 'EQUELLA.WS properties')]")).click();
      waitForElement(By.linkText("Delete"));
      driver.findElement(By.linkText("Delete")).click();
      acceptConfirmation();
      waitForMsg();
    }
    return get();
  }

  public BlackboardWebServicesListPage refresh() {
    driver.findElement(By.linkText("Refresh")).click();
    return get();
  }

  private void waitForMsg() {
    waitForElement(By.id("goodMsg1"));
    driver.findElement(By.xpath("id('inlineReceipt_good')//a")).click();
  }

  private WebElement equellaRow() {
    return driver.findElement(By.xpath("//tr[./th[normalize-space(text())='EQUELLA.WS']]"));
  }

  public boolean isEquellaInstalled() {
    return !driver
        .findElements(By.xpath("//tr[./th[normalize-space(text())='EQUELLA.WS']]"))
        .isEmpty();
  }
}
