package com.tle.webtests.test.payment.global;

import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.payment.storefront.BrowseCataloguePage;
import com.tle.webtests.pageobject.payment.storefront.ShopPage;
import com.tle.webtests.pageobject.payment.storefront.StoreFrontSettingsPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.settings.ScheduledTasksPage;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * @see DTEC: #017974, #017972
 * @author Seb
 */
// Can't run in parallel
@TestInstitution("storefront")
public class PurchaseCollectionTest extends AbstractCleanupTest
{
	private static final String NO_COLLECTION = "<None>";
	private static final String PURCHASE_COLLECTION = "Basic Items";
	private static final String STORE_NAME = RegisterStoreAndStoreFront.STORE_NAME;
	private static final String STORE_NAME2 = RegisterStoreAndStoreFront.STORE_NAME2;

	private static final PrefixedName CATALOGUE = new NotPrefixedName("cat1");

	@Test
	public void testPurchaseCollection()
	{
		final String ITEM = namePrefix + " Collection Item";
		final String ITEM2 = ITEM + " 2";

		logon(namePrefix, "``````");

		StoreFrontSettingsPage purchaseColl = new StoreFrontSettingsPage(context).load();
		purchaseColl.setCollection(NO_COLLECTION);
		Assert.assertTrue(purchaseColl.saveAndCheckReceipt(), "No/Wrong save receipt");

		BrowseCataloguePage browse = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME, CATALOGUE);
		browse.search(ITEM).getResult(1).viewSummary().addToCart().viewCart().getStoreSection(STORE_NAME)
			.downloadResources();

		ItemListPage results = new SearchPage(context).load().search(ITEM);
		Assert.assertFalse(results.doesResultExist(ITEM));

		purchaseColl = new StoreFrontSettingsPage(context).load();
		purchaseColl.setCollection(PURCHASE_COLLECTION);
		Assert.assertTrue(purchaseColl.saveAndCheckReceipt(), "No/Wrong save receipt");

		results = new SearchPage(context).load().search(ITEM);
		Assert.assertFalse(results.doesResultExist(ITEM));

		logout();
		logon("TLE_ADMINISTRATOR", "tle010");
		ScheduledTasksPage stp = new ScheduledTasksPage(context).load();
		long startTime = System.currentTimeMillis();
		stp.runCheckUpdatedPurchasedItemsTask(true);
		sixtySecondSleep();
		logout();
		logon(namePrefix, "``````");
		boolean found = false;
		// keep trying until it's there. Maximum 3 times
		for( int tries = 0; tries < 3; tries++ )
		{
			try
			{
				results = new SearchPage(context).load().waitForResult(ITEM, 1);
				if( results.doesResultExist(ITEM) )
				{
					found = true;
					break;
				}
			}
			catch( TimeoutException to )
			{
				// item not harvested, try again
			}
			sixtySecondSleep();

		}

		if( !found )
		{
			long finishTime = System.currentTimeMillis();
			long total = (finishTime - startTime) / 1000;
			Assert.assertTrue(false, "Waited " + total + " seconds, item not harvested");
		}

		// test that a different collection works the same
		purchaseColl = new StoreFrontSettingsPage(context).load();
		purchaseColl.setCollection("Purchase Collection");
		Assert.assertTrue(purchaseColl.saveAndCheckReceipt(), "No/Wrong save receipt");

		browse = new ShopPage(context).load().pickStoreSingleCatalogue(STORE_NAME2, CATALOGUE);
		browse.search(ITEM).getResult(1).viewSummary().addToCart().viewCart().getStoreSection(STORE_NAME2)
			.downloadResources();

		results = new SearchPage(context).load().setWithinCollection("Purchase Collection").search(namePrefix);

		results = new SearchPage(context).load().waitForResult(ITEM2, 1);
		Assert.assertTrue(results.doesResultExist(ITEM2), "Item not harvested");
		Assert.assertFalse(results.doesResultExist(ITEM), "Item put in wrong collection");
		results = new SearchPage(context).load().setWithinCollection("Basic Items").search(namePrefix);
		Assert.assertFalse(results.doesResultExist(ITEM2), "Item put in wrong collection");

	}

	private void sixtySecondSleep()
	{
		try
		{
			Thread.sleep(60000);
		}
		catch( InterruptedException e )
		{
			// Oh well
		}
	}

	@Override
	protected boolean isCleanupItems()
	{
		return false;
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();
		logon(namePrefix, "``````");
		StoreFrontSettingsPage purchaseColl = new StoreFrontSettingsPage(context).load();
		if( !PURCHASE_COLLECTION.equals(purchaseColl.getCollection()) )
		{
			purchaseColl.setCollection(PURCHASE_COLLECTION);
			purchaseColl.saveAndCheckReceipt();
		}
	}

}
