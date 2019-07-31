package com.tle.webtests.pageobject.cal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractItemList;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchResult;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ActivationListPage
    extends AbstractItemList<ActivationListPage, ActivationSearchResult> {

  @FindBy(id = "searchresults")
  private WebElement resultDiv;

  public ActivationListPage(PageContext context) {
    super(context);
  }

  @Override
  public WebElement getResultsDiv() {
    return resultDiv;
  }

  @Override
  protected ActivationSearchResult createResult(SearchContext relativeTo, By by) {
    return new ActivationSearchResult(this, relativeTo, by);
  }

  private By getActivationBy(String title, String course) {
    String xpath =
        ItemListPage.getXPathForTitle(title)
            + "[."
            + SearchResult.getDetailMatcherXPath("Course", course)
            + ']';
    return By.xpath(xpath);
  }

  public boolean isActive(String title) {
    return getResultForTitle(title, 1).isActive();
  }

  public boolean isActive(String title, String course) {
    return createResult(resultDiv, getActivationBy(title, course)).get().isActive();
  }

  public boolean isShowing(String title, String courseName) {
    return isPresent(getActivationBy(title, courseName));
  }
}
