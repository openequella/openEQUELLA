package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class LinkPage extends AbstractPage<LinkPage> {

  public LinkPage(PageContext context) {
    super(context);
  }

  public boolean containsText(String text) {
    driver.switchTo().frame(0);
    waitForBody();
    boolean contains = driver.findElement(By.tagName("body")).getText().contains(text);
    restoreFrame();
    return contains;
  }

  private void waitForBody() {
    waiter.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver d) {
            try {
              driver.findElement(By.tagName("body"));
              return Boolean.TRUE;
            } catch (StaleElementReferenceException ste) {
              return Boolean.FALSE;
            }
          }
        });
  }

  public <T extends PageObject> T clickLink(WaitingPageObject<T> target) {
    driver.switchTo().frame(0);
    driver.findElement(By.xpath("//p/span/a[text()='Music History - The Beatles']")).click();
    return target.get();
  }

  public PackageViewer clickLink() {
    driver.switchTo().frame(0);
    driver.findElement(By.xpath("//p/span/a[text()='Music History - The Beatles']")).click();

    return new PackageViewer(context).get();
  }

  public PackageViewer clickLink2() {
    return clickLink2(true);
  }

  public PackageViewer clickLink2(boolean get) {
    driver.switchTo().frame(0);
    driver.findElement(By.xpath("//la/a[text()='Hey Jude']")).click();
    if (get) {
      return new PackageViewer(context).get();
    } else {
      return new PackageViewer(context);
    }
  }

  public void restoreFrame() {
    driver.switchTo().defaultContent();
  }
}
