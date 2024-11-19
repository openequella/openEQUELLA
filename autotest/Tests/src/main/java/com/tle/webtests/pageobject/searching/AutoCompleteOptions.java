package com.tle.webtests.pageobject.searching;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AutoCompleteOptions extends AbstractPage<AutoCompleteOptions> {
  private static final String LIST_XPATH = "//ul[contains(@class, 'ui-autocomplete')]";
  private static final By LIST_BY = By.xpath(LIST_XPATH);

  @FindBy(xpath = LIST_XPATH)
  private WebElement listElement;

  @FindBy(xpath = "//input[@class='prompt']")
  protected WebElement promptField;

  private final AbstractQuerySection<?> qs;
  private final WebDriverWait acWaiter;

  public AutoCompleteOptions(AbstractQuerySection<?> qs) {
    super(qs.getContext(), LIST_BY);
    setMustBeVisible(true);
    this.qs = qs;
    acWaiter = new WebDriverWait(driver, context.getTestConfig().getStandardTimeout(), 600);
  }

  public String getPromptText() {
    return promptField.getAttribute("value");
  }

  public WebElement getPromptField() {
    return promptField;
  }

  public List<String> getAutoCompleteOptions() {
    listWait();
    List<WebElement> opts = listElement.findElements(By.xpath("li/a"));
    List<String> options = Lists.newArrayList();
    for (WebElement ele : opts) {
      options.add(ele.getText());
    }
    return options;
  }

  public AutoCompleteOptions partialQuery(String partial, String expected) {
    waiter.until(
        ExpectedConditions2.elementAttributeToContain(
            qs.getLoadedElement(), "class", "ui-autocomplete"));
    qs.setQuery(partial, false);
    listWait(expected);
    return get();
  }

  public void complete(Keys keys) {
    String realTextEscaped =
        (String)
            ((JavascriptExecutor) driver)
                .executeScript("return $(arguments[0]).data(\"real\");", promptField);
    qs.typeKeys(keys);
    waiter.until(ExpectedConditions.attributeToBe(qs.queryField, "value", realTextEscaped));
  }

  public void waitUntilDisappeared() {
    waiter.until(ExpectedConditions.invisibilityOfElementLocated(LIST_BY));
  }

  public void close() {
    qs.typeKeys(Keys.ESCAPE);
    waitUntilDisappeared();
  }

  private void listWait() {
    listWait(null);
  }

  private void listWait(String expected) {
    final By by;
    if (Check.isEmpty(expected)) {
      by = By.xpath("li/a");
    } else {
      by =
          By.xpath(
              "li/a[normalize-space(concat(descendant::text(), text()))="
                  + quoteXPath(expected.trim())
                  + "]");
    }
    acWaiter.until(
        new ExpectedCondition<Boolean>() {
          private String lastQuery;

          @Override
          public Boolean apply(WebDriver driver) {
            try {
              if (listElement.findElement(by).isDisplayed()) {
                return true;
              }
            } catch (NoSuchElementException nse) {

            }

            if (Check.isEmpty(lastQuery)) {
              lastQuery = qs.getQueryText();
              qs.getLoadedElement().sendKeys(Keys.ESCAPE);
              qs.getLoadedElement().clear();
              ((JavascriptExecutor) driver)
                  .executeScript("$(arguments[0]).keydown();", qs.getLoadedElement());
            } else {
              qs.setQuery(lastQuery);
              lastQuery = null;
            }

            return false;
          }
        });
  }
}
