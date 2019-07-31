package com.tle.webtests.pageobject.generic.component;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ShuffleBox extends AbstractPage<ShuffleBox> implements ListRenderer {
  private final String id;
  private WebElement boxDiv;

  private Select leftDropDown;
  private Select rightDropDown;

  public ShuffleBox(PageContext context, String id) {
    super(context, By.id(id));
    this.id = id;
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
    rightDropDown = new Select(driver.findElement(By.id(id)));
    leftDropDown = new Select(driver.findElement(By.id(id + "_left")));
    boxDiv =
        driver.findElement(
            By.xpath("//div[@class='shuffle-box' and .//select[@id=" + quoteXPath(id) + "]]"));
  }

  public void moveRightByValue(String... values) {
    deselectAll(leftDropDown);
    for (String value : values) {
      leftDropDown.selectByValue(value);
    }
    clickButton(">");
  }

  private void clickButton(String button) {
    // see http://code.google.com/p/selenium/issues/detail?id=4395
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("$('#" + id + "').change()");
    boxDiv.findElement(EBy.buttonText(button)).click();
  }

  private void deselectAll(Select select) {
    // select.deselectAll();
    for (WebElement option : select.getAllSelectedOptions()) {
      if (!Check.isEmpty(option.getAttribute("checked"))) {
        option.click();
      }
    }
  }

  public void moveLeftByValue(String... values) {
    deselectAll(rightDropDown);
    for (String value : values) {
      rightDropDown.selectByValue(value);
    }
    clickButton("<");
  }

  public void moveAllRight() {
    clickButton(">>");
  }

  public void moveRightByText(String... names) {
    deselectAll(leftDropDown);
    for (String name : names) {
      leftDropDown.selectByVisibleText(name);
    }
    clickButton(">");
  }

  public void moveAllLeft() {
    clickButton("<<");
  }

  @Override
  public void setSelectionByText(String... names) {
    moveAllLeft();
    moveRightByText(names);
  }

  @Override
  public void setSelectionByValue(String... values) {
    moveAllLeft();
    moveRightByValue(values);
  }

  @Override
  public void selectAll() {
    moveAllRight();
  }

  @Override
  public List<String> getSelectedTexts() {
    return Lists.transform(
        rightDropDown.getOptions(),
        new Function<WebElement, String>() {
          @Override
          public String apply(WebElement option) {
            return option.getText();
          }
        });
  }

  @Override
  public List<String> getSelectedValues() {
    return Lists.transform(
        rightDropDown.getOptions(),
        new Function<WebElement, String>() {
          @Override
          public String apply(WebElement option) {
            return option.getAttribute("value");
          }
        });
  }
}
