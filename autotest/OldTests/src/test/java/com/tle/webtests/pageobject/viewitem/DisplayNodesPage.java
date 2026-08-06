package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DisplayNodesPage extends AbstractPage<DisplayNodesPage> {

  public DisplayNodesPage(PageContext context) {
    super(context, By.className("displayNodes"));
  }

  private WebElement getElementForName(String name) {
    return driver.findElement(
        By.xpath(
            "//div[contains(@class, 'displayNode') and ./h3[text()=" + quoteXPath(name) + "]]"));
  }

  public String getTextByName(String name) {
    WebElement entry = getElementForName(name);
    return entry.findElement(By.xpath("./p")).getText();
  }

  public String getLinkTextByName(String name) {
    WebElement entry = getElementForName(name);
    return entry.findElement(By.xpath("./p/a")).getText();
  }

  public boolean isHalfSizeByName(String name) {
    WebElement entry = getElementForName(name);
    return entry.getAttribute("class").contains("displayNodeHalf");
  }
}
