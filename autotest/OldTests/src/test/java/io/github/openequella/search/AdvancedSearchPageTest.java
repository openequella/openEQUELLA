package io.github.openequella.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractSessionTest;
import io.github.openequella.pages.advancedsearch.NewAdvancedSearchPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import testng.annotation.NewUIOnly;

@TestInstitution("fiveo")
public class AdvancedSearchPageTest extends AbstractSessionTest {
  private NewAdvancedSearchPage advancedSearchPage;

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @BeforeMethod
  public void loadAdvancedSearchPage() {
    advancedSearchPage = new NewAdvancedSearchPage(context);
    advancedSearchPage.load();
    advancedSearchPage.selectAdvancedSearch("DRM");
    advancedSearchPage.waitForSearchCompleted(1);
  }

  @Test(description = "Select an Advanced search")
  @NewUIOnly
  public void accessAdvancedSearch() {
    // Check if the filter icon button is displayed.
    assertNotNull(advancedSearchPage.getAdvancedSearchFilterIcon());
    // Check value of the selector.
    String selected = advancedSearchPage.getSelection();
    assertEquals(selected, "DRM Party search");
  }

  @Test(description = "Exit Advanced search mode")
  @NewUIOnly
  public void exitAdvancedSearch() {
    advancedSearchPage.clearSelection();
    // Check if the filter icon button disappears.
    assertNull(advancedSearchPage.getAdvancedSearchFilterIcon());
    // The selector's value should be an empty string now.
    String selected = advancedSearchPage.getSelection();
    assertEquals(selected, "");
  }

  @Test(description = "Close and open Advanced search panel")
  @NewUIOnly
  public void toggleAdvancedSearchPanel() {
    // Opened initially.
    assertNotNull(advancedSearchPage.getAdvancedSearchPanel());
    // Close the panel.
    advancedSearchPage.closeAdvancedSearchPanel();
    assertNull(advancedSearchPage.getAdvancedSearchPanel());
    // Open again.
    advancedSearchPage.openAdvancedSearchPanel();
    assertNotNull(advancedSearchPage.getAdvancedSearchPanel());
  }
}
