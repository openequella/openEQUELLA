package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NewSearchPage extends AbstractPage<NewSearchPage> {

  public NewSearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "page/search");
  }

  /** Find the Search query bar. */
  public WebElement getSearchBar() {
    return driver.findElement(By.id("searchBar"));
  }

  @Override
  protected WebElement findLoadedElement() {
    // When the Search bar is visible, the page is loaded.
    return getSearchBar();
  }

  /**
   * Wait until the correct number of items are displayed.
   *
   * @param itemCount The expected number of items
   */
  public void waitForSearchCompleted(int itemCount) {
    waiter.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//span[text()='Search results (" + itemCount + ")']")));
  }

  /** Perform a new search. */
  public void newSearch() {
    WebElement newSearchButton = driver.findElement(By.xpath("//button[span=('New search')]"));
    newSearchButton.click();
  }

  /**
   * Change the search query.
   *
   * @param query A text used as search query.
   */
  public void changeQuery(String query) {
    WebElement searchBar = getSearchBar();
    searchBar.sendKeys(query);
  }

  /**
   * Get the link of an Item's title which links to the Item's summary page.
   *
   * @param title The text of an Item's title
   */
  public WebElement getItemTitleLink(String title) {
    return driver.findElement(By.linkText(title));
  }

  public void expandRefineControlPanel() {
    WebElement showMore = driver.findElement(By.id("collapsibleRefinePanelButton"));
    showMore.click();
  }
  /**
   * Select Collections by typing keywords in the Selector's TextField.
   *
   * @param collectionNames A list of Collection names
   */
  public void selectCollection(String... collectionNames) {
    WebElement collectionSelector = getRefineControl("CollectionSelector");
    WebElement collectionSelectTextField = collectionSelector.findElement(By.xpath(".//input"));
    for (String name : collectionNames) {
      collectionSelectTextField.sendKeys(name);
      collectionSelectTextField.sendKeys(Keys.DOWN);
      collectionSelectTextField.sendKeys(Keys.ENTER);
    }
    // Press TAB to remove the focus so the Collection dropdown list will disappear.
    collectionSelectTextField.sendKeys(Keys.TAB);
  }

  /**
   * Select one of the date range quick options.
   *
   * @param quickOption The text of a quick option
   */
  public void selectDateRangeQuickOption(String quickOption) {
    WebElement dateRangeSelector = getRefineControl("DateRangeSelector");
    dateRangeSelector.click();
    WebElement option = driver.findElement(By.xpath(".//li[@data-value='" + quickOption + "']"));
    option.click();
  }

  /**
   * Select a custom date range.
   *
   * @param start The start of a date range
   * @param end The end of a date range
   */
  public void selectCustomDateRange(String start, String end) {
    WebElement dateRangeSelector = getRefineControl("DateRangeSelector");
    WebElement quickOptionSwitch =
        dateRangeSelector.findElement(By.id("modified_date_selector_mode_switch"));
    quickOptionSwitch.click();
    WebElement startTextField = dateRangeSelector.findElements(By.tagName("input")).get(0);
    startTextField.sendKeys(start);
    WebElement endTextField = dateRangeSelector.findElements(By.tagName("input")).get(1);
    endTextField.sendKeys(end);
  }

  /**
   * Select either the Item status of 'LIVE' or 'ALL'.
   *
   * @param allStatus True to select 'ALL'
   */
  public void selectStatus(boolean allStatus) {
    String buttonText = allStatus ? "All" : "Live";
    WebElement statusSelector = getRefineControl("StatusSelector");
    selectFromButtonGroup(statusSelector, buttonText);
  }

  /**
   * Select whether to search attachments or not.
   *
   * @param search True to search attachments.
   */
  public void selectSearchAttachments(boolean search) {
    String buttonText = search ? "Yes" : "No";
    WebElement searchAttachmentsSelector = getRefineControl("SearchAttachmentsSelector");
    selectFromButtonGroup(searchAttachmentsSelector, buttonText);
  }

  /**
   * Select an Owner.
   *
   * @param ownerName The name of an Owner.
   */
  public void selectOwner(String ownerName) {
    WebElement ownerSelector = getRefineControl("OwnerSelector");
    // Click the SELECT button to open the dialog.
    WebElement selectButton = ownerSelector.findElement(By.xpath(".//button[span=('Select')]"));
    selectButton.click();
    WebElement ownerSelectDialog =
        driver.findElement(By.xpath("//div[@id='UserSearch']/ancestor::div[@role='dialog']"));
    WebElement ownerQueryInput = ownerSelectDialog.findElement(By.xpath(".//input"));
    // Put a query in and press ENTER to search.
    ownerQueryInput.sendKeys(ownerName);
    ownerQueryInput.sendKeys(Keys.ENTER);
    // Wait until the user search is done.
    getWaiter()
        .until(
            driver ->
                ownerSelectDialog
                        .findElements(By.xpath(".//ul[@id='UserSearch-UserList']/div"))
                        .size()
                    > 0);
    // Click one of found users.
    WebElement owner =
        ownerSelectDialog.findElement(By.xpath(".//span[text()='" + ownerName + "']"));
    owner.click();
    // Press SELECT to close the dialog. Note this is a different SELECT button.
    WebElement confirmButton =
        ownerSelectDialog.findElement(By.xpath(".//button[span=('Select')]"));
    confirmButton.click();
  }

  /**
   * Get one Refine Search control by ID.
   *
   * @param id The ID of a Refine Search control, excluding its prefix.
   */
  private WebElement getRefineControl(String id) {
    return driver.findElement(By.id("RefineSearchPanel-" + id));
  }

  /**
   * Select and click a button from a button group.
   *
   * @param buttonGroup The button group which contains buttons.
   * @param buttonText The text of a button.
   */
  private void selectFromButtonGroup(WebElement buttonGroup, String buttonText) {
    WebElement button = buttonGroup.findElement(By.xpath(".//button[span=('" + buttonText + "')]"));
    button.click();
  }
}
