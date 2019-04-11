package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SearchPage
    extends AbstractQueryableSearchPage<SearchPage, ItemListPage, ItemSearchResult> {
  @FindBy(id = "searchform-in")
  private WebElement inElem;

  @FindBy(id = "cfsa_fd_opener")
  private WebElement favouriteSearch;

  @FindBy(id = "rrsa_b")
  private WebElement searchOtherRepos;

  @FindBy(id = "fbo_so_opener")
  private WebElement selectUser;

  @FindBy(id = "fbo_r")
  private WebElement clearUser;

  private SearchTabsPage searchTabs;
  private QuerySection realQuerySection;

  @FindBy(xpath = "//button[normalize-space(text())='Select summary page']")
  private WebElement selectSummaryPageButton;

  @FindBy(className = "add-to-favourites")
  private WebElement saveSearchOpener;

  @FindBy(id = "sra_share")
  private WebElement shareSearchOpener;

  @FindBy(xpath = "//div[@id='result-type-select']/a[normalize-space(@title) = 'Standard']")
  private WebElement standardLink;

  @FindBy(xpath = "//div[@id='result-type-select']/a[normalize-space(@title) = 'Images']")
  private WebElement imagesLink;

  @FindBy(xpath = "//div[@id='result-type-select']/a[normalize-space(@title) = 'Videos']")
  private WebElement videosLink;

  @FindBy(xpath = "//div[@id='result-type-select']/strong[normalize-space(text()) = 'Standard']")
  private WebElement standardTypeSelected;

  @FindBy(xpath = "//div[@id='result-type-select']/strong[normalize-space(text()) = 'Images']")
  private WebElement imagesTypeSelected;

  @FindBy(xpath = "//div[@id='result-type-select']/strong[normalize-space(text()) = 'Videos']")
  private WebElement videosTypeSelected;

  public SearchPage(PageContext context) {
    super(context);
    setMustBeVisible(false);
    searchTabs = new SearchTabsPage(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return inElem;
  }

  @Override
  protected AbstractQuerySection<?> createQuerySection() {
    realQuerySection = new QuerySection(context);
    return realQuerySection;
  }

  public boolean isSelectButtonExist() {
    return isVisible(selectSummaryPageButton);
  }

  public QuerySection query() {
    return realQuerySection;
  }

  public SearchTabsPage getSearchTabs() {
    return searchTabs;
  }

  @Override
  public SearchScreenOptions openScreenOptions() {
    return new SearchScreenOptions(resultsPageObject()).open();
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
  }

  public WebElement getSelectedContainer() {
    return driver.findElement(
        By.xpath("id('searchform')//div[contains(@class, 'selectedcontainer')]"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "searching.do");
  }

  public ItemListPage search() {
    return querySection.search(resultsPageObject.getUpdateWaiter());
  }

  public SearchPage setIncludeNonLive(boolean b) {
    openScreenOptions().setNonLiveOption(b);
    return this;
  }

  protected void setWithin(String name) {
    getSelectedContainer().click();
    WebElement richDropdown =
        waitForElement(By.xpath("//div[contains(@class,'richdropdown active')]"));
    richDropdown.findElement(By.xpath("ul/li/a[text()=" + quoteXPath(name) + "]")).click();
  }

  public SearchPage setWithinCollection(String collection) {
    WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
    setWithin(collection);
    return waitForResultsReload(waiter);
  }

  public SearchPage setWithinAll() {
    return setWithinCollection("Within all resources");
  }

  public PowerSearchPage setWithinPowerSearch(String powersearch) {
    SearchPage sp = this;
    if (getSelectedWithin().equals(powersearch)) {
      setWithinAll();
      sp = new SearchPage(context).get();
    }
    sp.setWithin(powersearch);
    return new PowerSearchPage(this).get();
  }

  public String getSelectedWithin() {
    return getSelectedContainer().findElement(By.className("selectedtext")).getText();
  }

  public SearchPage saveSearchToFavourites(String searchName) {
    saveSearchOpener.click();
    FavouriteSearchIntegrationPage favouriteSearchIntegrationPage =
        new FavouriteSearchIntegrationPage(context);
    return favouriteSearchIntegrationPage.addSearchToFavourite(searchName, this);
  }

  public String getShareSearchUrl() {
    openShare();
    ShareSearchQueryIntegrationPage sharePage = new ShareSearchQueryIntegrationPage(context);
    String shareUrl = sharePage.getSharedUrl();
    shareSearchOpener.click();
    return shareUrl;
  }

  public void saveSearch(String searchName) {
    FavouriteSearchDialog dialog = new FavouriteSearchDialog(context, "cfsa").open();
    ReceiptPage.waiter(
            "Successfully added this search to your favourites", dialog.favourite(searchName, this))
        .get();
  }

  public ShareSearchQuerySection shareSearch() {
    return new ShareSearchQuerySection(context).open();
  }

  public RemoteRepoPage searchOtherRepositories() {
    return new RemoteRepoPage(context).load();
  }

  public static SummaryPage searchAndView(PageContext context, String itemFullName) {
    return new SearchPage(context)
        .load()
        .search('"' + itemFullName + '"')
        .viewFromTitle(itemFullName);
  }

  public static ItemListPage searchExact(PageContext context, String itemFullName) {
    return new SearchPage(context).load().search('"' + itemFullName + '"');
  }

  public SearchPage setOwnerFilter(String owner) {
    openFilters();
    WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
    selectUser.click();
    SelectUserDialog dialog = new SelectUserDialog(context, "fbo_so").get();
    dialog.search(owner).selectAndFinish(owner, waiter);
    return this;
  }

  public SearchPage clearOwnerFilter() {
    openFilters();
    WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
    clearUser.click();
    waiter.get();
    return this;
  }

  public boolean isOwnerSelected(String owner) {
    return isPresent(By.xpath("id('owner')/p/span[@title=" + quoteXPath(owner) + "]"));
  }

  public boolean hasResourceTypeFilter(String filterName) {
    openFilters();
    return isPresent(getByForResourceTypeFilter(filterName));
  }

  private By getByForResourceTypeFilter(String filterName) {
    return By.xpath("//ul[@id='mt']//label[text()=" + quoteXPath(filterName) + "]/../input");
  }

  public SearchPage clearResourceTypeFilters() {
    List<WebElement> webElements = driver.findElements(By.xpath("//ul[@id='mt']//input"));
    webElements.forEach(
        webElement -> {
          if (webElement.isSelected()) {
            webElement.click();
            waiter.until(ExpectedConditions.elementSelectionStateToBe(webElement, false));
          }
        });
    return get();
  }

  public SearchPage checkResourceTypeFilter(String filterName, boolean check) {
    WebElement element = driver.findElement(getByForResourceTypeFilter(filterName));
    if (element.isSelected() != check) {
      element.click();
      waiter.until(ExpectedConditions.elementSelectionStateToBe(element, check));
    }
    return get();
  }

  public boolean isPowerSearch() {
    return isPresent(By.id("searchform-editquery"));
  }

  public PowerSearchPage editQuery() {
    driver.findElement(By.id("searchform-editquery")).click();
    return new PowerSearchPage(this).get();
  }

  public SearchPage clearQuery() {
    if (isPresent(By.id("sq_clearQueryButton"))) {
      WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
      driver.findElement(By.id("sq_clearQueryButton")).click();
      return waitForResultsReload(waiter);
    } else {
      return get();
    }
  }

  @Override
  public ItemListPage resultsPageObject() {
    return new ItemListPage(context);
  }

  public boolean hasFavouriteSearchOption() {
    return isVisible(favouriteSearch);
  }

  public String totalItemFound() {
    String searchResults = driver.findElement(By.id("searchresults-stats")).getText();
    return searchResults.split("\\s+")[4];
  }

  private boolean isResultType(String type) {
    return isPresent(By.xpath("id('result-type-select')/input[@value = '" + type + "']"));
  }

  /**
   * @param type
   * @return
   */
  public SearchPage setResultType(String type) {
    WaitingPageObject<SearchPage> pageWaiter = this;
    WaitingPageObject<ItemListPage> resultWaiter = null;
    if (type.toLowerCase().equals("standard") && !isResultType("standard")) {
      resultWaiter = resultsPageObject.getUpdateWaiter();
      pageWaiter = ExpectWaiter.waiter(ExpectedConditions.visibilityOf(standardTypeSelected), this);
      standardLink.click();
    } else if (type.toLowerCase().equals("images") && !isResultType("gallery")) {
      resultWaiter = resultsPageObject.getUpdateWaiter();
      pageWaiter = ExpectWaiter.waiter(ExpectedConditions.visibilityOf(imagesTypeSelected), this);
      imagesLink.click();
    } else if (type.toLowerCase().equals("videos") && !isResultType("video")) {
      resultWaiter = resultsPageObject.getUpdateWaiter();
      pageWaiter = ExpectWaiter.waiter(ExpectedConditions.visibilityOf(videosTypeSelected), this);
      videosLink.click();
    }
    SearchPage ret = pageWaiter.get();
    if (resultWaiter != null) {
      resultWaiter.get();
    }
    return ret;
  }
}
