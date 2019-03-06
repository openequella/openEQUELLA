package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.component.PopupTermDialog;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.PowerSearchPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.controls.CalendarControl;
import com.tle.webtests.pageobject.wizard.controls.PopupTermControl;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("fiveo")
public class PowerSearchTest extends AbstractCleanupAutoTest
{
	static final String FIRST = "PowerSearchTest - Single Selections";
	static final String SECOND = "PowerSearchTest - Second Single";
	static final String MULTIPLE = "PowerSearchTest - Multiple Selections";

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		// dont clean anything
	}

	@Test
	public void editBox()
	{
		PowerSearchPage power = power();
		power.editbox(4, "An Edit box");
		power.search();
		assertResults(FIRST);

		power = power();
		power.editbox(4, "Something else");
		power.search();
		assertResults(SECOND);

		power = power();
		power.editbox(4, "*");
		power.search();
		assertResults(FIRST, SECOND, MULTIPLE);
	}

	@Test
	public void checkBox()
	{
		PowerSearchPage power = power();
		power.setCheck(3, "1", true);
		power.search();
		assertResults(FIRST, MULTIPLE);

		power = power();
		power.setCheck(3, "2", true);
		power.search();
		assertResults(SECOND, MULTIPLE);

		power = power();
		power.setCheck(3, "1", true);
		power.setCheck(3, "2", true);
		power.search();
		assertResults(FIRST, SECOND, MULTIPLE);
	}

	@Test
	public void dates() throws Exception
	{
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		dfm.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

		PowerSearchPage power = power();
		CalendarControl range = power.calendar(1);
		range.setDateRange(dfm.parse("2011-04-20"), dfm.parse("2011-04-29"));
		power.search();
		assertResults(FIRST, SECOND);

		power = power();
		range = power.calendar(1);
		range.setDateRange(dfm.parse("2011-04-29"), dfm.parse("2011-04-29"));
		power.search();
		assertResults(SECOND);

		power = power();
		range = power.calendar(1);
		range.setDateRange(dfm.parse("2011-04-01"), dfm.parse("2011-04-01"));
		power.search();
		assertResults();

		power = power();
		range = power.calendar(2);
		range.setDateRange(dfm.parse("2011-04-01"), dfm.parse("2011-04-10"));
		power.search();
		assertResults(FIRST);

		power = power();
		range = power.calendar(2);
		range.setDateRange(dfm.parse("2011-04-01"), dfm.parse("2011-04-01"));
		power.search();
		assertResults(FIRST);

		power = power();
		range = power.calendar(2);
		range.setDateRange(dfm.parse("2011-04-10"), dfm.parse("2011-04-10"));
		power.search();
		assertResults(FIRST);

		power = power();
		range = power.calendar(2);
		range.setDate(dfm.parse("2011-04-11"));
		power.search();
		assertResults();

		power = power();
		range = power.calendar(2);
		range.setEndDate(dfm.parse("2011-05-11"));
		power.search();
		assertResults(FIRST);
	}

	@Test()
	public void listBox()
	{
		PowerSearchPage power = power();
		power.selectDropDown(5, "1");
		power.search();
		assertResults(FIRST);
	}

	@Test
	public void radioGroup()
	{
		PowerSearchPage power = power();
		power.setCheck(6, "1", true);
		power.search();
		assertResults(FIRST);

		power = power();
		power.setCheck(6, "2", true);
		power.search();
		assertResults(SECOND);
	}

	@Test
	public void shuffleBox()
	{
		PowerSearchPage power = power();
		power.selectShuffle(9, "1");
		power.search();
		assertResults(FIRST, MULTIPLE);

		power = power();
		power.selectShuffle(9, "2");
		power.search();
		assertResults(SECOND, MULTIPLE);

		power = power();
		power.selectShuffle(9, "1");
		power.selectShuffle(9, "2");
		power.search();
		assertResults(FIRST, SECOND, MULTIPLE);
	}

	@Test
	public void shuffleList()
	{
		PowerSearchPage power = power();
		power.addToShuffleList(10, "1");
		power.search();
		assertResults(FIRST);

		power = power();
		power.addToShuffleList(10, "2");
		power.search();
		assertResults(SECOND);

		power = power();
		power.addToShuffleList(10, "1");
		power.addToShuffleList(10, "2");
		power.search();
		assertResults(FIRST, SECOND);
	}

	@Test
	public void autoTerm()
	{
		PowerSearchPage power = power();
		power.autoTermControl(12).addNewTerm("term");
		power.search();
		assertResults(FIRST, MULTIPLE);

		power = power();
		power.autoTermControl(12).addNewTerm("term 2");
		power.search();
		assertResults(MULTIPLE);
	}

	@Test
	public void popupTerm()
	{
		PowerSearchPage power = power();
		PopupTermControl popupTermControl = power.popupTermControl(13);
		PopupTermDialog dialog = popupTermControl.openDialog();
		dialog.selectTerm("term");
		dialog.finish(popupTermControl.selectWaiter("term"));
		power.search();
		assertResults();

		power = power();
		popupTermControl = power.popupTermControl(13);
		dialog = popupTermControl.openDialog();
		dialog.selectTerm("term 2");
		dialog.finish(popupTermControl.selectWaiter("term 2"));
		power.search();
		assertResults(FIRST);
	}

	@Test
	public void multipleControls()
	{
		PowerSearchPage power = power();
		power.setCheck(3, "1", true);
		power.selectShuffle(9, "2");
		power.search();
		assertResults(MULTIPLE);

		power = power();
		power.setCheck(3, "1", true);
		power.setCheck(3, "2", true);
		power.selectShuffle(9, "1");
		power.selectShuffle(9, "2");
		power.search();
		assertResults(FIRST, SECOND, MULTIPLE);
	}

	@Test
	public void userSelector()
	{
		PowerSearchPage power = power();
		power.selectUser(14).queryAndSelect("a", "AutoTest");
		power.search();
		assertResults(FIRST);
	}

	@Test
	public void rawHtml()
	{
		PowerSearchPage power = power();
		assertEquals(power.getRawHtmlText(8), "test");
	}

	@Test
	public void editAndClear()
	{
		PowerSearchPage power = power();
		power.editbox(4, "An Edit box");
		SearchPage search = power.search();
		assertResults(FIRST);

		// Edit query and ensure value is the same
		power = search.editQuery();
		assertEquals(power.editbox(4).getText(), "An Edit box");

		// Change it to something else and ensure the results change
		power.editbox(4, "Something else");
		search = power.search();
		assertResults(SECOND);

		// Clear the query and ensure the results are now for everything
		search = search.clearQuery();
		assertResults(FIRST, SECOND, MULTIPLE);

		// Edit again to ensure the control is now empty
		power = search.editQuery();
		assertEquals(power.editbox(4).getText(), "");

		// And again make sure the results are for everything
		power.search();
		assertResults(FIRST, SECOND, MULTIPLE);
	}

	private void assertResults(String... titles)
	{
		ItemListPage results = new SearchPage(context).results();
		assertEquals(results.getResults().size(), titles.length, "Wrong number of results");

		for( String title : titles )
		{
			assertTrue(results.doesResultExist(title, 1), "Item '" + title + "' not found");
		}
	}

	private PowerSearchPage power()
	{
		SearchPage search = new SearchPage(context).load();
		return search.setWithinPowerSearch("All Controls Power Search");
	}
}
