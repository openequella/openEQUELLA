package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * This page represents the search page settings, under the settings menu in Search -> Search page.
 */
public class SearchSettingsPage extends AbstractPage<SearchSettingsPage> {
  public static final String SEARCH_SETTINGS_SECTION_TITLE = "Search page settings";

  public enum Order {
    rank,
    datemodified,
    name,
    rating
  }

  @FindBy(id = "_showNonLiveCheckbox")
  private WebElement includeNonLive;

  @FindBy(id = "_authenticateByDefault")
  private WebElement genAuthFeeds;

  @FindBy(id = "_saveButton")
  private WebElement save;

  @FindBy(id = "cs_dc")
  private WebElement disableCloud;

  @FindBy(id = "_disableGallery")
  private WebElement disableGalleryCheckbox;

  @FindBy(id = "_sortOrder")
  private WebElement sortOrderDropdown;

  public SearchSettingsPage(PageContext context) {
    super(context, By.xpath("//h5[text()='" + SEARCH_SETTINGS_SECTION_TITLE + "']"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "page/searchsettings");
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
  }

  public SearchSettingsPage setOrder(Order order) {
    sortOrderDropdown.click();

    By by = By.xpath("//li[@data-value='" + order.name() + "']");
    waiter.until(ExpectedConditions.elementToBeClickable(by));
    find(getSearchContext(), by).click();
    return get();
  }

  public SearchSettingsPage includeNonLive(boolean showNonLive) {
    if (includeNonLive.isSelected() != showNonLive) {
      // click on the title to remove focus - fixes flakiness
      find(getSearchContext(), By.xpath("//h5[text()='" + SEARCH_SETTINGS_SECTION_TITLE + "']"))
          .click();
      includeNonLive.click();
    }
    return get();
  }

  public SearchSettingsPage setGenerateAuthFeeds(boolean authFeeds) {
    if (genAuthFeeds.isSelected() != authFeeds) {
      genAuthFeeds.click();
    }
    return get();
  }

  public SearchSettingsPage setDisableCloud(boolean disable) {
    if (disableCloud.isSelected() != disable) {
      disableCloud.click();
    }
    return get();
  }

  public SearchSettingsPage setDisableImageGallery(boolean disable) {
    if (disableGalleryCheckbox.isSelected() != disable) {
      disableGalleryCheckbox.click();
    }
    return get();
  }

  public SearchSettingsPage save() {
    waiter.until(ExpectedConditions.elementToBeClickable(save));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", save);
    waiter.until(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath("//span[text()='Saved successfully.']")));
    return get();
  }
}
