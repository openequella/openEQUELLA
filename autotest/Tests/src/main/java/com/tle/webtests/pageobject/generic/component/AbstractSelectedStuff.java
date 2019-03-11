package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractSelectedStuff<T, P extends AbstractSelectedStuff<T, P>>
    extends AbstractPage<P> {
  private static final String TABLE_XPATH = "table[contains(@class, 'selections')]";
  private static final String TBODY_XPATH =
      TABLE_XPATH + "[not(contains(@class, 'no-selections'))]/tbody";
  private static final String TD_NAME_XPATH = "tr/td[contains(@class, 'name')]";

  // Find the following element, whatever it is, and only then check it's an
  // anchor tag. local-name() returns tag name of first match only.
  private static final By ADD_ACTION_XPATH =
      By.xpath(TABLE_XPATH + "/following-sibling::*[local-name()='a']");

  private final By parent;

  public AbstractSelectedStuff(PageContext context, By parentElement) {
    super(context);
    this.parent = parentElement;
  }

  @Override
  protected void checkLoadedElement() {
    // Do nothing!
  }

  /**
   * There is no guarantee that this exists, so callees should ensure they check for
   * NotFoundException if the method is expected to be used in a "check if a selection has been
   * made" situation. For example, "clickAction" doesn't check because the user can check first
   * using the safe "getSelections" method.
   */
  protected WebElement getTableBody() {
    return driver.findElement(new ByChained(parent, By.xpath(TBODY_XPATH)));
  }

  public int getSelectionCount() {
    try {
      return getTableBody().findElements(By.tagName("tr")).size();
    } catch (NotFoundException ex) {
      return 0;
    }
  }

  public List<T> getSelections() {
    List<T> rv = new ArrayList<T>();

    WebElement tableBody = null;
    try {
      tableBody = getTableBody();
    } catch (NotFoundException ex) {
      // Ignore - return the empty list
      return rv;
    }

    for (WebElement we : tableBody.findElements(By.xpath(TD_NAME_XPATH))) {
      rv.add(getSelection(we));
    }

    return rv;
  }

  protected abstract T getSelection(WebElement we);

  public boolean hasAddAction() {
    return isPresent(ADD_ACTION_XPATH);
  }

  public void clickAddAction() {
    driver.findElement(new ByChained(parent, ADD_ACTION_XPATH)).click();
  }

  public <PO extends PageObject> PO clickAction(
      String selection, String action, WaitingPageObject<PO> returnTo) {
    return clickActionWithConfirm(selection, action, null, returnTo);
  }

  public <PO extends PageObject> PO clickActionWithConfirm(
      String selection, String action, Boolean confirm, WaitingPageObject<PO> returnTo) {

    getTableBody()
        .findElement(
            By.xpath(
                TD_NAME_XPATH
                    + getAdditionalNameXpathConstraint(selection)
                    + "/following-sibling::td/a[@title = "
                    + quoteXPath(action)
                    + "]"))
        .click();
    if (confirm != null) {
      if (confirm) {
        acceptConfirmation();
      } else {
        cancelConfirmation();
      }
    }
    return returnTo.get();
  }

  protected abstract String getAdditionalNameXpathConstraint(String selection);

  public WaitingPageObject<P> selectionWaiter(String selection) {
    return ExpectWaiter.waiter(
        ExpectedConditions.visibilityOfElementLocated(
            new ByChained(parent, getByForSelection(selection))),
        this);
  }

  public WaitingPageObject<P> removalWaiter(String selection) {
    return ExpectWaiter.waiter(
        ExpectedConditions.invisibilityOfElementLocated(
            new ByChained(parent, getByForSelection(selection))),
        this);
  }

  private By getByForSelection(String selection) {
    return By.xpath(
        TBODY_XPATH + '/' + TD_NAME_XPATH + getAdditionalNameXpathConstraint(selection));
  }
}
