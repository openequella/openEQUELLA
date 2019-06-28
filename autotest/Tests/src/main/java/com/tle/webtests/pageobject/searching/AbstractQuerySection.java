package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class AbstractQuerySection<T extends AbstractQuerySection<T>>
    extends AbstractPage<T> {
  @FindBy(name = "q")
  protected WebElement queryField;

  @FindBy(id = "rf_resetButton")
  private WebElement clearFilters;

  private AutoCompleteOptions autoCompleteOptions;

  public AbstractQuerySection(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return queryField;
  }

  @Override
  public void checkLoaded() {
    super.checkLoaded();
    autoCompleteOptions = new AutoCompleteOptions(this);
  }

  public <P extends PageObject> P search(WaitingPageObject<P> resultsUpdate) {
    getSearchButton().click();
    return resultsUpdate.get();
  }

  protected abstract WebElement getSearchButton();

  public void setQuery(String query) {
    setQuery(query, true);
  }

  // Default is to clear. Only SearchAutocompleteTest needs it
  public void setQuery(String query, boolean clear) {
    scrollToElement(queryField);
    queryField.click();
    queryField.clear();
    queryField.sendKeys(query);

    if (clear) {
      queryField.sendKeys(Keys.ESCAPE);
    }
  }

  public <P extends PageObject> P search(String query, WaitingPageObject<P> resultsUpdate) {
    setQuery(query);
    return search(resultsUpdate);
  }

  public String getQueryText() {
    return queryField.getAttribute("value");
  }

  public T clearFilters() {
    if (isPresent(clearFilters)) {
      clearFilters.click();
    }
    return get();
  }

  public AutoCompleteOptions autoCompleteOptions(String partial, String expected) {
    return autoCompleteOptions.partialQuery(partial, expected);
  }

  public void typeKeys(Keys keys) {
    queryField.sendKeys(keys);
  }
}
