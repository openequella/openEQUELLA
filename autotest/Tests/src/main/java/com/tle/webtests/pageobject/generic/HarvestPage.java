package com.tle.webtests.pageobject.generic;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class HarvestPage extends AbstractPage<HarvestPage> {
  public HarvestPage(PageContext context) {
    super(context, By.xpath("//h1[text()='Pages']"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "harvest.do");
  }

  public int getPageCount() {
    return driver.findElements(By.xpath("//ul[@class='pages']/li")).size();
  }

  public HarvestPage clickPage(final int i) {
    if (i != currentPage()) {
      driver.findElement(By.xpath("//ul[@class='pages']/li[" + quoteXPath(i) + "]/a")).click();
    }
    waiter.until(
        new ExpectedCondition<Boolean>() {
          private int currentPage;

          public Boolean apply(WebDriver driver) {
            currentPage = currentPage();
            return currentPage == i;
          }

          @Override
          public String toString() {
            return String.format(
                "Current page to be to be \"%d\". Current page: \"%d\"", i, currentPage);
          }
        });
    return get();
  }

  public int currentPage() {
    return Integer.valueOf(
        driver.findElement(By.xpath("//ul[@class='pages']/li[not(a)]")).getText().trim());
  }

  public boolean hasItem(String title) {
    return isPresent(By.xpath("//ul/li/a[text()=" + quoteXPath(title) + "]"));
  }

  public SummaryPage clickItem(String title) {
    driver.findElement(By.xpath("//ul/li/a[text()=" + quoteXPath(title) + "]")).click();
    return new SummaryPage(context).get();
  }

  public boolean hasMeta(String name, String content) {
    return isPresent(
        By.xpath(
            "//head/meta[@name="
                + quoteXPath(name)
                + " and @content="
                + quoteXPath(content)
                + "]"));
  }
}
