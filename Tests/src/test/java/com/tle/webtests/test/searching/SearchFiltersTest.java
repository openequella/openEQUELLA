package com.tle.webtests.test.searching;

import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.FilterByDateSectionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("fiveo")
public class SearchFiltersTest extends AbstractCleanupAutoTest
{
	@DataProvider(name = "datefilters", parallel = false)
	public Object[][] datefilters()
	{
		//@formatter:off
		return new Object[][]{
			{"AFTER", "2011-03-16 00:00:00", null, "SearchFilters - Basic Item"},
			{"BEFORE", "2011-03-11 00:00:00", null, "SearchSettings - Image 2 - PNG"},
			{"BETWEEN", "2011-03-11 00:00:00", "2011-03-13 00:00:00", "SearchSettings - Image 4 - GIF"},
			{"ON", "2011-03-15 12:34:38", null, "SearchSettings - Image 5 - TIFF"}
		};
		//@formatter:on
	}

	@Override
	protected boolean isCleanupItems()
	{
		return false;
	}

	@Test
	public void testFilterByOwner()
	{
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

	@Test(dataProvider = "datefilters", dependsOnMethods = {"testFilterByOwner"})
	public void testFilterByDateModified(String range, String date1, String date2, String result) throws ParseException
	{
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 'Conceptual' date!
		final TimeZone UTC = TimeZone.getTimeZone("Etc/UTC");
		dfm.setTimeZone(UTC);

		Calendar cal1 = Calendar.getInstance(UTC);
		Date d1 = dfm.parse(date1);
		cal1.setTime(d1);

		Calendar cal2 = null;
		Date d2 = null;
		if( date2 != null )
		{
			cal2 = Calendar.getInstance(UTC);
			d2 = dfm.parse(date2);
			cal2.setTime(d2);
		}

		SearchPage sp = new SearchPage(context).load();

		sp.setDateFilter(range, new Calendar[]{cal1, cal2});
		ItemListPage search = SearchPage.searchExact(context, result);

		assertTrue(search.doesResultExist(result, 1), "Result 1 does not exist");

		// make sure the presented date in the picker hasn't changed
		FilterByDateSectionPage dateFilter = sp.getDateFilter();
		assertTrue(dateFilter.getStartDate().dateEquals(d1), "Reflected start date does not match");
		assertTrue(dateFilter.getEndDate().dateEquals(d2), "Reflected end date does not match");

	}
}