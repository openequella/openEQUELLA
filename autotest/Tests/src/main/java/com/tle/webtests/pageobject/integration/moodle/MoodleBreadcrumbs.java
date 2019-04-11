package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodleBreadcrumbs extends AbstractPage<MoodleBreadcrumbs> {
  @FindBy(xpath = "//*[@id='page-header']//ul")
  private WebElement headerElem;

  public MoodleBreadcrumbs(PageContext context) {
    super(context, By.xpath("//*[@id='page-header']//ul"));
  }

  public void clickByTitle(String title) {
    headerElem.findElement(By.xpath(".//a[@title=" + quoteXPath(title) + "]")).click();
  }

  public <T extends PageObject> T clickCourse(WaitingPageObject<T> page) {
    headerElem.findElement(By.xpath(".//a[contains(@href, 'course/view.php')]")).click();
    return page.get();
  }
}
