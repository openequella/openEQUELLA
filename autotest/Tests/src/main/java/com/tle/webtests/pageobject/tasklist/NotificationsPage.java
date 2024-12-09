package com.tle.webtests.pageobject.tasklist;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.ModerationSearchResult;
import java.util.List;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NotificationsPage
    extends AbstractQueryableSearchPage<
        NotificationsPage, NotificationSearchResults, ModerationSearchResult> {
  public static final String REASON = "reason";
  public static final String COLLECTION = "in";

  @FindBy(id = "bss_clearSelectedButton")
  private WebElement clearSelected;

  @FindBy(id = "bss_selectAllButton")
  private WebElement selectAllButton;

  @FindBy(id = "bss_unselectAllLink")
  private WebElement unselectAllButton;

  @FindBy(id = "bss_viewSelectedLink")
  private WebElement viewSelectedButton;

  public NotificationsPage(PageContext context) {
    super(context);
    loadedBy = byForPageTitle("Notifications");
  }

  @Override
  protected WebElement findLoadedElement() {
    return driver.findElement(loadedBy);
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/notifications.do");
  }

  @Override
  public NotificationSearchResults resultsPageObject() {
    return new NotificationSearchResults(context);
  }

  public void setReasonFilter(String value) {
    setFilter(REASON, value);
  }

  public boolean ensureReasonFilterSelected(String filter) {
    return ensureFilterSelected(REASON, filter);
  }

  public void setCollectionFilter(String value) {
    setFilter(COLLECTION, value);
  }

  public boolean ensureCollectionFilterSelected(String filter) {
    return ensureFilterSelected(COLLECTION, filter);
  }

  public void setFilter(String filterType, String filterValue) {
    WaitingPageObject<NotificationSearchResults> waiter = resultsPageObject.getUpdateWaiter();
    EquellaSelect resourceOptions = new EquellaSelect(context, getFilterControl(filterType));
    resourceOptions.selectByValue(filterValue);
    waitForResultsReload(waiter);
  }

  public boolean ensureFilterSelected(String filterType, String filterValue) {
    EquellaSelect resourceOptions = new EquellaSelect(context, getFilterControl(filterType));
    return resourceOptions.getSelectedValue().equalsIgnoreCase(filterValue);
  }

  public int getNumberOfResults() {
    return results().getTotalAvailable();
  }

  public NotificationsPage selectNotification(int index) {
    // WebElement unselectButton =
    // driver.findElement(By.xpath("//div[contains(@class, 'itemresult-wrapper')]["
    // + index + "]//button[normalize-space(text())='Unselect']"));
    WebElement selectButton =
        driver.findElement(
            By.xpath(
                "//div[contains(@class, 'itemresult-wrapper')]["
                    + index
                    + "]//button[normalize-space(text())='Select']"));
    selectButton.click();
    waiter.until(ExpectedConditions2.stalenessOrNonPresenceOf(selectButton));
    return this;
  }

  public int countSelections() {
    List<WebElement> selected =
        driver.findElements(
            By.xpath(
                "//div[contains(@class, 'itemresult-wrapper')]/div[contains(@class,"
                    + " 'selected')]/.."));
    return selected.size();
  }

  public void clearSelected() {
    WaitingPageObject<NotificationSearchResults> updateWaiter = resultsPageObject.getUpdateWaiter();
    clearSelected.click();
    Alert alert = waiter.until(ExpectedConditions.alertIsPresent());
    alert.accept();
    waitForResultsReload(updateWaiter);
  }

  public void selectAll() {
    selectAllButton.click();
    waiter.until(ExpectedConditions2.presenceOfElement(unselectAllButton));
  }

  public void unselectAll() {
    WebElement firstResult =
        driver.findElement(By.xpath("//div[contains(@class, 'itemresult-wrapper')][1]/div[1]"));
    unselectAllButton.click();
    waiter.until(ExpectedConditions2.stalenessOrNonPresenceOf(firstResult));
  }

  public SelectedDialog viewSelected() {
    viewSelectedButton.click();
    return new SelectedDialog(context, this);
  }

  public void clearSelectedNoSelections() {
    clearSelected.click();
    waiter.until(ExpectedConditions2.acceptAlert());
  }

  public class SelectedDialog extends AbstractPage<SelectedDialog> {
    @FindBy(id = "bss_bulkDialog_close")
    private WebElement closeButton;

    private NotificationsPage parent;

    public SelectedDialog(PageContext context, NotificationsPage parent) {
      super(context);
      waiter.until(ExpectedConditions2.presenceOfElement(closeButton));
      this.parent = parent;
    }

    public void unselectNotification(int index) {
      WebElement unselectRowButton =
          driver.findElement(
              By.xpath(
                  "//tr[@id='bss_bulkDialog_selectionsTabletrbss_bulkDialog_i"
                      + index
                      + "']/td[2]/a"));
      unselectRowButton.click();
      waiter.until(ExpectedConditions2.stalenessOrNonPresenceOf(unselectRowButton));
    }

    public NotificationsPage closeDialog() {
      ExpectedCondition<Boolean> removalContition =
          ExpectedConditions2.stalenessOrNonPresenceOf(closeButton);
      closeButton.click();
      waiter.until(removalContition);
      return parent;
    }
  }
}
