package com.tle.webtests.pageobject.viewitem;

import com.tle.common.Check;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class PackageViewer extends AbstractPage<PackageViewer> {

  public PackageViewer(PageContext context) {
    super(context, By.id("pv-content"));
  }

  public PackageViewer clickAttachment(String title) {
    By selectedPath =
        By.xpath(
            "//a[text()=" + quoteXPath(title) + "]/../../../div[contains(@class, 'selected')]");
    if (!Check.isEmpty(title) && !isPresent(selectedPath)) {
      driver.findElement(By.xpath("//a[text()=" + quoteXPath(title) + "]")).click();
      waitForElement(By.xpath("id('content1')/iframe"));
    }
    return get();
  }

  public boolean hasAttachmentNode(String attachmentName) {
    return isPresent(By.xpath("//a[text()=" + quoteXPath(attachmentName) + "]"));
  }

  public boolean selectedAttachmentContainsText(String text) {
    switchToFrame(0);
    boolean containsText =
        waiter.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), text));
    driver.switchTo().defaultContent();
    return containsText;
  }

  private void switchToFrame(int frame) {
    waitForElement(By.className("content-base"));
    WebElement theFrame = driver.findElement(By.xpath("id('content" + (frame + 1) + "')/iframe"));
    driver.switchTo().frame(theFrame);
    waitForBody();
  }

  public <T extends AbstractPage<T>> T switchToSelectedAttachment(T page) {
    driver.switchTo().defaultContent();
    driver.switchTo().frame(0);
    return page.get();
  }

  public PackageViewer returnToPackageViewer() {
    driver.switchTo().defaultContent();
    return get();
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

  public List<String> getTabOrder() {
    List<String> rv = new ArrayList<String>();
    switchToFrame(0);
    for (WebElement we : driver.findElements(By.xpath("id('tabs')//a/span"))) {
      rv.add(we.getText());
    }
    driver.switchTo().defaultContent();
    return rv;
  }

  public String tabText(String attachment, String tabName) {
    return tabText(attachment, tabName, 0);
  }

  public String tabText(String attachment, String tabName, int split) {
    clickAttachment(attachment);
    switchToFrame(split);
    driver.findElement(By.xpath("//a/span[text()=" + quoteXPath(tabName) + "]")).click();
    waiter.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("iframe-content"));
    By body = By.cssSelector("body");
    String text = waiter.until(ExpectedConditions.visibilityOfElementLocated(body)).getText();
    driver.switchTo().defaultContent();
    return text;
  }

  public SummaryPage clickTitle() {
    driver.findElement(By.xpath("//a[@class='brand']")).click();
    return new SummaryPage(context).get();
  }

  public boolean hasTitle() {
    return !driver.findElements(By.xpath("//a[@class='brand']")).isEmpty();
  }

  public void clickSplitView() {
    driver.findElement(EBy.linkText("Split View")).click();
  }

  public boolean selectedAttachmentIsLargeImage() {
    switchToFrame(0);
    LargeImageViewerPage liv = new LargeImageViewerPage(context).get();
    boolean onLiv = liv.zoomInButtonExists();
    driver.switchTo().defaultContent();
    return onLiv;
  }
}
