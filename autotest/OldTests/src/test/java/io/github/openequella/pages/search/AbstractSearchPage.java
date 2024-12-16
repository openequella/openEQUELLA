package io.github.openequella.pages.search;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import java.util.List;
import java.util.stream.IntStream;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * This class contains common search page elements and operations. It can be extended by different
 * types of search pages, such as NewSearchPage, AdvancedSearchPage, and HierarchicalPage.
 */
public abstract class AbstractSearchPage<T extends PageObject> extends AbstractPage<T> {
  @FindBy(id = "searchBar")
  protected WebElement searchBar;

  @FindBy(xpath = "//button[text()='New search']")
  protected WebElement newSearchButton;

  @FindBy(id = "collapsibleRefinePanelButton")
  protected WebElement collapsibleRefinePanelButton;

  @FindBy(id = "exportSearchResult")
  protected WebElement exportButton;

  private final By searchResultListBy = By.xpath("//ul[@data-testid='search-result-list']");

  public AbstractSearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    // When the Search bar is visible, the page is loaded.
    return searchBar;
  }

  public WebElement getSearchBar() {
    return searchBar;
  }

  /** Get the search list element. */
  public WebElement getSearchList() {
    return driver.findElement(searchResultListBy);
  }

  /** Check if the search result list is present. */
  public Boolean hasSearchResultList() {
    return !driver.findElements(searchResultListBy).isEmpty();
  }

  /**
   * Wait until the initial search is completed. When the page is first rendered, there is no
   * element in the result list, and once the search is completed there will be some elements placed
   * in the list. Thus, it will wait for any element display in the list. If you know the exact
   * number of items will be displayed in the search result, you are expected to use the {@link
   * #waitForSearchCompleted(int)} instead.
   */
  public void waitForInitialSearchResult() {
    waiter.until(
        ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.xpath("//ul[@data-testid='search-result-list']/*")));
  }

  /**
   * Click one Item's title link and open the Item Summary page.
   *
   * @param itemTitle The title of an Item.
   */
  public SummaryPage selectItem(String itemTitle) {
    By title = By.linkText(itemTitle);
    WebElement titleLink = waiter.until(ExpectedConditions.visibilityOfElementLocated(title));
    titleLink.click();
    return new SummaryPage(context).get();
  }

  /**
   * Check if an Item is in the search result.
   *
   * @param itemTitle The title of an Item.
   */
  public boolean hasItem(String itemTitle) {
    return !driver.findElements(By.linkText(itemTitle)).isEmpty();
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
    newSearchButton.click();
  }

  /**
   * Change the search query.
   *
   * @param query A text used as search query.
   */
  public void changeQuery(String query) {
    searchBar.sendKeys(query);
  }

  public void expandRefineControlPanel() {
    collapsibleRefinePanelButton.click();
  }

  /** Execute an export by clicking the export button. */
  public void export() {
    exportButton.click();
  }

  /** Find the Tick icon. */
  public WebElement getExportDoneButton() {
    WebElement tickIcon = driver.findElement(By.id("exportCompleted"));
    getWaiter().until(ExpectedConditions.visibilityOf(tickIcon));
    return tickIcon;
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
    WebElement quickOptionSelector =
        dateRangeSelector.findElement(By.id("date-range-selector-quick-options"));
    quickOptionSelector.click();
    WebElement option = driver.findElement(By.xpath(".//li[@data-value='" + quickOption + "']"));
    option.click();
    // Wait until the popup menu disappears.
    getWaiter()
        .until(ExpectedConditions.invisibilityOfElementLocated(By.className("MuiPopover-root")));
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
    WebElement startTextField = dateRangeSelector.findElement(By.id("date-range-selector-start"));
    startTextField.sendKeys(start);
    WebElement endTextField = dateRangeSelector.findElement(By.id("date-range-selector-end"));
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
    // Normally hidden in the collapsed section of the Refine Panel
    waiter.until(ExpectedConditions.visibilityOf(statusSelector));
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
    // Normally hidden in the collapsed section of the Refine Panel
    waiter.until(ExpectedConditions.visibilityOf(searchAttachmentsSelector));
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
    WebElement selectButton = ownerSelector.findElement(By.xpath(".//button[text()='Select']"));
    selectButton.click();
    WebElement ownerSelectDialog =
        driver.findElement(By.xpath("//div[@id='BaseSearch']/ancestor::div[@role='dialog']"));
    WebElement ownerQueryInput = ownerSelectDialog.findElement(By.xpath(".//input"));
    // Put a query in and press ENTER to search.
    ownerQueryInput.sendKeys(ownerName);
    ownerQueryInput.sendKeys(Keys.ENTER);
    // Wait until the user search is done.
    getWaiter()
        .until(
            driver ->
                ownerSelectDialog.findElements(By.xpath(".//ul[@id='item-search-list']/div")).size()
                    > 0);
    // Click one of found users.
    WebElement owner =
        ownerSelectDialog.findElement(By.xpath(".//span[text()='" + ownerName + "']"));
    owner.click();
    // Press SELECT to close the dialog. Note this is a different SELECT button.
    WebElement confirmButton =
        ownerSelectDialog.findElement(By.xpath(".//button[text()='Select']"));
    confirmButton.click();
    // Wait until the dialog is closed.
    waiter.until(ExpectedConditions.invisibilityOfElementLocated(By.className("MuiDialog-root")));
  }

  /**
   * Returns a By for the aria-label attribute of an attachment link in a search result, given the
   * attachment title.
   *
   * @param text The text to search against to find the name of the attachment within search
   *     results.
   * @return a By for the text.
   */
  private By attachmentLinkByText(String text) {
    return By.xpath(String.format("//*[@aria-label='Attachment link %s']", text));
  }

  /**
   * Give the unique title for a search result, click on the attachments section to display all the
   * attachments.
   *
   * @param itemTitle the unique title of an item to show the attachments for
   */
  public void expandAttachments(String itemTitle) {
    WebElement titleLink = driver.findElement(By.linkText(itemTitle));
    WebElement searchResultItem = titleLink.findElement(By.xpath("ancestor::li[@data-item-id]"));
    String itemUuid = searchResultItem.getAttribute("data-item-id");
    String itemVersion = searchResultItem.getAttribute("data-item-version");

    String id = "attachments-list-" + itemUuid + ":" + itemVersion;
    searchResultItem.findElement(By.id(id)).click();
  }

  /**
   * Waits until the search results contain an Attachment with a name that matches the given text.
   *
   * @param attachmentText the attachment name to be present in the search.
   */
  public void verifyAttachmentDisplayed(String attachmentText) {
    waiter.until(ExpectedConditions.presenceOfElementLocated(attachmentLinkByText(attachmentText)));
  }

  /**
   * Waits until the search results contain no attachments with a name that matches the given text.
   * Uses numberOfElementsToBe(path, 0), as there is no non-presence condition in
   * ExpectedConditions.
   *
   * @param attachmentText the attachment name not to be present in the search.
   */
  public void verifyAttachmentNotDisplayed(String attachmentText) {
    waiter.until(ExpectedConditions.numberOfElementsToBe(attachmentLinkByText(attachmentText), 0));
  }

  /** Verify that the export button is hidden. */
  public void verifyExportButtonNotDisplayed() {
    waiter.until(ExpectedConditions.numberOfElementsToBe(By.id("exportSearchResult"), 0));
  }

  /**
   * Find the snackbar and verify its message.
   *
   * @param message The message expected to be displayed in the snackbar.
   */
  public void verifySnackbarMessage(String message) {
    WebElement snackbar =
        driver.findElement(By.xpath("//span[@id='client-snackbar' and text()='" + message + "']"));
    getWaiter().until(ExpectedConditions.visibilityOf(snackbar));
  }

  /**
   * Get the names of a list of Items in the specified the ranage.
   *
   * @param start The start index of the range.
   * @param end The end index of the range.
   */
  public String[] getItemNamesByRange(int start, int end) {
    return IntStream.range(start, end).mapToObj(this::getItemNameByIndex).toArray(String[]::new);
  }

  public String[] getTopNItemNames(int n) {
    return getItemNamesByRange(0, n);
  }

  /**
   * Get the name of an Item by its index in the search result list.
   *
   * @param index The position in which the target Item sits in the search result list.
   */
  public String getItemNameByIndex(int index) {
    List<WebElement> items =
        driver.findElements(By.xpath("//li[@aria-label='Search result list item']"));
    try {
      WebElement item = items.get(index);
      return item.findElement(By.tagName("a")).getText();
    } catch (IndexOutOfBoundsException e) {
      return "Unable to locate the target item";
    }
  }

  /**
   * Save a search to Favourite.
   *
   * @param name Name of the favourite search
   */
  public void addToFavouriteSearch(String name) {
    WebElement favouriteButton =
        driver.findElement(By.xpath("//button[@aria-label='Add search to favourites']"));
    favouriteButton.click();

    WebElement favouriteDialog = driver.findElement(By.xpath("//div[@role='dialog']"));
    WebElement nameInput = favouriteDialog.findElement(By.tagName("input"));
    nameInput.sendKeys(name);

    WebElement confirmButton = favouriteDialog.findElement(By.id("confirm-dialog-confirm-button"));
    waiter.until(ExpectedConditions.elementToBeClickable(confirmButton));
    confirmButton.click();
  }

  /**
   * Open modify key resource dialog and select an item to add to key resource.
   *
   * @param itemName Name of the item.
   * @param hierarchyName Name of the hierarchy to add the item to.
   */
  public void addToKeyResource(String itemName, String hierarchyName) {
    By addToKeyResourceButtonXpath =
        By.xpath(
            "//div[h6[a[text()='"
                + itemName
                + "']]]//button[@aria-label='Add as key resource to a hierarchy']");

    WebElement addToKeyResourceButton = driver.findElement(addToKeyResourceButtonXpath);
    addToKeyResourceButton.click();

    // wait for the dialog to loading
    By hierarchyXpath =
        By.xpath(
            "//div[@aria-labelledby='modify-key-resource-dialog-title']//div[div[span[contains(text(), '"
                + hierarchyName
                + "')]]]");
    By plusButtonXpath = By.xpath(".//button[@aria-label='Add to hierarchy']");
    WebElement plusButton =
        waiter
            .until(ExpectedConditions.visibilityOfElementLocated(hierarchyXpath))
            .findElement(plusButtonXpath);
    plusButton.click();
  }

  /** Get the number of search results. */
  public int getResultCount() {
    By itemX = By.xpath("//li[@aria-label='Search result list item']");
    return driver.findElements(itemX).size();
  }

  /**
   * Get one Refine Search control by ID.
   *
   * @param id The ID of a Refine Search control, excluding its prefix.
   */
  protected WebElement getRefineControl(String id) {
    return driver.findElement(By.id("RefineSearchPanel-" + id));
  }

  /**
   * Select and click a button from a button group.
   *
   * @param buttonGroup The button group which contains buttons.
   * @param buttonText The text of a button.
   */
  private void selectFromButtonGroup(WebElement buttonGroup, String buttonText) {
    WebElement button = buttonGroup.findElement(By.xpath(".//button[text()='" + buttonText + "']"));
    button.click();
  }
}
