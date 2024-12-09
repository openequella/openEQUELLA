package com.tle.webtests.test.workflow;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.AbstractItemList;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.List;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class TasksSortingAndFilteringTest extends AbstractCleanupTest {
  // @formatter:off
  private static final String PREFIX = "WorkFlowSortingAndFilteringTest";
  private static final String Y3 = PREFIX + " - 3 Step - A Little Bit Old"; // Review
  private static final String M3 = PREFIX + " - 3 Step - Sort Of Old"; // Rejected
  private static final String O3 = PREFIX + " - 3 Step - Extremely Old"; // Moderating
  private static final String Y2 = PREFIX + " - 2 Step - A Little Bit Old"; // Moderating
  private static final String M2 = PREFIX + " - 2 Step - Sort Of Old"; // Review, small timeinmod
  private static final String O2 = PREFIX + " - 2 Step - Extremely Old"; // Rejected

  private static final String AM = PREFIX + " - Assigned to me";
  private static final String AN = PREFIX + " - Assigned to no one";
  private static final String AO = PREFIX + " - Assigned to other";

  private static final String MMAM = PREFIX + " - Mustmod assigned";
  private static final String MMAN = PREFIX + " - Mustmod, unassigned";

  // @formatter:on

  @Test
  public void workflowSortingAndFilteringTest() {
    // Wish I didn't have so many test items...
    String[] titles = {Y3, M3, O3, Y2, M2, O2};
    boolean[] expected;
    int[] expectedTitles;

    logon("admin", "``````");

    ItemAdminPage filterListPage = new ItemAdminPage(context).load();
    ItemListPage filterResults = filterListPage.all().exactQuery(PREFIX);

    // Filters
    expected = new boolean[] {true, true, true, true, true, true};
    assertTrue(
        checkForItems(titles, expected, filterResults, filterListPage),
        "Expected different filter results");

    filterListPage.setModOnly(true);
    expected = new boolean[] {true, false, true, true, true, false};
    assertTrue(
        checkForItems(titles, expected, filterResults, filterListPage),
        "Expected different filter results");

    filterListPage.filterByStatus("review");
    expected = new boolean[] {true, false, false, false, true, false};
    assertTrue(
        checkForItems(titles, expected, filterResults, filterListPage),
        "Expected different filter results");

    filterListPage.filterByStatus("");
    filterListPage.setModOnly(false);

    // Time in moderation
    boolean temp = false;
    try {
      filterListPage.setSort("timeinmod");
    } catch (NoSuchElementException e) {
      temp = true;
    }
    assertTrue(temp, "Sort by time in moderation showed when it shouldn't");

    filterListPage.setModOnly(true);
    filterListPage.setSort("timeinmod");
    expectedTitles = new int[] {2, 0, 3, 4};
    assertTrue(
        checkOrder(titles, expectedTitles, filterListPage.results().getResults()),
        "Sorted order was wrong");

    filterListPage.setModOnly(false);
    temp = false;
    try {
      filterListPage.filterByWorkflow("3 Step Workflow");
    } catch (NoSuchElementException e) {
      temp = true;
    }
    assertTrue(temp, "Filter by workflow showed when it shouldn't");

    filterListPage.setModOnly(true);
    filterListPage.setWithinCollection("Simple 3 Step");

    filterListPage.filterByWorkflow("3 Step Workflow");

    filterListPage.setWithinAll();
    expected = new boolean[] {true, false, true, false, false, false};
    assertTrue(
        checkForItems(titles, expected, filterResults, filterListPage),
        "Expected different filter results");

    filterListPage.clearFilters();
    expected = new boolean[] {true, true, true, true, true, true};
    assertTrue(
        checkForItems(titles, expected, filterResults, filterListPage),
        "Expected different filter results");

    filterListPage.filterByStatus("review");
    filterListPage.setWithinCollection("Simple 3 Step");
    filterListPage.setSort("name");
    expected = new boolean[] {true, false, false, false, false, false};
    assertTrue(
        checkForItems(titles, expected, filterResults, filterListPage),
        "Expected different filter results");
  }

  @Test
  public void tasksListPageSortingAndFilteringTest() {
    String[] titles = {AM, AN, AO, MMAN, MMAM};

    logon("admin", "``````");

    TaskListPage tlp = new TaskListPage(context).load();
    ModerateListSearchResults results = tlp.exactQuery(PREFIX);
    boolean[] expected = new boolean[] {true, true, true, true, true};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.setAssignmentFilter("ME");
    expected = new boolean[] {true, false, false, false, true};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.setAssignmentFilter("OTHERS");
    expected = new boolean[] {false, false, true, false, false};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.setAssignmentFilter("NOONE");
    expected = new boolean[] {false, true, false, true, false};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.setMustMod(true);
    expected = new boolean[] {false, false, false, true, false};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.setAssignmentFilter("ME");
    expected = new boolean[] {false, false, false, false, true};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.setAssignmentFilter("ANY");
    expected = new boolean[] {false, false, false, true, true};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");

    tlp.clearFilters();
    expected = new boolean[] {true, true, true, true, true};
    assertTrue(checkForItems(titles, expected, results, tlp), "Expected different filter results");
  }

  // Slightly neater than the alternative (Still a horrible mess)
  private boolean checkForItems(
      String[] arr,
      boolean[] expected,
      AbstractItemList<?, ?> list,
      AbstractQueryableSearchPage<?, ?, ?> page) {
    for (int i = 0; i < arr.length; i++) {
      if (list.doesResultExist(arr[i]) != expected[i]) {
        page.clickPaging(2);
        if (list.doesResultExist(arr[i]) != expected[i]) {
          return false;
        }
        page.clickPaging(1);
      }
    }
    return true;
  }

  private boolean checkOrder(String[] expected, int[] expectedTitles, List<ItemSearchResult> list) {
    int j = 0;

    for (int i = 0; i < list.size() && j < expectedTitles.length; i++) {
      if (list.get(i).getTitle().equalsIgnoreCase(expected[expectedTitles[j]])) {
        j++;
      }
    }
    return j == expectedTitles.length;
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }
}
