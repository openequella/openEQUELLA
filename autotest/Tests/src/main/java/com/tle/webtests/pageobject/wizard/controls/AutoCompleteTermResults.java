package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AutoCompleteTermResults extends AbstractPage<AutoCompleteTermResults> {
  private WebElement rootUl;
  private AutoCompleteTermControl control;

  public AutoCompleteTermResults(AutoCompleteTermControl control) {
    super(control.getContext());
    this.control = control;
  }

  @Override
  protected WebElement findLoadedElement() {
    List<WebElement> controls =
        driver.findElements(
            By.xpath(
                "//ul[@class='ui-autocomplete ui-menu ui-widget ui-widget-content"
                    + " ui-corner-all']"));

    rootUl = controls.get(0);
    for (WebElement element : controls) {
      if (element.isDisplayed()) {
        rootUl = element;
        break;
      }
    }
    return rootUl;
  }

  public AutoCompleteTermControl selectByIndex(int number) {
    WebElement listElem = waitForElement(rootUl, By.xpath("li[" + number + "]/a/.."));
    waiter.until(ExpectedConditions.elementToBeClickable(listElem));
    scrollToElement(listElem);
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", listElem);
    return control.get();
  }
}
