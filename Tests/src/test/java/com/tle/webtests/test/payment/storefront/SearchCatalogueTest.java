package com.tle.webtests.test.payment.storefront;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.CatalogueSearchList;
import com.tle.webtests.pageobject.payment.storefront.CatalogueSearchResult;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;

/**
 * @see DTEC: #017961
 * @author Dustin
 */
@TestInstitution("storefront")
public class SearchCatalogueTest extends AbstractSessionTest
{
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final PrefixedName CATALOGUE_NAME = new NotPrefixedName("cat1");
	private static final String SUBSCRIPTION_ITEM = "Subscription Item";
	private static final String OUTRIGHT_ITEM = "Purchase Outright Item";
	private static final String BOTH_ITEM = "Outright and Subscribe Item";
	private static final String OLD_ITEM = "Resource from the past";
	private static final String OLDER_ITEM = "Even older resource";

	@Test
	public void queryTest()
	{
		logon("autotest", "automated");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load()
			.pickStoreSingleCatalogue(STORE_NAME, CATALOGUE_NAME).get();
		CatalogueSearchList results = browseCataloguePage.search(SUBSCRIPTION_ITEM);
		assertTrue(results.doesResultExist(SUBSCRIPTION_ITEM));
		assertFalse(results.doesResultHaveThumbnail(SUBSCRIPTION_ITEM));
		results = browseCataloguePage.search(OUTRIGHT_ITEM);
		assertTrue(results.doesResultExist(OUTRIGHT_ITEM));
		results = browseCataloguePage.search(BOTH_ITEM);
		assertTrue(results.doesResultExist(BOTH_ITEM));

		browseCataloguePage.setQuery("outright");
		results = browseCataloguePage.search();

		assertFalse(results.doesResultExist(SUBSCRIPTION_ITEM));
		assertTrue(results.doesResultExist(OUTRIGHT_ITEM));
		assertTrue(results.doesResultExist(BOTH_ITEM));

		logout();
		// Other tests will cover whether the links work etc
	}

	// Everything in the top box, title, store name
	// icon is tested in SetupStoreImagesTest
	// Won't worry about breadcrumbs just yet
	@Test
	public void infoTest()
	{
		logon("autotest", "automated");
		BrowseCataloguePage cataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME).get();
		assertTrue(cataloguePage.getCatalogueName().contains(CATALOGUE_NAME.toString()));
		assertEquals(cataloguePage.getStoreName(), STORE_NAME);
		logout();
	}

	@Test
	public void sortTest() throws ParseException
	{
		logon("autotest", "automated");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME)
			.get();
		browseCataloguePage = browseCataloguePage.filterByDates(null, "2012-09-03");
		CatalogueSearchList results = browseCataloguePage.search();
		// Set filter (items modified before the test was written (so newly
		// created items don't screw up the paging))
		assertTrue(verifyInOrder(results, new String[] { SUBSCRIPTION_ITEM, OLD_ITEM, OLDER_ITEM }));
		browseCataloguePage = browseCataloguePage.reverseSort();
		assertTrue(verifyInOrder(results, new String[] { OLDER_ITEM, OLD_ITEM, SUBSCRIPTION_ITEM }));
		browseCataloguePage = browseCataloguePage.setSort("name");
		browseCataloguePage = browseCataloguePage.reverseSort();
		assertTrue(verifyInOrder(results, new String[] { OLDER_ITEM, BOTH_ITEM, OLD_ITEM }));
		assertTrue(results.doesResultHaveThumbnail(OLDER_ITEM));
		assertFalse(results.doesResultHaveThumbnail(BOTH_ITEM));
		assertTrue(results.doesResultHaveThumbnail(OLD_ITEM));
	}

	private boolean verifyInOrder(CatalogueSearchList results, String[] titles)
	{
		int j = 0;
		for( int i = 0; i < 10; i++ )
		{
			String title = results.getResult(i + 1).getTitle();
			if( title.equals(titles[j]) )
			{
				j++;
				if( j == titles.length )
				{
					return true;
				}
			}
		}
		return false;
	}

	@Test
	public void filterTest() throws ParseException
	{
		logon("autotest", "automated");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME)
			.get();
		browseCataloguePage = browseCataloguePage.filterByDates(null, "2012-07-03");
		CatalogueSearchList results = browseCataloguePage.search();
		assertTrue(results.doesResultExist(OLD_ITEM));
		assertTrue(results.doesResultExist(OLDER_ITEM));
		assertEquals(results.getResults().size(), 2);
		// I'm assuming no-one else will be contributing items in the past

		browseCataloguePage = browseCataloguePage.filterByDates("2011-09-01", "2012-07-03");
		results = browseCataloguePage.search();
		assertEquals(results.getResults().size(), 1);

		browseCataloguePage = browseCataloguePage.clearDateFilter();
		results = browseCataloguePage.search();
		assertTrue(results.getResults().size() >= 5);

		browseCataloguePage = browseCataloguePage.setPriceFilter("subscription");
		results = browseCataloguePage.exactQuery(BOTH_ITEM);
		assertTrue(results.doesResultExist(BOTH_ITEM));

		results = browseCataloguePage.exactQuery(SUBSCRIPTION_ITEM);
		assertTrue(results.doesResultExist(SUBSCRIPTION_ITEM));

		results = browseCataloguePage.exactQuery(OUTRIGHT_ITEM);
		assertFalse(results.isResultsAvailable());

		browseCataloguePage = browseCataloguePage.setPriceFilter("purchase");
		results = browseCataloguePage.exactQuery(BOTH_ITEM);
		assertTrue(results.doesResultExist(BOTH_ITEM));

		results = browseCataloguePage.exactQuery(SUBSCRIPTION_ITEM);
		assertFalse(results.isResultsAvailable());

		results = browseCataloguePage.exactQuery(OUTRIGHT_ITEM);
		assertTrue(results.doesResultExist(OUTRIGHT_ITEM));

		browseCataloguePage = browseCataloguePage.clearPriceFilter();
	}

	// Stupid name huh?
	// This tests whether the item summary stuff in the search page (Name
	// description, price) is right
	@Test
	public void resultItemTest() throws ParseException
	{
		logon("autotest", "automated");

		BrowseCataloguePage browseCataloguePage = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME)
			.get();
		browseCataloguePage.setQuery(OLDER_ITEM);
		CatalogueSearchList results = browseCataloguePage.search();
		CatalogueSearchResult result = results.getResultForTitle(OLDER_ITEM, 1);

		String title = result.getTitle();
		String description = result.getDescription();
		String details = result.getDetailText("Subscription price");

		assertEquals(title, OLDER_ITEM);
		assertEquals(description, "01/08/2010");
		assertEquals(details, "$25.00 AUD / Week");
	}
}
