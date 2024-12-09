package com.tle.webtests.remotetest.contribute.remoterepo;

import static org.testng.Assert.assertTrue;

import com.google.common.primitives.Ints;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoViewResultPage;
import com.tle.webtests.pageobject.remoterepo.z3950.RemoteRepoAdvancedZ3950SearchPage;
import com.tle.webtests.pageobject.remoterepo.z3950.RemoteRepoBasicZ3950SearchPage;
import com.tle.webtests.pageobject.remoterepo.z3950.RemoteRepoViewZ3950ResultPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class RemoteRepoZ3950Test extends AbstractCleanupAutoTest {
  private static final String Z3950_BASIC = "Z3950 (Basic)";
  private static final String Z3950_ADVANCED = "Z3950 (Advanced)";
  private static final String Z3950_BATH_0 = "Z3950 (Bath 0)";
  private static final int MAX_PAGES = 5;

  @Test
  public void testZ3950BasicSearchAndContribute() {
    SearchPage sp = new SearchPage(context).load();
    RemoteRepoPage rrp = sp.searchOtherRepositories();

    assertTrue(rrp.isRemoteRepositoryVisible(Z3950_BASIC));

    RemoteRepoBasicZ3950SearchPage rrZ3950 =
        rrp.clickRemoteRepository(Z3950_BASIC, new RemoteRepoBasicZ3950SearchPage(context));
    String resultTitle = "Linux for dummies /";
    String query = "Linux for dummies";
    RemoteRepoListPage searchResults = rrZ3950.search(query);

    assertTrue(searchResults.doesResultExist(resultTitle, 1));

    RemoteRepoSearchResult sr = searchResults.getResultForTitle(resultTitle, 1);
    RemoteRepoViewZ3950ResultPage z3950ViewResult =
        sr.viewResult(new RemoteRepoViewZ3950ResultPage(context));

    WizardPageTab contribution = z3950ViewResult.importResult();
    String text = contribution.getControl(1).getAttribute("value");
    contribution.editbox(1, "RemoteRepoZ3950Test - " + text);
    contribution.save().publish();
  }

  @Test
  public void testBathZeroAttributes() {
    SearchPage sp = new SearchPage(context).load();
    RemoteRepoPage rrp = sp.searchOtherRepositories();

    assertTrue(rrp.isRemoteRepositoryVisible(Z3950_BATH_0));

    RemoteRepoAdvancedZ3950SearchPage rrZ3950 =
        rrp.clickRemoteRepository(Z3950_BATH_0, new RemoteRepoAdvancedZ3950SearchPage(context));

    RemoteRepoListPage searchResults =
        rrZ3950.exactQuery(
            "Any field [Keyword]",
            "kanye west",
            "AND",
            "Title [Keyword]",
            "dark twisted fantasy",
            "AND",
            "Author [Precision Match]",
            "Kirk Walker Graves");

    String resultTitle = "My beautiful dark twisted fantasy /";
    assertTrue(searchResults.doesResultExist(resultTitle, 1));
  }

  @Test
  public void testZ3950AdvancedSearchAndContribute() {
    SearchPage sp = new SearchPage(context).load();
    RemoteRepoPage rrp = sp.searchOtherRepositories();

    assertTrue(rrp.isRemoteRepositoryVisible(Z3950_ADVANCED));

    RemoteRepoAdvancedZ3950SearchPage rrZ3950 =
        rrp.clickRemoteRepository(Z3950_ADVANCED, new RemoteRepoAdvancedZ3950SearchPage(context));

    RemoteRepoListPage searchResults =
        rrZ3950.exactQuery("Title", "Ubuntu", "AND", "Title", "Linux", "AND", "Title", "Fedora");

    String resultTitle =
        "Linux bibleboot up to Ubuntu, Fedora, KNOPPIX, Debian, openSUSE, and 13 other"
            + " distributions /";
    assertTrue(searchResults.doesResultExist(resultTitle, 1));

    RemoteRepoSearchResult sr = searchResults.getResultForTitle(resultTitle, 1);
    RemoteRepoViewResultPage z3950ViewResult =
        sr.viewResult(new RemoteRepoViewZ3950ResultPage(context));

    WizardPageTab contribution = z3950ViewResult.importResult();
    String text = contribution.getControl(1).getAttribute("value");
    contribution.editbox(1, "RemoteRepoZ3950Test - " + text);
    contribution.save().publish();
  }

  @Test
  public void testZ3950Paging() {

    // Via contribute
    MenuSection ms = new MenuSection(context).get();
    ContributePage contribPage = ms.clickMenu("Contribute", new ContributePage(context));

    assertTrue(contribPage.hasRemoteRepo(Z3950_BASIC));

    RemoteRepoBasicZ3950SearchPage rrZ3950 =
        contribPage.openRemoteRepo(Z3950_BASIC, new RemoteRepoBasicZ3950SearchPage(context));
    rrZ3950.search("Test");

    if (rrZ3950.hasPaging()) {
      String stats = rrZ3950.getStats();
      String[] split = stats.split(" ");
      int perPage = (Integer.parseInt(split[0]) + Integer.parseInt(split[2])) - 1;
      int total = Integer.parseInt(split[4]);
      int currentPage = rrZ3950.getCurrentPage();
      int pages = total / perPage;

      for (int i = currentPage + 1; i <= Ints.min(pages, MAX_PAGES); i++) {
        rrZ3950.clickPaging(i);
        assertTrue(rrZ3950.onPage(i));
      }
    }
  }

  @Test
  public void testZ3950Breadcrumbs() {
    ContributePage contribPage = new ContributePage(context).load();
    RemoteRepoBasicZ3950SearchPage rrZ3950 =
        contribPage.openRemoteRepo(Z3950_BASIC, new RemoteRepoBasicZ3950SearchPage(context));

    String resultTitle = "Linux for dummies /";
    String query = "Linux for dummies";
    RemoteRepoListPage searchResults = rrZ3950.search(query);
    assertTrue(searchResults.doesResultExist(resultTitle, 1));

    RemoteRepoSearchResult sr = searchResults.getResultForTitle(resultTitle, 1);
    RemoteRepoViewZ3950ResultPage z3950ViewResult =
        sr.viewResult(new RemoteRepoViewZ3950ResultPage(context));

    rrZ3950 = z3950ViewResult.clickBasicRemoteRepoBreadcrumb(Z3950_BASIC);
    assertTrue(rrZ3950.results().doesResultExist(resultTitle, 1));

    contribPage = rrZ3950.clickContributeBreadcrumb();
    assertTrue(contribPage.hasRemoteRepo(Z3950_BASIC));
  }
}
