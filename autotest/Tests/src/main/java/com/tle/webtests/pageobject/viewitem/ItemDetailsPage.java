package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ItemDetailsPage extends AbstractPage<ItemDetailsPage> {

  @FindBy(xpath = "//li[starts-with(normalize-space(text()),'Views:')]")
  private WebElement viewsElem;

  public ItemDetailsPage(PageContext context) {
    super(context, By.id("adjacentuls"));
  }

  public int getViewCount() {
    final String viewsText = viewsElem.getText();
    return Integer.parseInt(viewsText.substring(7));
  }

  public String getOwner() {
    By ownerLocator = By.xpath("//li[starts-with(text(),'Owner:')]/span");
    String owner = driver.findElement(ownerLocator).getText();
    return owner;
  }
}
