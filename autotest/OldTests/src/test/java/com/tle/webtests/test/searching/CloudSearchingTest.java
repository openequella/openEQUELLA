package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.searching.cloud.CloudSearchPage;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

/** @author Aaron */
@TestInstitution("fiveo")
public class CloudSearchingTest extends AbstractCleanupTest {
  @Name("new retro wave")
  private static PrefixedName SEARCH_QUERY;

  @Test
  public void testLocalQueryTransferredToCloudQuery() {
    logon("AutoTest", "automated");
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.search(SEARCH_QUERY);
    CloudSearchPage cloudSearch =
        searchPage.getSearchTabs().get().clickTab("cloud", new CloudSearchPage(context));
    assertEquals(
        cloudSearch.getQuery(),
        SEARCH_QUERY.toString(),
        "Query wasn't maintained in cloud tab switch");
  }
}
