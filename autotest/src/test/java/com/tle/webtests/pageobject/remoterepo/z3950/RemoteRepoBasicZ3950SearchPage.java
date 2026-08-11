package com.tle.webtests.pageobject.remoterepo.z3950;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.AbstractRemoteRepoSearchPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RemoteRepoBasicZ3950SearchPage
    extends AbstractRemoteRepoSearchPage<
        RemoteRepoBasicZ3950SearchPage, RemoteRepoListPage, RemoteRepoSearchResult> {
  @FindBy(xpath = "id('searchform')/h2[text()='Searching Z3950 (Basic)']")
  private WebElement mainElem;

  public RemoteRepoBasicZ3950SearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return mainElem;
  }

  @Override
  public RemoteRepoListPage resultsPageObject() {
    return new RemoteRepoListPage(context);
  }
}
