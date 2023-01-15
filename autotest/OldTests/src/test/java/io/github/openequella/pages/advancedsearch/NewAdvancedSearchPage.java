package io.github.openequella.pages.advancedsearch;

import com.tle.webtests.framework.PageContext;
import io.github.openequella.pages.search.NewSearchPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class NewAdvancedSearchPage extends NewSearchPage {

  public NewAdvancedSearchPage(PageContext context) {
    super(context);
  }

  public void selectAdvancedSearch(String name) {
    WebElement input = getAdvancedSearchInput();
    input.sendKeys(name);
    WebElement option = driver.findElement(By.xpath("//li[contains(text(), '" + name + "')]"));
    option.click();
  }

  public String getSelection() {
    WebElement input = getAdvancedSearchInput();
    return input.getAttribute("value");
  }

  public void clearSelection() {
    WebElement input = getAdvancedSearchInput();
    // Click the input to display the Clear icon button.
    input.click();

    WebElement clearButton =
        input.findElement(By.xpath("./following-sibling::div/button[@aria-label='Clear']"));
    clearButton.click();
  }

  public WebElement getAdvancedSearchFilterIcon() {
    List<WebElement> buttons =
        driver.findElements(By.xpath(".//button[@aria-label='Show advanced search filters']"));
    if (buttons.size() > 0) {
      return buttons.get(0);
    }

    return null;
  }

  public WebElement getAdvancedSearchPanel() {
    List<WebElement> divs = driver.findElements(By.id("advanced-search-panel"));
    if (divs.size() > 0) {
      return divs.get(0);
    }

    return null;
  }

  public void closeAdvancedSearchPanel() {
    WebElement panel = getAdvancedSearchPanel();
    if (panel != null) {
      WebElement closeButton = panel.findElement(By.xpath(".//button[@aria-label='Close']"));
      closeButton.click();
    } else {
      throw new NoSuchElementException("Failed to find Advanced search panel");
    }
  }

  public void openAdvancedSearchPanel() {
    WebElement toggle = getAdvancedSearchFilterIcon();
    if (toggle != null) {
      toggle.click();
    } else {
      throw new NoSuchElementException("Failed to find Advanced search filter icon");
    }
  }

  private WebElement getAdvancedSearchInput() {
    WebElement advSearchSelector = getRefineControl("AdvancedSearchSelector");
    return advSearchSelector.findElement(By.tagName("input"));
  }
}
