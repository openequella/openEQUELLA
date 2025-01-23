package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractResultList<
        T extends AbstractResultList<T, SR>, SR extends SearchResult<SR>>
    extends AbstractPage<T> {
  public AbstractResultList(PageContext context, int timeoutSeconds) {
    super(context, null, timeoutSeconds);
  }

  public AbstractResultList(PageContext context) {
    super(context);
  }

  public abstract WebElement getResultsDiv();

  @Override
  protected WebElement findLoadedElement() {
    return getResultsDiv();
  }

  public WaitingPageObject<T> getUpdateWaiter() {
    WebElement firstChild =
        ((WrapsElement) getResultsDiv().findElement(By.xpath("*[1]"))).getWrappedElement();
    return ExpectWaiter.waiter(
        ExpectedConditions.and(
            ExpectedConditions2.stalenessOrNonPresenceOf(firstChild),
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("id('searchresults')[div[@class='itemlist'] or h3]"))),
        this);
  }
  ;

  protected static String getXPathForTitle(String title) {
    return "//div/div[contains(@class,'itemresult-wrapper') and .//h3/a[normalize-space(string())="
        + quoteXPath(normaliseSpace(title))
        + "]]";
  }

  protected static By getByForResult(String title, int index) {
    return By.xpath(getXPathForTitle(title) + "[" + index + "]");
  }

  protected static By getByForResult(int index) {
    return By.xpath("//div/div[contains(@class,'itemresult-wrapper')][" + index + "]");
  }

  public SR getResultForTitle(PrefixedName title, int index) {
    return getResultForTitle(title.toString(), index);
  }

  public SR getResultForTitle(String title, int index) {
    return createResult(getResultsDiv(), getByForResult(title, index)).get();
  }

  public SR getResultForTitle(PrefixedName title) {
    return getResultForTitle(title.toString());
  }

  public SR getResultForTitle(String title) {
    return createResult(getResultsDiv(), getByForResult(title, 1)).get();
  }

  /**
   * Get the result at the specified index (1 based)
   *
   * @param index - an integer greater than or equal to 1
   * @return the result
   */
  public SR getResult(int index) {
    return createResult(getResultsDiv(), getByForResult(index)).get();
  }

  public List<SR> getResults() {
    List<SR> results = new ArrayList<SR>();

    if (isResultsAvailable()) {
      By items = By.xpath("//div[@class='itemresult-wrapper']");
      waiter.until(ExpectedConditions.presenceOfAllElementsLocatedBy(items));
      int count = getResultsDiv().findElements(items).size();

      for (int i = 1; i <= count; i++) {
        results.add(getResult(i));
      }
    }
    return results;
  }

  protected abstract SR createResult(SearchContext relativeTo, By by);

  public void setChecked(String name, boolean b) {
    setChecked(name, 1, b);
  }

  public void setSelectionChecked(String name, boolean b) {
    getResultForTitle(name, 1).setChecked(b, true);
  }

  public void setChecked(String title, int index, boolean b) {
    getResultForTitle(title, index).setChecked(b);
  }

  public boolean doesResultExist(String title, int index) {
    return isPresent(getByForResult(title, index));
  }

  public boolean doesResultExist(PrefixedName title) {
    return doesResultExist(title.toString());
  }

  public boolean doesResultExist(String title) {
    boolean found = false;
    int size = getResults().size();

    for (int i = 1; i <= size; i++) {
      found = doesResultExist(title, i);
      if (found) {
        break;
      }
    }
    return found;
  }

  /**
   * Legacy method to check if results are available. It always uses the old UI element to check.
   */
  public boolean isResultsAvailable() {
    return isResultsAvailable(false);
  }

  /**
   * Check if results are available.
   *
   * @param newUi - true to use the new UI element to check.
   */
  public boolean isResultsAvailable(boolean newUi) {
    try {
      if (newUi) {
        waiter.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(@aria-label, 'Search result list item')]")));
      } else {
        waiter.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@class='itemresult-wrapper']")));
      }
      return true;
    } catch (TimeoutException Time) {
      return false;
    }
  }
}
