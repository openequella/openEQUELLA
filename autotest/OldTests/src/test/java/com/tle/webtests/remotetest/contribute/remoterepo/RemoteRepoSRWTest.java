package com.tle.webtests.remotetest.contribute.remoterepo;

import static org.testng.Assert.assertTrue;

import com.google.common.primitives.Ints;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import com.tle.webtests.pageobject.remoterepo.srw.RemoteRepoSRWSearchPage;
import com.tle.webtests.pageobject.remoterepo.srw.RemoteRepoViewSRWResultPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class RemoteRepoSRWTest extends AbstractCleanupAutoTest {
  private static final String SRW = "SRW";
  private static final int MAX_PAGES = 5;

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @Test
  public void testSRWSearchAndContribute() {
    // Via search
    SearchPage sp = new SearchPage(context).load();
    RemoteRepoPage rrp = sp.searchOtherRepositories();

    assertTrue(rrp.isRemoteRepositoryVisible(SRW));

    RemoteRepoSRWSearchPage rrSRW =
        rrp.clickRemoteRepository(SRW, new RemoteRepoSRWSearchPage(context));
    String query = "Java programming for dummies /";
    RemoteRepoListPage searchResults = rrSRW.exactQuery(query);

    assertTrue(searchResults.doesResultExist(query, 1));

    RemoteRepoSearchResult sr = searchResults.getResultForTitle(query, 1);
    RemoteRepoViewSRWResultPage srwViewResult =
        sr.viewResult(new RemoteRepoViewSRWResultPage(context));

    WizardPageTab contribution = srwViewResult.importResult();
    String text = contribution.getControl(1).getAttribute("value");
    contribution.editbox(1, "RemoteRepoSRWTest - " + text);
    contribution.save().publish();
  }

  @Test
  public void testSRWPaging() {
    // Via contribute
    MenuSection ms = new MenuSection(context).get();
    ContributePage contribPage = ms.clickMenu("Contribute", new ContributePage(context));

    assertTrue(contribPage.hasRemoteRepo(SRW));

    RemoteRepoSRWSearchPage rrSRW =
        contribPage.openRemoteRepo(SRW, new RemoteRepoSRWSearchPage(context));
    rrSRW.exactQuery("Java programming");

    if (rrSRW.hasPaging()) {
      String stats = rrSRW.getStats();
      String[] split = stats.split(" ");
      int perPage = (Integer.parseInt(split[0]) + Integer.parseInt(split[2])) - 1;
      int total = Integer.parseInt(split[4]);
      int currentPage = rrSRW.getCurrentPage();
      int pages = total / perPage;

      for (int i = currentPage + 1; i <= Ints.min(pages, MAX_PAGES); i++) {
        rrSRW.clickPaging(i);
        assertTrue(rrSRW.onPage(i));
      }
    }
  }

  @Test
  public void testSRWBreadcrumbs() {
    ContributePage contribPage = new ContributePage(context).load();
    RemoteRepoSRWSearchPage rrSRW =
        contribPage.openRemoteRepo(SRW, new RemoteRepoSRWSearchPage(context));

    String query = "Java programming for dummies /";
    RemoteRepoListPage searchResults = rrSRW.exactQuery(query);
    assertTrue(searchResults.doesResultExist(query, 1));

    RemoteRepoSearchResult sr = searchResults.getResultForTitle(query, 1);
    RemoteRepoViewSRWResultPage srwViewResult =
        sr.viewResult(new RemoteRepoViewSRWResultPage(context));

    rrSRW = srwViewResult.clickRemoteRepoBreadcrumb(SRW);
    assertTrue(rrSRW.results().doesResultExist(query, 1));

    contribPage = rrSRW.clickContributeBreadcrumb();
    assertTrue(contribPage.hasRemoteRepo(SRW));
  }
}
