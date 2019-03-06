package com.tle.webtests.test.searching;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.Keys;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class BasicSearchingTest extends AbstractCleanupTest
{

	private static final String[] ALL_SEARCHING_ITEMS = new String[]{"Searching item 1", "Searching item 2",
			"Searching item 3", "Searching item 4"};
	private static final String[] ALL_HORSE_ITEMS = new String[]{"Horse 1", "Horse 2", "Horse 3", "Horse 4"};

	public BasicSearchingTest()
	{
		setDeleteCredentials("admin", "``````");

	}

	@Override
	public void setupSubcontext(Method testMethod)
	{
		// no subcontext
	}

	/* This test creates a bunch of items and runs through the different search syntaxes listed
	 * in the help file from the search page to check that it returns the correct results. The
	 * items are added from the one collection but have name variations to account for the
	 * different syntax options.
	*/

	@DataProvider(name = "itemNames", parallel = false)
	public Object[][] itemNames()
	{
		return new Object[][]{{"Searching item 1"}, {"Searching item 2"}, {"Searching item 3"}, {"Searching item 4"},
				{"Horse 1"}, {"Horse 2"}, {"Horse 3"}, {"Horse 4"},

				{"Search Item Horse 1"}, {"Search 2"}, {"Horsey 3"}, {"Horge 4"}, {"Search horse 4"}};
	}

	@DataProvider(name = "queries", parallel = false)
	public Object[][] queries()
	{
		List<String> tempCol = new ArrayList<String>();
		tempCol.addAll(Arrays.asList(ALL_SEARCHING_ITEMS));
		tempCol.addAll(Arrays.asList(ALL_HORSE_ITEMS));
		String[] allHorseAndSearch = tempCol.toArray(new String[tempCol.size()]);
		return new Object[][]{
				// Searching item
				{"searching item", ALL_SEARCHING_ITEMS, null},
				// Wildcard action
				{"sear*", ALL_SEARCHING_ITEMS, ALL_HORSE_ITEMS},
				// Horses
				{"Horse", ALL_HORSE_ITEMS, null},
				{"Horse or Searching", allHorseAndSearch, null},
				{"horse or searching", allHorseAndSearch, null},
				{"hor*", new String[]{"Horsey 3", "Horge 4", "Horse 3", "Search Item Horse 1"}, null},
				{"hor?e", new String[]{"Horse 3", "Horge 4", "Horse 1", "Search Item Horse 1"},
						new String[]{"Horsey 3"}},
				{"hor?e*", new String[]{"Horsey 3", "Horge 4", "Horse 3", "Search Item Horse 1"}, null},
				{"horse AND search", new String[]{"Search Item Horse 1"}, null},
				{"horse +search", new String[]{"Search Item Horse 1"}, ALL_HORSE_ITEMS},
				{"horse NOT search", ALL_HORSE_ITEMS, ALL_SEARCHING_ITEMS},
				{"+horse +search", new String[]{"Search Item Horse 1"}, allHorseAndSearch},
				{"search -horse", new String[]{"Search 2"}, ALL_HORSE_ITEMS},
				{/* BUG in Selenium - http://code.google.com/p/selenium/issues/detail?id=1723 */
				"search AND " + Keys.chord(Keys.SHIFT, "9") + "item OR horse" + Keys.chord(Keys.SHIFT, "0"),
						new String[]{"Search Item Horse 1", "Search horse 4"}, null},
				{"house~", ALL_HORSE_ITEMS, null},};
	}

	@Test(dataProvider = "itemNames")
	public void contribute(String itemName)
	{
		// Login as the admin user and create some items.
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Basic collection for searching");
		wizard.editbox(1, context.getFullName(itemName));
		wizard.save().publish();
	}

	@Override
	protected void prepareBrowserSession()
	{
		logon("admin", "``````");
		new SearchPage(context).load().setPerPage("max");
	}

	@Test(dependsOnMethods = "contribute", dataProvider = "queries")
	public void search(String query, String[] items, String[] notItems)
	{
		ItemListPage results = new SearchPage(context).load().search("+" + context.getNamePrefix() + " " + query);
		for( String item : items )
		{
			String itemName = context.getFullName(item);
			assertTrue(results.doesResultExist(itemName, 1), "Expecting '" + itemName + "' for query:" + query);
		}
		if( notItems != null )
		{
			for( String item : notItems )
			{
				String itemName = context.getFullName(item);
				assertFalse(results.doesResultExist(itemName, 1), "Not expecting '" + itemName + "' for query:" + query);
			}
		}
	}
}
