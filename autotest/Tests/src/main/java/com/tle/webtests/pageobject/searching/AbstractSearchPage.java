package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractSearchPage<
        T extends AbstractSearchPage<T, RL, SR>,
        RL extends AbstractResultList<RL, SR>,
        SR extends SearchResult<SR>>
    extends AbstractPage<T> {
  private static final TimeZone USERS_TIMEZONE = TimeZone.getTimeZone("America/Chicago");
  public static final String SEARCH_WITHIN_COLLECTION_ID = "searchform-collection";
  public static final String SORT_BUTTON_ID = "sra_sort";
  public static final String SHARE_BUTTON_ID = "sra_share";
  public static final String FILTER_BUTTON_ID = "sra_filter";
  public static final String FILTER_BOX_COLLECTION_ID = "in";
  public static final String FILTER_RUN_SEARCH_CONTROL_ID = "fbakw_s";
  protected RL resultsPageObject;

  @FindBy(id = SORT_BUTTON_ID)
  private WebElement sortOpener;

  public AbstractSearchPage(PageContext context) {
    this(context, -1);
  }

  public AbstractSearchPage(PageContext context, int timeout) {
    super(context, null, timeout);
    resultsPageObject = resultsPageObject();
  }

  @Override
  protected abstract WebElement findLoadedElement();

  public abstract RL resultsPageObject();

  public WaitingPageObject<RL> getResultsUpdateWaiter() {
    return resultsPageObject.getUpdateWaiter();
  }

  public final RL results() {
    return resultsPageObject.get();
  }

  public boolean hasResults() {
    return results().isResultsAvailable();
  }

  public String getStats() {
    String fullStats = null;
    By stats = By.id("searchresults-stats");
    if (isPresent(stats)) {
      fullStats = driver.findElement(stats).getText();
    }
    return fullStats;
  }

  @SuppressWarnings("unchecked")
  public T setPerPage(String perPage) {
    AbstractSearchPageScreenOptions<?> screenOptions = openScreenOptions();
    WaitingPageObject<RL> waiter = resultsPageObject.getUpdateWaiter();
    screenOptions.setPerPage(perPage);
    waiter.get();
    return (T) this;
  }

  public int getPerPage() {
    return openScreenOptions().getPerPage();
  }

  public AbstractSearchPageScreenOptions<?> openScreenOptions() {
    return new DefaultSearchPageScreenOptions(context).open();
  }

  public T setSort(String sort) {
    WaitingPageObject<RL> waiter = resultsPageObject.getUpdateWaiter();
    new EquellaSelect(context, getSortList()).selectByValue(sort);
    return waitForResultsReload(waiter);
  }

  public WebElement getSortList() {
    WebElement sortList = getCollapsibleControl("sort", SORT_BUTTON_ID);
    return sortList;
  }

  /**
   * @param controlId the id of the inner control, to override the default of "sort", so we can ask
   *     for "rs" (reverse sort checkbox) for example.
   * @return
   */
  public WebElement getSortList(String controlId) {
    WebElement collapsibleControl = getCollapsibleControl(controlId, SORT_BUTTON_ID);
    return collapsibleControl;
  }

  public WebElement getFilterControl() {
    WebElement sortList = getCollapsibleControl("q", getFilterOpenerId());
    return sortList;
  }

  /**
   * @param controlId the id of the inner control, to override the default of "sort", so we can ask
   *     for "in" (institution) or "dr" (date range) for example.
   * @return
   */
  public WebElement getFilterControl(String controlId) {
    WebElement filterControl = getCollapsibleControl(controlId, getFilterOpenerId());
    return filterControl;
  }

  /**
   * @param inputControlId
   * @param controlName
   * @return
   */
  protected WebElement getCollapsibleControl(final String inputControlId, String controlName) {
    WebElement collapsibleElement = null;
    try {
      collapsibleElement = driver.findElement(By.id(inputControlId));
    } catch (NoSuchElementException e) {
      WebElement opener = driver.findElement(By.id(controlName));
      scrollToElement(opener);
      opener.click();
      waiter.until(
          new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver arg0) {
              return Boolean.valueOf(isPresent(By.id(inputControlId)));
            }
          });
      collapsibleElement = driver.findElement(By.id(inputControlId));
    }
    return collapsibleElement;
  }

  public boolean ensureSortSelected(String sortOption) {
    EquellaSelect sortOptions = new EquellaSelect(context, getSortList());
    return sortOptions.getSelectedValue().equalsIgnoreCase(sortOption);
  }

  public T selectViaFilterFromResourceCollection(String resourceOption) {
    EquellaSelect resourceOptions =
        new EquellaSelect(context, getFilterControl(FILTER_BOX_COLLECTION_ID));
    String selectedText = resourceOptions.getSelectedText();
    if (!resourceOption.equals(selectedText)) {
      WaitingPageObject<RL> waiter = resultsPageObject.getUpdateWaiter();
      resourceOptions.selectByVisibleText(resourceOption);
      return waitForResultsReload(waiter);
    }
    return get();
  }

  public T waitForResultsReload(WaitingPageObject<RL> resultWaiter) {
    resultWaiter.get();
    return get();
  }

  public T clickPaging(int page) {
    WaitingPageObject<RL> waiter = resultsPageObject.getUpdateWaiter();
    driver
        .findElement(
            By.xpath("id('page')/ul/li/a[text()=" + quoteXPath(String.valueOf(page)) + "]"))
        .click();
    return waitForResultsReload(waiter);
  }

  public boolean onPage(int page) {
    return isPresent(By.xpath("id('page')/ul/li/span[text()='" + page + "']"));
  }

  public int getCurrentPage() {
    By currentPage = By.xpath("id('page')/ul/li/span");
    if (isPresent(currentPage)) {
      return Integer.parseInt(driver.findElement(currentPage).getText());
    }
    return -1;
  }

  public boolean hasPaging() {
    return getCurrentPage() != -1;
  }

  public FilterByDateSectionPage getDateFilter() {
    openFilters();
    return new FilterByDateSectionPage(context, resultsPageObject).get();
  }

  public T setDateFilter(String option, Calendar[] dates) {
    openFilters();
    new FilterByDateSectionPage(context, resultsPageObject).get().setDateFilter(option, dates);
    return get();
  }

  public T setItemStatusFilter(String status) {
    openFilters();
    new FilterByItemStatusSectionPage(context, resultsPageObject).get().setItemStatusFilter(status);
    return get();
  }

  /**
   * @param date format yyyy-MM-dd
   * @return
   */
  public T setDateFilterBefore(String date) throws ParseException {
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
    dfm.setTimeZone(USERS_TIMEZONE);
    dfm.parse(date);
    Calendar d1 = dfm.getCalendar();
    openFilters();
    new FilterByDateSectionPage(context, resultsPageObject)
        .get()
        .setDateFilter("BEFORE", new Calendar[] {d1});
    return get();
  }

  // Abstract all the calender making stuff to the page because I need to do
  // it a few times
  // Just for convenience, use setDateFilter if you feel like building your
  // own calenders...
  /**
   * @param date1
   * @param date2<br>
   *     Format: yyyy-MM-dd<br>
   *     date-date = between<br>
   *     null - date = before<br>
   *     date - null = after
   * @return
   * @throws ParseException
   */
  public T filterByDates(String date1, String date2) throws ParseException {
    Calendar c1 = null;
    Calendar c2 = null;
    String option = null;

    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat dfm2 = new SimpleDateFormat("yyyy-MM-dd");
    dfm.setTimeZone(USERS_TIMEZONE);
    dfm2.setTimeZone(USERS_TIMEZONE);

    if (date1 != null) {
      dfm.parse(date1);
      c1 = dfm.getCalendar();
      option = "AFTER";
    }
    if (date2 != null) {
      dfm2.parse(date2);
      c2 = dfm2.getCalendar();
      option = "BEFORE";
    }
    if (date1 != null && date2 != null) {
      option = "BETWEEN";
    }
    c1 = c1 == null ? c2 : c1;
    c2 = c1 == c2 ? null : c2; // Trust me
    return this.setDateFilter(option, new Calendar[] {c1, c2});
  }

  public RL exactQuery(PrefixedName query) {
    return exactQuery(query.toString());
  }

  public RL exactQuery(String query) {
    return search('"' + query + '"');
  }

  public abstract RL search(String query);

  public void delete(SR searchResult) {
    searchResult.clickActionConfirm("Delete", true, updateWaiter());
  }

  public <P extends PageObject> P edit(SR searchResult, WaitingPageObject<P> editorPage) {
    return searchResult.clickAction("Edit", editorPage);
  }

  protected void openSort() {
    if (!isPresent(By.className("sortaction"))) {
      sortOpener.click();
      waiter.until(ExpectedConditions.visibilityOfElementLocated(By.className("sortaction")));
    }
  }

  protected void openFilters() {
    By filterContent = By.xpath("id('actioncontent')/div[contains(@class, 'filter')]");
    if (!isPresent(filterContent)) {
      driver.findElement(By.id(getFilterOpenerId())).click();
      waiter.until(ExpectedConditions.visibilityOfElementLocated(filterContent));
    }
  }

  protected void openShare() {
    By shareContent = By.xpath("id('actioncontent')/div[contains(@class, 'sharesearchquery')]");
    if (!isPresent(shareContent)) {
      driver.findElement(By.id(getShareOpenerId())).click();
      waiter.until(ExpectedConditions.visibilityOfElementLocated(shareContent));
    }
  }

  protected String getFilterOpenerId() {
    return FILTER_BUTTON_ID;
  }

  protected String getShareOpenerId() {
    return SHARE_BUTTON_ID;
  }
}
