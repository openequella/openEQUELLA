package com.tle.webtests.pageobject;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class CustomLinksPage extends AbstractPage<CustomLinksPage> {
  @FindBy(id = "cls_addButton")
  private WebElement addButton;

  @FindBy(id = "cls_us")
  private WebElement tableElem;

  public CustomLinksPage(PageContext context) {
    super(context, By.id("linkListDiv"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/customlinks.do");
  }

  public CustomLinksEditPage newLink() {
    addButton.click();
    return new CustomLinksEditPage(this).get();
  }

  public boolean linkExistsOnMenu(String name, String url, boolean icon) {
    if (icon) {
      return isPresent(
          By.xpath(
              "//div[@class='menu-full']//a[normalize-space(text())="
                  + AbstractPage.quoteXPath(name)
                  + " and normalize-space(@href)="
                  + AbstractPage.quoteXPath(url)
                  + " and contains(@style, 'background-image')]"));
    } else {
      return isPresent(
          By.xpath(
              "//div[@class='menu-full']//a[normalize-space(text())="
                  + AbstractPage.quoteXPath(name)
                  + " and normalize-space(@href)="
                  + AbstractPage.quoteXPath(url)
                  + "]"));
    }
  }

  public boolean linkExists(String name, String url) {
    return isPresent(tableElem, getRowForLink(name, url));
  }

  public CustomLinksPage deleteLink(int linkIndex) {
    return deleteRow(tableElem.findElement(By.xpath("li[" + linkIndex + "]")));
  }

  private CustomLinksPage deleteRow(WebElement row) {
    WebElement deleteButton = row.findElement(EBy.buttonText("Delete"));
    WaitingPageObject<CustomLinksPage> waiter = removalWaiter(row);
    deleteButton.click();
    acceptConfirmation();
    return waiter.get();
  }

  public CustomLinksPage deleteLink(String name, String url) {
    return deleteRow(findLink(name, url));
  }

  public CustomLinksEditPage editLink(String name, String url) {
    tableElem.findElement(getRowForLink(name, url)).findElement(EBy.buttonText("Edit")).click();
    return new CustomLinksEditPage(this).get();
  }

  private int linkCount() {
    return tableElem.findElements(By.xpath("li")).size();
  }

  private By getRowForLink(String name, String url) {
    return By.xpath(
        "li[@class='ui-state-default' and div[text()="
            + AbstractPage.quoteXPath(name + " - " + url)
            + "]]");
  }

  public CustomLinksPage moveLink(String name, String url, String name2, String url2) {

    WebElement linkFrom = findLink(name, url);
    WebElement linkTo = findLink(name2, url2);

    Actions actionsProvider = new Actions(driver);
    actionsProvider.dragAndDrop(linkFrom, linkTo).perform();

    return get();
  }

  public CustomLinksPage moveLink(String fullOne, String fullTwo) {
    WebElement linkFrom = findLink(fullOne);
    WebElement linkTo = findLink(fullTwo);

    Actions actionsProvider = new Actions(driver);
    actionsProvider.dragAndDrop(linkFrom, linkTo).perform();
    return get();
  }

  private WebElement findLink(String name, String url) {
    return tableElem.findElement(getRowForLink(name, url));
  }

  private WebElement findLink(String full) {
    return tableElem.findElement(By.xpath("li/div[text()=" + AbstractPage.quoteXPath(full) + "]"));
  }

  public void deleteAllLinks() {
    while (linkCount() > 0) {
      deleteLink(0);
    }
  }

  public WaitingPageObject<CustomLinksPage> getNewExpectation(String name, String url) {
    return visibilityWaiter(tableElem, getRowForLink(name, url));
  }
}
