package com.tle.webtests.pageobject.myresources;

import static com.tle.webtests.framework.Assert.assertTrue;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.AbstractQuerySection;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.FilterByKeywordPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.viewitem.ModerationQueueShowCommentDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MyResourcesPage
    extends AbstractQueryableSearchPage<MyResourcesPage, ItemListPage, ItemSearchResult> {
  private static final TimeZone USERS_TIMEZONE = TimeZone.getTimeZone("America/Chicago");

  public static final String ADD_SEARCH_TO_FAV_CONTROL_ID = "mrs_fd_opener";

  // Moderation Queue Table
  @FindBy(id = "mqil_mt")
  private WebElement modqueuetable;

  @FindBy(id = "searchresults-select")
  private WebElement mainElem;

  private String urlTypeArg;

  public MyResourcesPage(PageContext context, String urlTypeArg) {
    super(context);
    this.urlTypeArg = urlTypeArg;
  }

  // Applicable to New UI only to find the icon button for creating a Scrapbook.
  private WebElement getButtonForCreatingScrapbook(String scrapbookType) {
    WebElement addIcon = driver.findElement(By.xpath("//button[@aria-label='Add to Scrapbook']"));
    addIcon.click();

    return driver.findElement(By.xpath("//span[text()='" + scrapbookType + "']"));
  }

  // Applicable to New UI only to find the icon button for uploading a file.
  private WebElement getUploadFileButton() {
    if (isNewUI()) {
      return getButtonForCreatingScrapbook("Upload files");
    } else {
      return driver.findElement(By.id("cmca_b"));
    }
  }

  // Applicable to New UI only to find the icon button for creating a webpage.
  private WebElement getAuthorWebPageButton() {
    if (isNewUI()) {
      return getButtonForCreatingScrapbook("Author new web pages");
    } else {
      return driver.findElement(By.id("cmca2_b"));
    }
  }

  // Applicable to New UI only to find the action button for editing or deleting a Scrapbook.
  private WebElement getScrapbookActionButton(String scrapbookName, String buttonTitle) {
    return getScrapbookByName(scrapbookName)
        .findElement(By.xpath("//button[@aria-label='" + buttonTitle + "']"));
  }

  // Applicable to New UI only to build a XPATH for the provided Scrapbook.
  private By scrapbookXpath(String name) {
    return By.xpath(
        "//li/div[./h6/a[normalize-space(string())=" + quoteXPath(normaliseSpace(name)) + "]]");
  }

  // Applicable to New UI only to build a XPATH for the provided webpage.
  private By individualPageXpath(String pageTitle) {
    return By.xpath(".//div/span[text()=" + quoteXPath(pageTitle) + "]");
  }

  /** Applicable to New UI only to find a SearchResult by Scrapbook name. */
  private WebElement getScrapbookByName(String scrapbookName) {
    return waiter.until(
        ExpectedConditions.visibilityOfElementLocated(scrapbookXpath(scrapbookName)));
  }

  /** Applicable to New UI only to find All SearchResult that match the provided Scrapbook name. */
  private List<WebElement> getAllScrapbooksByName(String scrapbookName) {
    return waiter.until(
        ExpectedConditions.visibilityOfAllElementsLocatedBy(scrapbookXpath(scrapbookName)));
  }

  @Override
  public ItemListPage search(PrefixedName query) {
    openFilters();
    querySection = createQuerySection();
    return super.search(query);
  }

  @Override
  public ItemListPage search(String query) {
    openFilters();
    querySection = createQuerySection();
    return super.search(query);
  }

  @Override
  public ItemListPage exactQuery(String query, int minExpected) {
    if (isNewUI()) {
      WebElement searchBar = driver.findElement(By.id("searchBar"));
      searchBar.sendKeys(query);
      return null;
    }
    openFilters();
    return super.exactQuery(query, minExpected);
  }

  @Override
  protected WebElement findLoadedElement() {
    if (isNewUI()) {
      return driver.findElement(By.xpath("//h5[text() = " + quoteXPath("My resources") + "]"));
    }
    return driver.findElement(By.xpath("//h2[text() = " + quoteXPath("My resources") + "]"));
  }

  @Override
  protected AbstractQuerySection<?> createQuerySection() {
    return new FilterByKeywordPage(context);
  }

  @Override
  protected void loadUrl() {
    String myResourcesURL = isNewUI() ? "page/myresources?" : "access/myresources.do?";
    if (getUrlTypeArg() != null && getUrlTypeArg().length() > 0) {
      myResourcesURL += ("type=" + getUrlTypeArg());
    }
    driver.get(context.getBaseUrl() + myResourcesURL);
  }

  protected String getUrlTypeArg() {
    return urlTypeArg;
  }

  public MyResourcesPage uploadFile(String path, String description, String tags) {
    getUploadFileButton().click();
    new MyResourcesUploadFilesPage(this).get().uploadFile(path, description, tags);
    return get();
  }

  public MyResourcesUploadFilesPage getUploadPage() {
    getUploadFileButton().click();
    return new MyResourcesUploadFilesPage(this).get();
  }

  public MyResourcesPage uploadFile(File file, String description, String tags) {
    return uploadFile(file.getAbsolutePath(), description, tags);
  }

  public MyResourcesPage uploadFile(URL file, String description, String tags) {
    return uploadFile(getPathFromUrl(file), description, tags);
  }

  public MyResourcesPage authorWebPage(String description, String pageTitle, String pageBody) {
    getAuthorWebPageButton().click();
    MyResourcesAuthorWebPage page = new MyResourcesAuthorWebPage(this).get();
    page.setDescription(description);
    page.addPage(pageTitle, pageBody);
    return page.save();
  }

  public MyResourcesPage authorWebPages(
      String description, String[] pageTitles, String[] pageBodies) {
    getAuthorWebPageButton().click();
    MyResourcesAuthorWebPage authorPages = new MyResourcesAuthorWebPage(this).get();
    authorPages.setDescription(description);
    int numTitles = pageTitles.length;
    int numBodies = pageBodies.length;
    // allow for a discrepancy in the array sizes
    for (int i = 0; i < numTitles || i < numBodies; ++i) {
      int aTitleIndex = i;
      if (numTitles <= i) {
        aTitleIndex = numTitles - 1;
      }
      int aBodyIndex = i;
      if (numBodies <= i) {
        aBodyIndex = numBodies - 1;
      }
      authorPages.addPage(pageTitles[aTitleIndex], pageBodies[aBodyIndex]);
    }
    ((JavascriptExecutor) getContext().getDriver())
        .executeScript("window.scrollTo(0, -document.body.scrollHeight)");
    return authorPages.save();
  }

  public MyResourcesAuthorWebPage authorWebPage() {
    getAuthorWebPageButton().click();
    return new MyResourcesAuthorWebPage(this).get();
  }

  /**
   * Bring up a tab identified by its displayed link.
   *
   * @param linkText
   * @return
   */
  public MyResourcesPage clickSelectedTab(String linkText) {
    WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
    WebElement tabLinksDiv =
        context.getDriver().findElement(By.xpath("//div[@id='searchresults-select']"));
    List<WebElement> otherTabLinks = tabLinksDiv.findElements(By.xpath(".//a"));

    for (WebElement otherTabLink : otherTabLinks) {
      String tabLinkText = otherTabLink.getText();
      if (tabLinkText.toLowerCase().contains(linkText.toLowerCase())) {
        otherTabLink.click();
        break;
      }
    }
    return waitForResultsReload(waiter);
  }

  public List<String> gatherAllScrapbookTitles(ItemListPage itemsPage, boolean queryForEditDelete) {
    List<String> presumedScrapbookTitles = new ArrayList<String>();
    WebElement pagesDiv = null;
    int pageCounter = 0, pages = 1; // We'll assume by default
    // How many pages in this result set?
    try {
      pagesDiv = context.getDriver().findElement(By.id("page"));
      List<WebElement> linksToOtherPages = pagesDiv.findElements(By.xpath(".//li/a"));
      pages = linksToOtherPages.size();
    } catch (NoSuchElementException noseeum) {
    }

    int numUnfiltered = itemsPage.getResults().size();

    do {
      // Ordinal sequence in getByForResult, ie 1-based not 0-based
      for (int sequencer = 1; sequencer <= numUnfiltered; ++sequencer) {
        ItemSearchResult itemAdded = itemsPage.getResult(sequencer);
        if (!queryForEditDelete || itemAdded.isEditDeletableItem()) {
          presumedScrapbookTitles.add(itemAdded.getTitle());
        }
      }

      // Turn the pages till we get the end of the set
      if (pageCounter < pages - 1) {
        try {
          WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
          pagesDiv = context.getDriver().findElement(By.id("page"));
          pagesDiv.findElement(By.xpath(".//li/a[text()='next']")).click();
          itemsPage = waitForResultsReload(waiter).results();
          numUnfiltered = itemsPage.getResults().size();
          pagesDiv = context.getDriver().findElement(By.id("page"));
        } catch (NoSuchElementException nosuchthingasnext) {
        }
      }
    } while (++pageCounter < pages);

    return presumedScrapbookTitles;
  }

  public MyResourcesPage enterStringIntoFilter(String targetFilterName) {
    // Open filter tab
    openFilters();

    // confirm the presence of the resource filter
    WebElement resourceDiv = context.getDriver().findElement(By.xpath("//ul[@id='mt']"));
    List<WebElement> filterCheckboxes = resourceDiv.findElements(By.xpath(".//li/input"));
    // Allow for the possibility that there are others than the two we
    // require,
    // and don't make this test dependent on exact order
    for (Iterator<WebElement> iter = filterCheckboxes.iterator(); iter.hasNext(); ) {
      WebElement filterCheckbox = iter.next();
      String filterName = filterCheckbox.findElement(By.xpath("../label")).getText();
      if (filterName.toLowerCase().contains(targetFilterName)) {
        WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
        filterCheckbox.click();
        waitForResultsReload(waiter);
      }
    }
    return this;
  }

  /**
   * Most tabs bring up a standard set of sort options, the exception being the status-related
   * variations for the Moderation queue.
   */
  public void examineStandardSortOptions() {
    openSort();

    WebElement sortElement = getSortList();
    EquellaSelect sortOptions = new EquellaSelect(context, sortElement);
    List<WebElement> selectableOptions = sortOptions.getSelectableHyperinks();
    // We expect these three. Allow the test to succeed if there are more.
    // Noting that "Rating' (found in the sort-options for main search)
    // should not be here
    boolean hasRelevance = false, hasTitle = false, hasDate = false;
    for (WebElement anOption : selectableOptions) {
      String anOptionsText = anOption.getText();
      if (anOptionsText.toLowerCase().contains("Date".toLowerCase())) {
        hasDate = true;
      } else if (anOptionsText.toLowerCase().contains("Title".toLowerCase())) {
        hasTitle = true;
      } else if (anOptionsText.toLowerCase().contains("Relevance".toLowerCase())) {
        hasRelevance = true;
      }
    }
    sortOptions.clickOn();
    assertTrue(
        hasRelevance && hasDate && hasTitle,
        "Expected date("
            + (hasDate ? "OK" : "Not OK")
            + "), relevance("
            + (hasRelevance ? "OK" : "Not OK")
            + ") and title ("
            + (hasTitle ? "OK" : "Not OK")
            + ") sort options");
  }

  public MyResourcesPage clearFilters() {
    resetFilters();
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
    dfm.setTimeZone(USERS_TIMEZONE);
    try {
      dfm.parse("2012-01-01");
    } catch (ParseException e) {
      throw new Error(e);
    }
    Calendar d1 = dfm.getCalendar();
    setDateFilter("BEFORE", new Calendar[] {d1, null});
    return this;
  }

  public MyResourcesPage resetFilters() {
    try {
      WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
      WebElement resetButton = context.getDriver().findElement(By.id("rf_resetButton"));
      resetButton.click();
      return waitForResultsReload(waiter);
    } catch (NoSuchElementException noseeum) {
    }
    return this;
  }

  public boolean filterByMimeTypeIsPresent() {
    return this.isPresent(By.xpath(".//div[@class='input checkbox filterbymimetype']"));
  }

  // Define XPath for a Row
  private String xpathForRow(int i) {
    return ".//tbody/tr[contains(@class, 'odd') or contains(@class, 'even')][" + i + "]";
  }

  // Verify Show Comment link of an Item
  public boolean isShowCommentLinkPresentAtIndex(int i) {
    return modqueuetable
        .findElement(By.xpath(xpathForRow(i) + "/td[2]"))
        .findElement(By.linkText("Show comment"))
        .isDisplayed();
  }

  // Access Moderation Queue Show Comment Dialog
  public String accessShowCommentViewAtIndex(int i) {
    modqueuetable
        .findElement(By.xpath(xpathForRow(i) + "/td[2]"))
        .findElement(By.linkText("Show comment"))
        .click();
    ModerationQueueShowCommentDialog commentDialog =
        new ModerationQueueShowCommentDialog(context).get();
    String comment = commentDialog.getComment();
    WaitingPageObject<MyResourcesPage> removalWaiter =
        removalWaiter(commentDialog.getLoadedElement());
    commentDialog.closeComment();
    removalWaiter.get();
    return comment;
  }

  // View Item Summary in Moderation Queue
  public SummaryPage accessItemSummeryInModQueueItemAtIndex(int i) {
    modqueuetable
        .findElement(By.xpath(xpathForRow(i) + "/td[1]"))
        .findElement(By.tagName("a"))
        .click();
    return new SummaryPage(context).get();
  }

  @Override
  public ItemListPage resultsPageObject() {
    return new ItemListPage(context);
  }

  public MyResourcesAuthorWebPage editWebPage(String scrapbookItem) {
    if (isNewUI()) {
      editScrapbook(scrapbookItem);
      return new MyResourcesAuthorWebPage(this).get();
    } else {
      return edit(results().getResultForTitle(scrapbookItem), new MyResourcesAuthorWebPage(this));
    }
  }

  public MyResourcesUploadFilesPage editFile(String scrapbookItem) {
    if (isNewUI()) {
      editScrapbook(scrapbookItem);
      return new MyResourcesUploadFilesPage(this).get();
    } else {
      return edit(results().getResultForTitle(scrapbookItem), new MyResourcesUploadFilesPage(this));
    }
  }

  /** Applicable to New UI only to expand the attachment list for a Scrapbook. */
  public void expandAttachmentsForScrapbookItem(String scrapbookName) {
    getScrapbookByName(scrapbookName).click();
    // Wait until the Accordion is expanded.
    waiter.until(
        ExpectedConditions.visibilityOfElementLocated(By.className("MuiAccordionDetails-root")));
  }

  /** Applicable to New UI only to check whether a Scrapbook has a certain number of attachments. */
  public void checkAttachmentNumber(String scrapbookName, int number) {
    expandAttachmentsForScrapbookItem(scrapbookName);
    waiter.until(
        ExpectedConditions.numberOfElementsToBe(
            By.xpath("//div[contains(@class, 'MuiAccordionDetails-root')]/ul/div"), number));
  }

  /** Find tags of a Scrapbook. Both New and Old UI are supported. */
  public String getScrapbookTags(String scrapbookName) {
    if (isNewUI()) {
      return getScrapbookByName(scrapbookName).findElement(By.xpath("//li/span[2]")).getText();
    } else {
      return results().getResultForTitle(scrapbookName).getDetailText("Tags");
    }
  }

  /** Applicable to New UI only to find the action button for editing the provided Scrapbook. */
  public void editScrapbook(String scrapbookName) {
    getScrapbookActionButton(scrapbookName, "Edit").click();
  }

  /** Delete the provided Scrapbook and refresh the page. Both New and Old UI are supported. */
  public MyResourcesPage deleteScrapbook(String scrapbookName) {
    if (isNewUI()) {
      getScrapbookActionButton(scrapbookName, "Delete").click();

      WebElement confirmButton =
          getContext().getDriver().findElement(By.id("confirm-dialog-confirm-button"));
      confirmButton.click();
      // Wait until the dialog is closed.
      getWaiter().until(ExpectedConditions.invisibilityOf(confirmButton));
    } else {
      delete(results().getResultForTitle(scrapbookName));
    }

    return new MyResourcesPage(context, "scrapbook").load();
  }

  /**
   * Check whether the provided Scrapbook has been created or not. Both New and Old UI are
   * supported.
   */
  public boolean isScrapbookCreated(String scrapbookName) {
    return isNewUI()
        ? getAllScrapbooksByName(scrapbookName).size() > 0
        : results().doesResultExist(scrapbookName);
  }

  /**
   * Check whether the provided Scrapbook has been deleted or not. Both New and Old UI are
   * supported.
   */
  public boolean isScrapbookDeleted(String scrapbookName) {
    return isNewUI()
        ? driver.findElements(scrapbookXpath(scrapbookName)).size() == 0
        : !results().doesResultExist(scrapbookName);
  }

  /**
   * Open a webpage. Both New and Old UI are supported. But in New UI, since the page is opened in a
   * new tab, this method will also control the browser to switch to the new tab.
   */
  public void openWebpage(String pageName, String pageTitle) {
    if (isNewUI()) {
      expandAttachmentsForScrapbookItem(pageName);

      WebElement title = driver.findElement(individualPageXpath(pageTitle));
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", title);

      // Switch to the second tab where the index is 1.
      switchTab(1);

    } else {
      results().getResultForTitle(pageName).clickLink(pageTitle);
    }
  }

  /**
   * Check if an individual page of a webpage is present. Both New and Old UI are supported.
   * Recommend to call {@link #expandAttachmentsForScrapbookItem} before calling this method in New
   * UI.
   */
  public boolean isIndividualPagePresent(String scrapbookName, String pageTitle) {
    if (isNewUI()) {
      return waiter
              .until(
                  ExpectedConditions.visibilityOfAllElementsLocatedBy(
                      By.xpath(".//div/span[text()=" + quoteXPath(pageTitle) + "]")))
              .size()
          > 0;
    } else {
      ItemSearchResult searchResult = results().getResultForTitle(scrapbookName, 1);
      return searchResult.isDetailLinkPresent("Web Pages", pageTitle);
    }
  }

  /**
   * Check if an individual page of a webpage is no longer in the page. Both New and Old UI are
   * supported. Recommend to call {@link #expandAttachmentsForScrapbookItem} before calling this
   * method in New UI.
   */
  public boolean isIndividualPageDeleted(String scrapbookName, String pageTitle) {
    if (isNewUI()) {
      return driver.findElements(individualPageXpath(pageTitle)).size() == 0;
    } else {
      ItemSearchResult searchResult = results().getResultForTitle(scrapbookName, 1);
      return !searchResult.isDetailLinkPresent("Web Pages", pageTitle);
    }
  }
}
