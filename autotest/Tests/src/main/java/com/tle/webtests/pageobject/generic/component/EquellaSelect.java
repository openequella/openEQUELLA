package com.tle.webtests.pageobject.generic.component;

import com.tle.common.Utils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class EquellaSelect extends AbstractPage<EquellaSelect> {
  private WebElement dropDiv;
  private final WebElement origSelect;
  private WebElement textDiv;

  public EquellaSelect(PageContext context, WebElement select) {
    super(context, By.xpath("following-sibling::div[contains(@class, 'newListSelected')]"));
    origSelect = select;
    setRelativeTo(origSelect);
    get();
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
    dropDiv = origSelect.findElement(loadedBy);
    textDiv = dropDiv.findElement(By.xpath("div[@class='selectedTxt']"));
  }

  public void selectByVisibleText(String name) {
    textDiv.click();
    WebElement entry =
        dropDiv.findElement(
            By.xpath(
                "id("
                    + quoteXPath(origSelect.getAttribute("id"))
                    + ")/following-sibling::div[1]//a[text()="
                    + quoteXPath(name)
                    + "]"));
    scrollToElement(entry);
    entry.click();
  }

  public void selectByValue(String value) {
    WebElement option =
        origSelect.findElement(By.xpath(".//option[@value=" + quoteXPath(value) + "]"));
    String text =
        (String)
            ((JavascriptExecutor) driver).executeScript("return arguments[0].innerHTML;", option);
    text = Utils.unent(text);
    selectByVisibleText(text);
  }

  /**
   * Weird behavior in the xpath: if the int parameter is 0, the resultant xpath ".//a[1]" acts as
   * expected, but for any other value, the integer value must be quoted, to wit: ".//a['7']" or
   * ".//a[position()='7']". An alternative to that lunacy is to retrieve the WebElements as a list,
   * and then (having established list size lies within parameter size) use the list.get(n) accessor
   * to click the targeted element. Selenium bug?
   *
   * @param index
   */
  public void selectByIndex(int index) {
    textDiv.click();
    try {
      dropDiv.findElement(By.xpath(".//a[" + (index + 1) + "]")).click();
    } catch (NoSuchElementException noseeum) {
      List<WebElement> allLinks = dropDiv.findElements(By.xpath(".//a"));
      if (allLinks.size() > index) {
        allLinks.get(index).click();
      } else throw noseeum;
    }
  }

  public List<WebElement> getOptionElements() {
    return origSelect.findElements(By.xpath(".//option"));
  }

  public String getSelectedValue() {
    List<WebElement> options = getOptionElements();
    String value = null;
    for (WebElement opt : options) {
      if (opt.isSelected()) {
        value = opt.getAttribute("value");
        break;
      }
    }
    return value;
  }

  public List<WebElement> getSelectableHyperinks() {
    // first click opens the list
    textDiv.click();
    String theXpath = "id(" + quoteXPath(origSelect.getAttribute("id")) + ")/..//a";
    List<WebElement> foundElements = dropDiv.findElements(By.xpath(theXpath));
    // we may need to click again to close the list, but can't do it yet
    return foundElements;
  }

  public String getSelectedText() {
    return textDiv.getText();
  }

  public void clickOn() {
    textDiv.click();
  }

  public boolean isDisabled() {
    return !origSelect.isEnabled();
  }
}
