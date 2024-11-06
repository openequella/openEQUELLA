package com.tle.webtests.pageobject.remoterepo.sru;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.remoterepo.AbstractRemoteRepoSearchPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import java.time.Duration;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RemoteRepoSRUSearchPage
    extends AbstractRemoteRepoSearchPage<
        RemoteRepoSRUSearchPage, RemoteRepoListPage, RemoteRepoSearchResult> {
  @FindBy(id = "searchform-search")
  private WebElement searchButton;

  @FindBy(className = "searchError")
  private WebElement errorDiv;

  @FindBy(id = "searchresults")
  private WebElement resultsAjaxDiv;

  @FindBy(xpath = "id('searchform')/h2[text()='Searching SRU']")
  private WebElement mainElem;

  public RemoteRepoSRUSearchPage(PageContext context) {
    super(context);
    this.waiter =
        new WebDriverWait(context.getDriver(), Duration.ofMinutes(1), Duration.ofMillis(50));
  }

  @Override
  protected WebElement findLoadedElement() {
    return mainElem;
  }

  @Override
  public RemoteRepoListPage resultsPageObject() {
    return new RemoteRepoListPage(context);
  }

  public void searchErrorOnPage() {
    WaitingPageObject<RemoteRepoSRUSearchPage> waiter = ajaxUpdateExpect(resultsAjaxDiv, errorDiv);
    searchButton.click();
    waiter.get();
  }

  public RemoteRepoSRUSearchPage blankQuery() {
    setQuery("");
    searchButton.click();
    driver.switchTo().alert().accept();
    return this;
  }
}
