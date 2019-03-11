package com.tle.webtests.test.searching;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchUrlSupportedParamsPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SearchUrlSupportedParamsTest extends AbstractSessionTest {
  private SearchUrlSupportedParamsPage page;

  @Override
  protected void prepareBrowserSession() {
    logon("AutoTest", "automated");
    page = new SearchUrlSupportedParamsPage(context).load();
  }

  @DataProvider(name = "params")
  public Object[][] params() {
    return new Object[][] {
      {"doc"}, {"in"}, {"q"}, {"sort"}, {"rs"}, {"owner"}, {"dp"}, {"ds"}, {"dr"}, {"mt"}
    };
  }

  @Test(dataProvider = "params")
  public void testSupportedParameters(String param) {
    context.getDriver();
    assertTrue(page.hasParameter(param));
  }
}
