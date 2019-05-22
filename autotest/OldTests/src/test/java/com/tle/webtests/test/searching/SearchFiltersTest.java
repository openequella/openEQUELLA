package com.tle.webtests.test.searching;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.FilterByDateSectionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SearchFiltersTest extends AbstractCleanupAutoTest {
  @DataProvider(name = "datefilters", parallel = false)
  public Object[][] datefilters() {
    // @formatter:off
    return new Object[][] {
      {"AFTER", "2011-03-16 00:00:00", "SearchFilters - Basic Item"},
      {"BEFORE", "2011-03-10 17:19:55", "SearchSettings - Image 2 - PNG"},
      {"BETWEEN", "2011-03-12 17:20:29", "SearchSettings - Image 4 - GIF"},
      {"ON", "2011-03-15 12:34:38", "SearchSettings - Image 5 - TIFF"}
    };
    // @formatter:on
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @Test(enabled = false)
  public void testFilterByOwner() {
    SearchPage sp = new SearchPage(context).load();
    String owner = "DoNotUse";
    sp.setOwnerFilter(owner);
    assertTrue(sp.isOwnerSelected(owner));

    ItemListPage search = sp.search();
    assertTrue(search.getResults().size() == 1);
    assertTrue(search.doesResultExist("SearchFilters - Basic Item", 1));
    sp.clearOwnerFilter();
    logon("AutoTest", "automated");
  }

  @Test(
      enabled = false,
      dataProvider = "datefilters",
      dependsOnMethods = {"testFilterByOwner"})
  public void testFilterByDateModified(String range, String date1, String result)
      throws ParseException {
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final TimeZone UTC = TimeZone.getTimeZone("Australia/Hobart");
    dfm.setTimeZone(UTC);

    // 'Conceptual' date!
    final TimeZone LTZ = TimeZone.getTimeZone("America/Chicago");
    Calendar cal1 = Calendar.getInstance(LTZ);
    Date d1 = dfm.parse(date1);
    cal1.setTime(d1);

    if (!range.equals("ON")) {
      if (!range.equals("BEFORE")) {
        cal1.add(Calendar.DAY_OF_MONTH, -1);
      } else {
        cal1.add(Calendar.DAY_OF_MONTH, 1);
      }
    }
    Calendar cal2 = null;
    if (range.equals("BETWEEN")) {
      cal2 = (Calendar) cal1.clone();
      cal2.add(Calendar.DAY_OF_MONTH, 2);
    }

    SearchPage sp = new SearchPage(context).load();

    sp.setDateFilter(range, new Calendar[] {cal1, cal2});
    ItemListPage search = SearchPage.searchExact(context, result);

    assertTrue(search.doesResultExist(result, 1), "Result 1 does not exist");

    // make sure the presented date in the picker hasn't changed
    FilterByDateSectionPage dateFilter = sp.getDateFilter();
    assertTrue(dateFilter.getStartDate().dateEquals(cal1), "Reflected start date does not match");
    assertTrue(dateFilter.getEndDate().dateEquals(cal2), "Reflected end date does not match");
  }
}
