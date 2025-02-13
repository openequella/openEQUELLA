package io.github.openequella.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.searching.FavouritesPage;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.searching.PowerSearchTest;
import io.github.openequella.pages.advancedsearch.NewAdvancedSearchPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import testng.annotation.NewUIOnly;

@TestInstitution("fiveo")
public class AdvancedSearchPageTest extends AbstractSessionTest {
  private NewAdvancedSearchPage advancedSearchPage;

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  private static final String EDITBOX_VALUE = "An Edit box";

  // Must turn on New Search UI for the favourite integration test case.
  @BeforeClass
  public void enableNewSearchUI() {
    SettingsPage settingsPage = new SettingsPage(context).load();
    settingsPage.setNewSearchUI(true);
  }

  // Must turn off New Search UI in the end so that tests that do not run against NEW Search UI
  // can run properly.
  @AfterClass
  public void disableNewSearchUI() {
    SettingsPage settingsPage = new SettingsPage(context).load();
    settingsPage.setNewSearchUI(false);
  }

  @BeforeMethod
  public void loadAdvancedSearchPage() {
    advancedSearchPage = new NewAdvancedSearchPage(context);
    advancedSearchPage.load();
    advancedSearchPage.selectAdvancedSearch("All Controls");
    advancedSearchPage.waitForSearchCompleted(3);
  }

