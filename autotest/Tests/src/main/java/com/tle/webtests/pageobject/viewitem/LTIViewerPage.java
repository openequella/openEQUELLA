package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LTIViewerPage extends AbstractPage<LTIViewerPage> {
  // LTI viewers are dependent on the tool, this one is based on the eduapps
  // youtube tool: https://www.edu-apps.org/index.html?tool=youtube
  @FindBy(id = "search")
  private WebElement search;

  @FindBy(className = "load-more")
  private WebElement more;

  public LTIViewerPage(PageContext context) {
    super(context, By.className("lti-search"));
  }

  public void searchYoutube(String query) {
    search.sendKeys(query);
    search.sendKeys(Keys.RETURN);
    getWaiter().until(ExpectedConditions.presenceOfElementLocated(By.className("load-more")));
  }

  public String embedYTResult(int index) {
    String embedded =
        driver
            .findElement(
                By.xpath(
                    "//li[contains(@class,'list-item')]["
                        + index
                        + "]//h2[contains(@class,'title')]/a"))
            .getText();
    driver.findElement(By.xpath("//li[contains(@class, 'list-item')][" + index + "]")).click();
    return embedded;
  }
}
