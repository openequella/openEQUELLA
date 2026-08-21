package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import java.util.Iterator;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class ModifyKeyResourcePage extends AbstractPage<ModifyKeyResourcePage> {

  public ModifyKeyResourcePage(PageContext context) {
    super(context, By.xpath("//h2[text()='Modify key resource']"));
  }

  public static By getByForTopic(int index) {
    return By.xpath("//input[@id='itemresult-wrapper'][" + index + "]");
  }

  public ModifyKeyResourcePage removeKeyResourceFromHierarchy(String topic) {
    expandAll();
    WebElement selectedTopic =
        driver.findElement(By.xpath("//span[text()=" + quoteXPath(topic) + "]/../input"));
    if (selectedTopic.isSelected()) {
      selectedTopic.click();
    }
    driver.findElement(By.id("ht_addButton")).click();
    return ReceiptPage.waiter("Successfully modified your key resource", this).get();
  }

  public ModifyKeyResourcePage addToHierarchy(String topic) {
    expandAll();
    WebElement selectedTopic =
        driver.findElement(By.xpath("//span[text()=" + quoteXPath(topic) + "]/../input"));
    if (!selectedTopic.isSelected()) {
      selectedTopic.click();
    }
    driver.findElement(By.id("ht_addButton")).click();
    return ReceiptPage.waiter("Successfully modified your key resource", this).get();
  }

  private void expandAll() {
    List<WebElement> elements =
        driver.findElements(By.xpath("//li[contains(@class,'expandable')]/div"));
    while (elements.size() > 0) {
      for (Iterator<WebElement> eles = elements.iterator(); eles.hasNext(); ) {
        final WebElement webElement = eles.next();
        webElement.click();
        waiter.until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                boolean expanded = !webElement.getAttribute("class").contains("expandable");
                return expanded && !isVisible(webElement, By.className("placeholder"));
              }
            });
      }
      elements = driver.findElements(By.xpath("//li[contains(@class,'expandable')]/div"));
    }
  }
}