  @Test(description = "Select an Advanced search")
  @NewUIOnly
  public void accessAdvancedSearch() {
    // Check if the filter icon button is displayed.
    assertNotNull(advancedSearchPage.getAdvancedSearchFilterIcon());
    // Check value of the selector.
    String selected = advancedSearchPage.getSelection();
    assertEquals(selected, "All Controls Power Search");
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

  @Test(description = "Search by using the Calendar control", dataProvider = "calendarTestData")
  @NewUIOnly
  public void calender(String[] dataRange, int expectItemCount, String[] expectedItemNames) {
    advancedSearchPage.selectDateRange(dataRange);
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(expectItemCount);

    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
    assertEquals(actualItemNames, expectedItemNames);
  }

  @Test(description = "Search by using the Checkbox control", dataProvider = "checkboxTestData")
  @NewUIOnly
  public void checkbox(String[] targetOptions, int expectItemCount, String[] expectedItemNames) {
    for (String option : targetOptions) {
      advancedSearchPage.selectCheckbox(option);
    }
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(expectItemCount);

    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
    assertEquals(actualItemNames, expectedItemNames);
  }

  @Test(description = "Search by using the EditBox control", dataProvider = "editBoxTestData")
  @NewUIOnly
  public void editBox(String query, int expectItemCount, String[] expectedItemNames) {
    advancedSearchPage.updateEditBox(query);
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(expectItemCount);

    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
    assertEquals(actualItemNames, expectedItemNames);
  }

  @Test(description = "Search by using the Listbox control")
  @NewUIOnly
  public void listbox() {
    advancedSearchPage.selectListbox("1");
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(1);
    assertEquals(advancedSearchPage.getItemNameByIndex(0), PowerSearchTest.FIRST);
  }

  @Test(description = "Search by using the RadioGroup control", dataProvider = "radioGroupTestData")
  @NewUIOnly
  public void radioGroup(String targetRadio, int expectItemCount, String[] expectedItemNames) {
    advancedSearchPage.selectRadio(targetRadio);
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(expectItemCount);

    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
    assertEquals(actualItemNames, expectedItemNames);
  }

  @Test(description = "Interact with a raw HTML control")
  @NewUIOnly
  public void rawHtml() {
    WebElement rawHtml = getContext().getDriver().findElement(By.id("wiz-7-html"));
    assertEquals(rawHtml.getText(), "test");
  }

  // ShuffleBox can reuse the Checkbox test data.
  @Test(description = "Search by using the ShuffleBox control", dataProvider = "checkboxTestData")
  @NewUIOnly
  public void shuffleBox(String[] targetOptions, int expectItemCount, String[] expectedItemNames) {
    for (String option : targetOptions) {
      advancedSearchPage.selectShuffleBox(option);
    }
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(expectItemCount);

    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
    assertEquals(actualItemNames, expectedItemNames);
  }

  @Test(
      description = "Search by using the ShuffleList control",
      dataProvider = "shuffleListTestData")
  @NewUIOnly
  public void shuffleList(String[] targetQueries, int expectItemCount, String[] expectedItemNames) {
    for (String query : targetQueries) {
      advancedSearchPage.updateShuffleList(query);
    }
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(expectItemCount);

    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
    assertEquals(actualItemNames, expectedItemNames);
  }

  // todo: enable this test case after OEQ-1758 is resolved.
  //  @Test(description = "Search by using the Auto Term control", dataProvider =
  // "autoTermTestData")
  //  @NewUIOnly
  //  public void autoTerm(String[] targetTerms, int expectItemCount, String[] expectedItemNames) {
  //    for(String term : targetTerms) {
  //      advancedSearchPage.selectAutoTerm(term);
  //    }
  //    advancedSearchPage.search();
  //    advancedSearchPage.waitForSearchCompleted(expectItemCount);
  //
  //    String[] actualItemNames = advancedSearchPage.getTopNItemNames(expectItemCount);
  //    assertEquals(actualItemNames, expectedItemNames);
  //  }

  @Test(description = "Search by using the User selector control")
  @NewUIOnly
  public void userSelector() {
    advancedSearchPage.selectUser("AutoTest");
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(1);
    assertEquals(advancedSearchPage.getItemNameByIndex(0), PowerSearchTest.FIRST);
  }

  @Test(description = "Search by using different controls together")
  @NewUIOnly
  public void multipleControls() {
    advancedSearchPage.updateEditBox(EDITBOX_VALUE);
    advancedSearchPage.selectCheckbox("1");
    advancedSearchPage.selectRadio("1");
    advancedSearchPage.selectShuffleBox("1");
    advancedSearchPage.updateShuffleList("1");
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(1);
    assertEquals(advancedSearchPage.getItemNameByIndex(0), PowerSearchTest.FIRST);
  }

  @Test(description = "visibility of controls controlled by scripting")
  @NewUIOnly
  public void script() {
    advancedSearchPage.clearSelection();
    advancedSearchPage.waitForSearchCompleted();
    advancedSearchPage.selectAdvancedSearch("script test");
    advancedSearchPage.waitForSearchCompleted();

    // Only show options for QLD cities if select the state QLD.
    advancedSearchPage.selectRadio("qld");
    WebElement cityTitle =
        getContext().getDriver().findElement(By.xpath("//h6[text()='Qld City']"));
    assertNotNull(cityTitle);

    // Now select state TAS and show TAS cities
    advancedSearchPage.selectRadio("tas");
    cityTitle = getContext().getDriver().findElement(By.xpath("//h6[text()='Tas City']"));
    assertNotNull(cityTitle);
  }

  @Test(description = "Integration with favourite searches")
  @NewUIOnly
  public void favouriteIntegration() {
    String favouriteName = "Favourite advanced search";

    advancedSearchPage.updateEditBox(EDITBOX_VALUE);
    advancedSearchPage.selectCheckbox("1");
    advancedSearchPage.search();
    advancedSearchPage.waitForSearchCompleted(1);
    assertEquals(advancedSearchPage.getItemNameByIndex(0), PowerSearchTest.FIRST);

    advancedSearchPage.addToFavouriteSearch(favouriteName);

    // Access Favorites Search Page
    FavouritesPage favouritePage = new FavouritesPage(context).load();
    assertTrue(favouritePage.searches().results().doesResultExist(favouriteName));
    favouritePage.accessSavedSearches(favouriteName);

    // There should be only 1 item in the search result.
    advancedSearchPage.waitForSearchCompleted(1);
    assertEquals(advancedSearchPage.getItemNameByIndex(0), PowerSearchTest.FIRST);

    // Advanced search panel should be opened with values populated.
    WebElement advancedSearchPanel = advancedSearchPage.getAdvancedSearchPanel();
    WebElement checkedBox =
        advancedSearchPanel.findElement(By.xpath("//input[@type='checkbox' and @value='1']"));
    assertTrue(checkedBox.isSelected());

    WebElement editbox = advancedSearchPanel.findElement(By.id("wiz-3-editbox"));
    assertEquals(editbox.getAttribute("value"), EDITBOX_VALUE);
  }

  @DataProvider
  private Object[][] calendarTestData() {
    return new Object[][] {
      {new String[] {null, "2011-04-01"}, 0, new String[] {}},
      {
        new String[] {"2011-04-01", "2011-04-29"},
        2,
        new String[] {PowerSearchTest.FIRST, PowerSearchTest.SECOND}
      },
      {new String[] {"2011-04-29", null}, 1, new String[] {PowerSearchTest.SECOND}},
    };
  }

  @DataProvider
  private Object[][] editBoxTestData() {
    return new Object[][] {
      {EDITBOX_VALUE, 1, new String[] {PowerSearchTest.FIRST}},
      {"Something else", 1, new String[] {PowerSearchTest.SECOND}},
      // Todo: Enable below test case after OEQ-1755 is resolved.
      // {"*", 3, new String[]{PowerSearchTest.FIRST, PowerSearchTest.SECOND,
      // PowerSearchTest.MULTIPLE}}
    };
  }

  @DataProvider
  private Object[][] checkboxTestData() {
    return new Object[][] {
      {new String[] {"1"}, 2, new String[] {PowerSearchTest.FIRST, PowerSearchTest.MULTIPLE}},
      {new String[] {"2"}, 2, new String[] {PowerSearchTest.SECOND, PowerSearchTest.MULTIPLE}},
      {
        new String[] {"1", "2"},
        3,
        new String[] {PowerSearchTest.FIRST, PowerSearchTest.SECOND, PowerSearchTest.MULTIPLE}
      },
    };
  }

  @DataProvider
  private Object[][] radioGroupTestData() {
    return new Object[][] {
      {"1", 1, new String[] {PowerSearchTest.FIRST}},
      {"2", 1, new String[] {PowerSearchTest.SECOND}},
    };
  }

  @DataProvider
  private Object[][] shuffleListTestData() {
    return new Object[][] {
      {new String[] {"1"}, 1, new String[] {PowerSearchTest.FIRST}},
      {new String[] {"2"}, 1, new String[] {PowerSearchTest.SECOND}},
      {new String[] {"1", "2"}, 2, new String[] {PowerSearchTest.FIRST, PowerSearchTest.SECOND}},
    };
  }

  @DataProvider
  private Object[][] autoTermTestData() {
    return new Object[][] {
      {new String[] {"term"}, 2, new String[] {PowerSearchTest.FIRST, PowerSearchTest.MULTIPLE}},
      {new String[] {"term 2"}, 1, new String[] {PowerSearchTest.MULTIPLE}},
    };
  }
}
