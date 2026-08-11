package com.tle.webtests.pageobject.remoterepo.z3950;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.AbstractRemoteRepoSearchPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import com.tle.webtests.pageobject.searching.AbstractQuerySection;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RemoteRepoAdvancedZ3950SearchPage
    extends AbstractRemoteRepoSearchPage<
        RemoteRepoAdvancedZ3950SearchPage, RemoteRepoListPage, RemoteRepoSearchResult> {
  // FIXME turrible loadedBy
  @FindBy(xpath = "id('searchform')/h2[contains(text(),'Searching Z3950 (')]")
  private WebElement mainElem;

  private RemoteRepoAdvancedZ3950QuerySection z3950querySection;

  public RemoteRepoAdvancedZ3950SearchPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return mainElem;
  }

  @Override
  protected AbstractQuerySection<?> createQuerySection() {
    z3950querySection = new RemoteRepoAdvancedZ3950QuerySection(context);
    return z3950querySection;
  }

  public RemoteRepoListPage exactQuery(
      String s1, String q1, String o1, String s2, String q2, String o2, String s3, String q3) {
    z3950querySection.get().setAvancedQuery(s1, q1, o1, s2, q2, o2, s3, q3);
    return querySection.search(resultsPageObject.getUpdateWaiter());
  }

  @Override
  public RemoteRepoListPage resultsPageObject() {
    return new RemoteRepoListPage(context);
  }
}
