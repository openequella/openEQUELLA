package com.tle.webtests.pageobject.selection;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import java.net.URL;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class SelectionSession extends AbstractPage<SelectionSession> {

  private WebElement getQuickSearchField() {
    return driver.findElement(By.id(getQuickSearchSectionId("_q")));
  }

  private WebElement getQuickSearchButton() {
    return driver.findElement(By.id(getQuickSearchSectionId("_s")));
  }

  @FindBy(id = "srsh_box")
  private WebElement recentBox;

  private WebElement getQuickFile() {
    return driver.findElement(By.id(getQuickUploadSectionId("_fileUploader")));
  }

  private WebElement getContributeButton() {
    return findWithId(getQuickUploadContributeSectionId(), "_contributeButton");
  }

  @FindBy(id = "selection-header")
  private WebElement selectionHeader;

  @FindBy(id = "_slcl_s")
  private WebElement finishStructuredButton;

  public SelectionSession(PageContext context) {
    super(context);
  }

  public String getQuickSearchSectionId(String postFix) {
    return "psp" + postFix;
  }

  public String getQuickUploadContributeSectionId() {
    return "quc";
  }

  public String getQuickUploadSectionId(String postFix) {
    return "qu" + postFix;
  }

  @Override
  protected WebElement findLoadedElement() {
    return selectionHeader;
  }

  /**
   * Will only work if "searchResources" was used
   *
   * @return
   */
  public SearchPage getSearchPage() {
    return new SearchPage(context).get();
  }

  /**
   * Will only work if "contribute" was used
   *
   * @return
   */
  public ContributePage getContributePage() {
    return new ContributePage(context).get();
  }

  public ItemListPage homeSearch(String query) {
    getQuickSearchField().clear();
    /*
     * BUG in Selenium -
     * http://code.google.com/p/selenium/issues/detail?id=1723
     */
    if (query.contains("(") && !query.contains(Keys.chord(Keys.SHIFT, "9"))) {
      query = query.replace("(", Keys.chord(Keys.SHIFT, "9"));
    }
    getQuickSearchField().sendKeys(query);
    getQuickSearchButton().click();
    return new ItemListPage(context).get();
  }

  public ItemListPage homeExactSearch(String query) {
    return homeSearch('"' + query + '"');
  }

  public boolean favItemExists(String fullName) {
    setRecentTab("Favourites");
    return !recentBox
        .findElements(
            By.xpath(
                ".//a[(contains(@class, 'even') or contains(@class,'odd')) and text()="
                    + quoteXPath(fullName)
                    + "]"))
        .isEmpty();
  }

  public void clickShowAllFavourites() {
    setRecentTab("Favourites");
    recentBox.findElement(By.id("fav_showAll")).click();
  }

  public boolean recentContributionExists(String fullName) {
    setRecentTab("Contributed");
    return !recentBox
        .findElements(
            By.xpath(
                ".//a[(contains(@class, 'even') or contains(@class,'odd')) and text()="
                    + quoteXPath(fullName)
                    + "]"))
        .isEmpty();
  }

  public boolean recentSelectedExists(String fullName) {
    setRecentTab("Selected");
    return !recentBox
        .findElements(
            By.xpath(
                ".//a[(contains(@class, 'even') or contains(@class,'odd')) and text()="
                    + quoteXPath(fullName)
                    + "]"))
        .isEmpty();
  }

  public boolean setRecentTab(String name) {
    if (!isPresent(By.xpath("//strong[text()='" + name + "']"))) {
      clickAndRemove(driver.findElement(By.xpath("//a[text()='" + name + "']")));
      return true;
    }
    return false;
  }

  public boolean favSearchExists(String search) {
    return favItemExists("Search results for " + '"' + search + '"');
  }

  public <T extends PageObject> T quickContribute(URL file, WaitingPageObject<T> returnTo) {
    getQuickFile().sendKeys(getPathFromUrl(file));
    return returnTo.get();
  }

  public ContributePage contribute() {
    getContributeButton().click();
    return new ContributePage(context).get();
  }

  public WizardPageTab contributeSingle() {
    getContributeButton().click();
    return new WizardPageTab(context, 0).get();
  }

  public <T extends PageObject> T finishedSelecting(WaitingPageObject<T> returnTo) {
    return new SelectionStatusPage(context).get().finishSelections().returnSelection(returnTo);
  }

  public SelectionCheckoutPage finish() {
    return new SelectionStatusPage(context).get().finishSelections();
  }

  public <T extends PageObject> T finishStructured(WaitingPageObject<T> returnTo) {
    finishStructuredButton.click();
    return returnTo.get();
  }

  public List<WebElement> getTopLinkMenu() {
    return driver.findElements(By.xpath("//div[@class='centered-pills']/ul/li/a"));
  }

  public <T extends PageObject> T clickBrowseTopLinkMenu(WaitingPageObject<T> returnTo) {
    driver.findElement(By.xpath("//div[@class='centered-pills']/ul/li/a[text()='Browse']")).click();
    return returnTo.get();
  }

  public boolean hasBreadcrumbShow() {
    return isPresent(driver.findElement(By.xpath("id('breadcrumbs')/span/a")));
  }

  public boolean isNewSearchPresent() {
    ExpectedCondition<Boolean> isPresent =
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver driver) {
            try {
              return isVisible(By.xpath("//*[contains(text(),'Search result')]"));
            } catch (StaleElementReferenceException | NoSuchElementException e) {
              return false;
            }
          }
        };

    return waiter.until(isPresent);
  }
}
