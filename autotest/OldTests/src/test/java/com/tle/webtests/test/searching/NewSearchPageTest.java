package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.NewSearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class NewSearchPageTest extends AbstractCleanupAutoTest {
  private NewSearchPage searchPage;

  @BeforeMethod(description = "open the new Search page and wait for initial search completed")
  public void openSearchPage() {
    searchPage = new NewSearchPage(context).load();
    // The initial search should return 10 items.
    searchPage.getWaiter().until((driver) -> searchPage.getSearchResultItems().size() == 10);
  }

  @Test(description = "open an item's summary page")
  public void openItemSummaryPage() {
    final String ITEM_TITLE = "ExclamationTest ! Crazy! Horse!";
    WebElement titleLink = searchPage.getItemTitleLink(ITEM_TITLE);
    titleLink.click();
    SummaryPage summary = new SummaryPage(context).get();
    assertEquals(summary.getItemTitle(), ITEM_TITLE);
  }
}
