package com.tle.webtests.test.searching.manage;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.WhereQueryPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=14778 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14779 Preconditions: that there exists a collection
 * titled 'Generic testing collection' which defines items with a date filed (in this case in
 * metadata path /xml/calendar, where at least one item exists with a data value as 1962-10-30 and
 * another with 2011-09-26. It shall be assumed no items exist with dates outside the range of these
 * two dates. The queries in this test will seek for values less than, greater than or equal to
 * these dates.
 *
 * @author larry
 */
@TestInstitution("manageresources")
public class ManageResourcesSearchTest extends AbstractCleanupAutoTest {
  private static final String DATE_METADATA_PATH = "/xml/calendar";

  //	private final static String OLDEST_DATE_STR = "1962-10-30"; // Date somebody arrived on earth.
  //	private final static String NEWEST_DATE_STR = "2011-09-26"; // Date somebody else left the
  // building.

  @Test
  public void aboveAndBelowDatesTest() {
    // Having loaded the page, click "Manage resources"
    ItemAdminPage resultsPage = new ItemAdminPage(context).load();
    // Ensure that we are looking at a collection to search from (editable
    // search not applicable to "All resources" for example)
    resultsPage.setWithinCollection(GENERIC_TESTING_COLLECTION);

    WhereQueryPage<ItemAdminPage> whereQueryPage = resultsPage.editWhere();

    String newerThanNewest = "2011-10-01";
    int howMany = howManyBeyondLimit(whereQueryPage, ">", newerThanNewest);
    assertTrue(
        howMany == 0,
        "Expected no items to store date newer than '"
            + newerThanNewest
            + "' but found "
            + howMany);

    // find the 'Edit query' link again and click
    whereQueryPage = resultsPage.editWhere();
    whereQueryPage.clearCriteria();
    howMany = howManyBeyondLimit(whereQueryPage, "<", newerThanNewest);
    assertTrue(
        howMany >= 2,
        "Expected at least 2 items to store date older than '"
            + newerThanNewest
            + "' but found "
            + howMany);

    // find the 'Edit query' link again and click
    whereQueryPage = resultsPage.editWhere();
    whereQueryPage.clearCriteria();
    String olderThanOldest = "1960-12-31";
    howMany = howManyBeyondLimit(whereQueryPage, "<", olderThanOldest);
    assertTrue(
        howMany == 0,
        "Expected no items to store date older than '"
            + olderThanOldest
            + "' but found "
            + howMany);

    // find the 'Edit query' link again and click
    whereQueryPage = resultsPage.editWhere();
    whereQueryPage.clearCriteria();
    howMany = howManyBeyondLimit(whereQueryPage, ">", olderThanOldest);
    assertTrue(
        howMany >= 2,
        "Expected at least 2 items to store date newer than '"
            + olderThanOldest
            + "' but found "
            + howMany);
  }

  private int howManyBeyondLimit(
      WhereQueryPage<ItemAdminPage> whereQueryPage, String operand, String dateStr) {
    whereQueryPage.setPredicate("WHERE");
    // enter the metadata path for the date value
    whereQueryPage.setWherePath(DATE_METADATA_PATH);
    // specify entered date is 'greater than'
    whereQueryPage.setWhereOperand(operand);
    // enter a date known to be greater than at least 2 known elements
    whereQueryPage.setWhereValue(dateStr);
    // having constructed a where clause, we add it to the query and
    // now we can execute the search
    return whereQueryPage.executeCriterion().results().getResults().size();
  }
}
