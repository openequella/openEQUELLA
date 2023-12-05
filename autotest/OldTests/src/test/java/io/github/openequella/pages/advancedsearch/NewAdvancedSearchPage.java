package io.github.openequella.pages.advancedsearch;

import com.tle.webtests.framework.PageContext;
import io.github.openequella.pages.search.NewSearchPage;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

  public void search() {
    WebElement searchButton = driver.findElement(By.xpath("//button[text()='Search']"));
    waiter.until(ExpectedConditions.elementToBeClickable(searchButton));
    searchButton.click();
  }

  public void updateEditBox(String query) {
    WebElement editbox = driver.findElement(By.id("wiz-3-editbox"));
    editbox.sendKeys(query);
  }

  public void selectCheckbox(String value) {
    WebElement targetBox =
        driver.findElement(By.xpath("//input[@type='checkbox' and @value='" + value + "']"));
    targetBox.click();
  }

  public void selectDateRange(String[] range) {
    Optional.ofNullable(range[0])
        .ifPresent(date -> driver.findElement(By.id("wiz-0-calendar-start")).sendKeys(date));

    Optional.ofNullable(range[1])
        .ifPresent(date -> driver.findElement(By.id("wiz-0-calendar-end")).sendKeys(date));
  }

  public void selectListbox(String value) {
    driver.findElement(By.id("List Box-select")).click();
    WebElement targetOption =
        driver.findElement(By.xpath("//li[@role='option' and text()='" + value + "']"));
    targetOption.click();
  }

  public void selectRadio(String value) {
    WebElement targetRadio =
        driver.findElement(By.xpath("//input[@type='radio' and @value='" + value + "']"));
    targetRadio.click();
  }

  public void selectShuffleBox(String value) {
    WebElement shuffleBox = driver.findElement(By.id("wiz-8-shufflebox-options"));
    WebElement targetOption = shuffleBox.findElement(By.xpath(".//span[text()='" + value + "']"));
    targetOption.click();

    driver.findElement(By.xpath("//button[@aria-label='Add selected']")).click();
  }

  public void updateShuffleList(String query) {
    WebElement editbox = driver.findElement(By.id("wiz-9-shufflelist-new-entry"));
    editbox.sendKeys(query);
    editbox.sendKeys(Keys.ENTER);
  }

  public void selectAutoTerm(String term) {
    WebElement autoTerm = driver.findElement(By.id("wiz-10-termselector"));
    WebElement editbox = autoTerm.findElement(By.tagName("input"));
    editbox.sendKeys(term);
    editbox.sendKeys(Keys.DOWN);
    editbox.sendKeys(Keys.ENTER);
  }

  public void selectUser(String username) {
    WebElement userSelector = driver.findElement(By.id("wiz-12-userselector"));

    // Open the dialog and search for the target user.
    userSelector.findElement(By.tagName("button")).click();
    WebElement dialog = driver.findElement(By.xpath("//div[@role='dialog']"));
    WebElement input = dialog.findElement(By.tagName("input"));
    input.sendKeys(username);
    input.sendKeys(Keys.ENTER);

    // Select the target result.
    WebElement targetUser =
        waiter.until(
            ExpectedConditions.elementToBeClickable(
                dialog.findElement(By.xpath(".//span[text()='" + username + "']"))));
    targetUser.click();

    // Confirm the selection.
    WebElement confirmButton = dialog.findElement(By.xpath(".//button[text()='Select']"));
    waiter.until(ExpectedConditions.elementToBeClickable(confirmButton));
    confirmButton.click();
  }

  private WebElement getAdvancedSearchInput() {
    WebElement advSearchSelector = getRefineControl("AdvancedSearchSelector");
    return advSearchSelector.findElement(By.tagName("input"));
  }
}
