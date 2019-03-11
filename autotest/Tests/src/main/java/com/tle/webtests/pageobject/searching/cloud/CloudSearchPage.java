package com.tle.webtests.pageobject.searching.cloud;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** @author Aaron */
public class CloudSearchPage
    extends AbstractQueryableSearchPage<CloudSearchPage, CloudResultList, CloudSearchResult> {
  // Look for cloud icon on search query... weak I know
  @FindBy(className = "glyphicon-cloud")
  private WebElement mainElem;

  public CloudSearchPage(PageContext context) {
    super(context);
  }

  @Override
  public CloudResultList resultsPageObject() {
    return new CloudResultList(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return mainElem;
  }
}
