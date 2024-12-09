package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class BlackboardProxyPage extends AbstractPage<BlackboardProxyPage> {

  public BlackboardProxyPage(PageContext context) {
    super(context, By.id("listContainer"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "/webapps/ws/wsadmin/wsclientprograms");
  }

  public boolean hasEquella() {
    return isPresent(By.xpath("//td[normalize-space(text())='EQUELLA']"));
  }

  /**
   * @return The shared secret
   */
  public String setAvailable() {
    driver.findElement(By.xpath("//a[normalize-space(@title)='Pearson EQUELLA Edit']")).click();
    waitForElement(By.xpath("//a[@title='Edit']"));
    driver.findElement(By.xpath("//a[@title='Edit']")).click();

    BlackboardEquellaProxyPage page = new BlackboardEquellaProxyPage(context);
    page.setAvailable();
    String password = page.getPassword();
    page.save();
    waitForMsg();
    return password;
  }

  private BlackboardProxyPage waitForMsg() {
    waitForElement(By.id("goodMsg1"));
    driver.findElement(By.xpath("id('inlineReceipt_good')//a")).click();
    return get();
  }
}
