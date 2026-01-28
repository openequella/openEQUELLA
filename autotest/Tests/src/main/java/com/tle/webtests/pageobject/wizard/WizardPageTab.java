package com.tle.webtests.pageobject.wizard;

import com.tle.common.Utils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.Map;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class WizardPageTab extends AbstractWizardControlPage<WizardPageTab> {
  @FindBy(id = "nav_nextButton")
  private WebElement nextButton;

  @FindBy(id = "nav_previousButton")
  private WebElement prevButton;

  @FindBy(id = "wizard-major-actions")
  private WebElement updateAjax;

  @FindBy(xpath = "//div[@id='wizard-pagelist']/ul")
  private WebElement pageList;

  public WizardPageTab(PageContext context, int pageNum) {
    super(context, By.className("wizard-layout"), pageNum);
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
    try {
      String pageNumStr =
          driver.findElement(By.xpath("//input[@name='pages.pg']")).getAttribute("value");
      if (!pageNumStr.equals(Integer.toString(pageNum))) {
        throw new Error("Wrong page: " + pageNum + ":" + pageNumStr);
      }
    } catch (NoSuchElementException nse) {
      if (pageNum != 0) {
        throw new Error(nse);
      }
    }
  }

  @Override
  public String getControlId(int ctrlNum) {
    return "p" + pageNum + "c" + ctrlNum;
  }

  @Override
  public WaitingPageObject<WizardPageTab> getGeneralWaiter() {
    return ajaxUpdate(updateAjax);
  }

  public WizardPageTab setCheckNextAppear(int ctrlnum, String value, boolean checked) {
    return setCheckWaiter(
        ctrlnum,
        value,
        checked,
        ExpectWaiter.waiter(ExpectedConditions2.presenceOfElement(nextButton), this));
  }

  public WizardPageTab setCheckNextDisappear(int ctrlnum, String value, boolean checked) {
    return setCheckWaiter(
        ctrlnum,
        value,
        checked,
        ExpectWaiter.waiter(ExpectedConditions2.stalenessOrNonPresenceOf(nextButton), this));
  }

  public WizardPageTab next(WaitingPageObject<WizardPageTab> expect) {
    nextButton.click();
    pageNum++;
    return expect.get();
  }

  public WizardPageTab next() {
    int currentPage = getCurrentPageIndex();
    return next(ExpectWaiter.waiter(new PageCondition(currentPage + 1), this));
  }

  public WizardPageTab prev(WaitingPageObject<WizardPageTab> expect) {
    prevButton.click();
    pageNum--;
    return expect.get();
  }

  public WizardPageTab prev() {
    int currentPage = getCurrentPageIndex();
    return prev(ExpectWaiter.waiter(new PageCondition(currentPage - 1), this));
  }

  public boolean hasControl(int ctrlNum) {
    try {
      driver.findElement(By.id(getControlId(ctrlNum)));
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  public String getNextButtonText() {
    if (isPresent(nextButton)) {
      return nextButton.getText().replaceAll("<i.*</i>", "").trim();
    }
    return "";
  }

  public String getPrevButtonText() {
    if (isPresent(prevButton)) {
      return prevButton.getText().replaceAll("<i.*</i>", "").trim();
    }
    return "";
  }

  public boolean hasPage(String text, boolean isLink) {
    return isPresent(
        pageList, By.xpath("li/" + (isLink ? "a" : "span") + "[text()=" + quoteXPath(text) + "]"));
  }

  public boolean deletestatus(String change) {
    String trackerStatus =
        driver.findElement(By.xpath("//div[@id='adjacentuls']/ul[1]/li[4]")).getText();
    return trackerStatus.equals(change);
  }

  /**
   * Retrieves the current active page index from the wizard page list.
   *
   * <p>This method identifies which page in the wizard navigation is currently active by locating
   * the list item with the "active" class and parsing its ID attribute. The implementation handles
   * transient DOM staleness that may occur during wizard page transitions.
   *
   * @return the zero-based index of the currently active wizard page
   * @throws RuntimeException if the active page element cannot be found or its index cannot be
   *     determined (e.g., if the wizard page list is not present or has an unexpected structure)
   */
  public int getCurrentPageIndex() {
    return retrieveActivePageIndex()
        .orElseThrow(
            () ->
                new RuntimeException("Failed to retrieve active page index from wizard page list"));
  }

  /**
   * Retrieves the active page index from the wizard page list, handling transient staleness during
   * DOM updates.
   *
   * <p>This method uses a waiter to retry the entire operation (finding the element AND reading its
   * attributes) since both the element lookup and getAttribute() can trigger stale element
   * exceptions.
   *
   * <p>Expected ID format: "pages_N" where N is the page index number.
   *
   * @return an Optional containing the parsed page index if successful, or empty if retrieval timed
   *     out
   */
  private Optional<Integer> retrieveActivePageIndex() {
    return Optional.ofNullable(
        waiter.until(
            driver -> {
              try {
                WebElement activePageElement =
                    pageList.findElement(By.xpath("li[@class='active']"));
                return parsePageId(activePageElement.getAttribute("id"));
              } catch (StaleElementReferenceException | NoSuchElementException e) {
                return null; // Waiter will retry
              } catch (WebDriverException e) {
                // Handle Chrome-specific stale node error
                if (ExpectedConditions2.isChromeStaleNodeException(e)) {
                  return null; // Waiter will retry
                }
                throw e;
              }
            }));
  }

  /**
   * Parses the page index from a page element's ID attribute value.
   *
   * <p>Expected ID format: "pages_N" where N is the page index number.
   *
   * @param id the ID attribute value to parse
   * @return the parsed page index, or null if parsing failed
   */
  private Integer parsePageId(String id) {
    final String prefix = "pages_";

    return Optional.ofNullable(id)
        .filter(idValue -> idValue.startsWith(prefix))
        .map(idValue -> Utils.safeSubstring(idValue, prefix.length()))
        .map(Integer::parseInt)
        .orElse(null);
  }

  public String getCurrentPageName() {
    return pageList.findElement(By.xpath("li[@class='active']/span")).getText();
  }

  public WizardPageTab clickPage(String text, int pageNum) {
    if (hasPage(text, true)) {
      pageList.findElement(By.xpath("li/a[text()=" + quoteXPath(text) + "]")).click();
      return ExpectWaiter.waiter(new PageCondition(pageNum), new WizardPageTab(context, pageNum))
          .get();
    } else {
      throw new RuntimeException("Page '" + text + "' is not present or is not clickable");
    }
  }

  /**
   * Fills multiple edit boxes on the wizard page.
   *
   * @param fields A map where the key is the control index(start from 1) and the value is the text
   *     to enter.
   */
  public void fillFields(Map<Integer, String> fields) {
    fields.forEach(this::editbox);
  }

  /**
   * An {@link ExpectedCondition} that waits for the wizard to navigate to a specific page index.
   *
   * <p>This condition is used by Selenium's WebDriverWait to poll until the wizard's active page
   * matches the expected page index. It's typically used after clicking navigation buttons or page
   * links to ensure the page transition has completed before proceeding with further actions.
   *
   * <p>The condition relies on {@link #getCurrentPageIndex()} which handles all staleness and retry
   * logic internally, so this class can simply compare the result to the expected value.
   */
  private class PageCondition implements ExpectedCondition<Boolean> {
    private final int expectedPageIndex;

    /**
     * Creates a condition that waits for the wizard to reach the specified page.
     *
     * @param expectedPageIndex the zero-based index of the page to wait for
     */
    public PageCondition(int expectedPageIndex) {
      this.expectedPageIndex = expectedPageIndex;
    }

    @Override
    public Boolean apply(WebDriver driver) {
      return getCurrentPageIndex() == expectedPageIndex;
    }

    @Override
    public String toString() {
      return "pageCondition " + expectedPageIndex;
    }
  }
}
