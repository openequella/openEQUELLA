package com.tle.webtests.test.searching;

import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.google.common.io.Closeables;
import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.framework.soap.SoapService50;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.searching.CreateSearchFilterPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.searching.SearchScreenOptions;
import com.tle.webtests.pageobject.searching.SearchSettingsPage;
import com.tle.webtests.pageobject.searching.SearchSettingsPage.Order;
import com.tle.webtests.pageobject.searching.SearchTabsPage;
import com.tle.webtests.pageobject.searching.ShareSearchQuerySection;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("flakey")
public class SearchSettingsTest extends AbstractCleanupAutoTest
{
	private SoapService50 soapService;
	private SoapHelper soapHelper;

	@Override
	protected boolean isCleanupItems()
	{
		return false;
	}

	@DataProvider(name = "filters", parallel = false)
	public Object[][] filters()
	{
		return new Object[][]{{"JPEG/JPG", "image/jpeg", 1}, {"PNG", "image/png", 2}, {"BMP", "image/bmp", 3},
				{"GIF", "image/gif", 4}, {"TIFF", "image/tiff", 5}};
	}

	// Test the sort order options
	@Test(dependsOnMethods = {"testRemoveSearchFilters"})
	public void testChangeResultOrder()
	{
		SettingsPage sp = new SettingsPage(context).load();

		// Load SearchSettings Page by clicking settings link
		SearchSettingsPage searchSettingsPage = sp.searchSettings();
		Order sortOption = SearchSettingsPage.Order.RANK;
		searchSettingsPage.setOrder(sortOption).save();

		logon("AutoTest", "automated");

		SearchPage searchPage = new SearchPage(context).load();

		assertTrue(searchPage.ensureSortSelected(sortOption.name()));
		// TODO Check results properly
		// assertTrue(checkItemOrder(searchPage.search().get().getResults(),
		// sortOption));

		sortOption = Order.DATEMODIFIED;
		searchSettingsPage = new SettingsPage(context).load().searchSettings();
		searchSettingsPage.setOrder(sortOption).save();

		logon("AutoTest", "automated");

		searchPage = new SearchPage(context).load();

		assertTrue(searchPage.ensureSortSelected(sortOption.name()));
		// assertTrue(true); TODO Check results

		sortOption = SearchSettingsPage.Order.NAME;
		searchSettingsPage = new SettingsPage(context).load().searchSettings();
		searchSettingsPage.setOrder(sortOption).save();

		logon("AutoTest", "automated");

		searchPage = new SearchPage(context).load();

		assertTrue(searchPage.ensureSortSelected(sortOption.name()));
		// assertTrue(true); TODO Check results

		sortOption = SearchSettingsPage.Order.RATING;
		searchSettingsPage = new SettingsPage(context).load().searchSettings().get();

		searchSettingsPage.setOrder(sortOption).save();

		logon("AutoTest", "automated");

		searchPage = new SearchPage(context).load();

		assertTrue(searchPage.ensureSortSelected(sortOption.name()));
		// assertTrue(true); TODO Check results
	}

	// Test the show non live options
	@Test(dependsOnMethods = {"testRemoveSearchFilters"})
	public void testShowNonLive()
	{
		logon("AutoTest", "automated");
		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings().load();
		ssp.setOrder(SearchSettingsPage.Order.RANK);
		ssp.includeNonLive(true).save();

		SearchScreenOptions sso = new SearchPage(context).load().openScreenOptions();

		assertTrue(sso.hasNonLiveOption());
		sso.setNonLiveOption(true);
		ItemListPage results = SearchPage.searchExact(context, "SearchSettings - Draft Item");
		assertTrue(results.doesResultExist("SearchSettings - Draft Item", 1));

		ssp = new SettingsPage(context).load().searchSettings();
		ssp.includeNonLive(false).save();

		sso = new SearchPage(context).load().openScreenOptions();
		assertTrue(!(sso.hasNonLiveOption()));

		results = SearchPage.searchExact(context, "SearchSettings - Draft Item");
		assertTrue(!results.doesResultExist("SearchSettings - Draft Item", 1));

	}

	@Test
	public void testDisableCloud()
	{
		logon("AutoTest", "automated");
		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings().load();
		ssp.setDisableCloud(true);
		ssp.save();

		// go to search page, test non-cloudiness
		SearchPage searchPage = new SearchPage(context).load();
		SearchTabsPage searchTabs = searchPage.getSearchTabs();
		assertTrue(!searchTabs.hasTab("cloud"), "Can still see cloud tab");

		ssp = new SettingsPage(context).load().searchSettings().load();
		assertTrue(ssp.isDisableCloud(), "Cloud was not disabled");
		ssp.setDisableCloud(false);
		ssp.save();

		// go to search page, test cloudiness
		searchPage = new SearchPage(context).load();
		searchTabs = searchPage.getSearchTabs();
		assertTrue(searchTabs.hasTab("cloud"), "Can not see cloud tab");

		ssp = new SettingsPage(context).load().searchSettings().load();
		assertTrue(!ssp.isDisableCloud(), "Cloud was not enabled");
	}

	// Test the setup/usage of the MIME type search filters
	@Test(dataProvider = "filters")
	public void testSetupSearchFilters(String filterName, String mimetype, int count)
	{
		logon("AutoTest", "automated");
		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings();
		CreateSearchFilterPage sfp = ssp.addFilter();
		sfp.setName(filterName).selectMimeType(mimetype, true).save();

		// Check for receipt
		ReceiptPage.waiter("Search filter saved successfully", ssp).get();

		// Check table
		assertTrue(ssp.hasFilter(filterName), "Did not find filter with name: " + filterName);

		SearchPage searchPage = new SearchPage(context).load();
		assertTrue(searchPage.hasResourceTypeFilter(filterName), "Expected to find " + filterName
			+ " in resource type filters");
		searchPage.checkResourceTypeFilter(filterName, true);
		String itemName = MessageFormat.format("SearchSettings - Image {0} - {1}", count, filterName);
		ItemListPage results = SearchPage.searchExact(context, itemName);
		assertTrue(results.doesResultExist(itemName, 1));
	}

	// Test the editing of MIME type search filters
	@Test(dependsOnMethods = {"testRemoveSearchFilters"})
	public void testEditSearchFilter()
	{
		logon("AutoTest", "automated");
		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings();
		CreateSearchFilterPage sfp = ssp.addFilter();
		String filterName = "All Images";
		String filterNameEdited = "All Images - Edited";

		sfp.saveWithErrors();
		assertTrue(sfp.nameValidationExists());
		assertTrue(sfp.mimeTypeValidationExists());

		sfp.setName(filterName).saveWithErrors();
		assertTrue(!sfp.nameValidationExists());

		sfp.setName("");
		sfp.selectMimeTypes(Arrays.asList("image/jpeg", "image/bmp", "image/png", "image/tiff", "equella/plan"))
			.saveWithErrors();
		assertTrue(!sfp.mimeTypeValidationExists());

		ssp = sfp.setName(filterName).save();

		sfp = ssp.editFilter(filterName);
		sfp.selectMimeType("equella/plan", false);
		ssp = sfp.selectMimeType("image/gif", true).setName(filterNameEdited).save().get();
		assertTrue(ssp.hasFilter(filterNameEdited), "Filter '" + filterNameEdited + "' was not present");

		SearchPage searchPage = new SearchPage(context).load();
		assertTrue(searchPage.hasResourceTypeFilter(filterNameEdited));

		ssp = new SettingsPage(context).load().searchSettings();
		sfp = ssp.editFilter(filterNameEdited);
		String bogusName = "bogusfiltername";
		sfp.setName(bogusName).cancel();
		assertTrue(!ssp.hasFilter(bogusName));

		ssp = new SettingsPage(context).load().searchSettings();
		ssp.removeFilter(filterNameEdited);
		assertTrue(!ssp.hasFilter(filterNameEdited));

	}

	// Test the removal of the MIME type search filters
	@Test(dependsOnMethods = {"testSetupSearchFilters"}, dataProvider = "filters")
	public void testRemoveSearchFilters(String filterName, String mimetype, int count)
	{
		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings();
		ssp.removeFilter(filterName);
		assertTrue(!ssp.hasFilter(filterName), "Expected not to see " + filterName + " after removal");
	}

	// Test Authenticated feeds
	@Test(dependsOnMethods = {"testRemoveSearchFilters"})
	public void testGenerateAuthenticatedFeeds() throws Exception
	{
		logon("AutoTest", "automated");
		final String searchTerm = "Relevance";
		final String authString = "auth=basic";

		// Login
		soapService.login("AutoTest", "automated");

		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings();
		ssp.setGenerateAuthFeeds(false).save();

		// Do a search and get RSS url
		SearchPage searchPage = new SearchPage(context).load();
		searchPage.search(searchTerm);
		ShareSearchQuerySection ssDialog = searchPage.shareSearch();
		String rssUrl = ssDialog.getRssUrl();
		String atomUrl = ssDialog.getAtomUrl();

		assertTrue(!rssUrl.contains(authString));
		assertTrue(!atomUrl.contains(authString));

		// Check results
		Credentials basicCreds = new UsernamePasswordCredentials("AutoTest", "automated");
		assertTrue(checkRssResponse(getResponse(soapService, rssUrl, null), true));
		assertTrue(checkAtomResponse(getResponse(soapService, atomUrl, null), true));

		// Enable authenticated results
		ssp = new SettingsPage(context).load().searchSettings();
		ssp.setGenerateAuthFeeds(true).save();

		// Do a search and get RSS url
		searchPage = new SearchPage(context).load();
		searchPage.search(searchTerm);
		ssDialog = searchPage.shareSearch();
		rssUrl = ssDialog.getRssUrl();
		atomUrl = ssDialog.getAtomUrl();
		assertTrue(rssUrl.contains(authString));
		assertTrue(atomUrl.contains(authString));

		// Check results
		assertTrue(checkRssResponse(getResponse(soapService, rssUrl, basicCreds), false));
		assertTrue(checkAtomResponse(getResponse(soapService, atomUrl, basicCreds), false));
	}

	@Test(dependsOnMethods = {"testRemoveSearchFilters"})
	public void testContentIndexing() throws Exception
	{
		logon("AutoTest", "automated");
		SearchSettingsPage ssp = new SettingsPage(context).load().searchSettings();
		ssp.setNoIndexingOfPages().save();
		logout();

		logon("AutoTest", "automated");
		ssp = new SettingsPage(context).load().searchSettings();
		assertTrue(ssp.isNoIndexingOfPages());

		ssp.setIndexPageOnly().save();
		logout();

		logon("AutoTest", "automated");
		ssp = new SettingsPage(context).load().searchSettings();
		assertTrue(ssp.isIndexPageOnly());

		ssp.setIndex2ndryPages().save();
		logout();

		logon("AutoTest", "automated");
		ssp = new SettingsPage(context).load().searchSettings();
		assertTrue(ssp.isIndex2ndryPages());
	}

	private boolean checkRssResponse(PropBagEx response, boolean single)
	{
		final String itemRandom = "SearchSettings - Random Item";
		final String itemRelevance = "SearchSettings - Relevance";

		if( single )
		{
			String title = response.getNode("channel/item/title");
			return title.equals(itemRandom);
		}
		else
		{
			PropBagIterator iter = response.iterator("channel/item");
			List<String> itemTitles = new ArrayList<String>();
			for( PropBagEx item : iter )
			{
				itemTitles.add(item.getNode("title"));
			}
			return itemTitles.contains(itemRandom) && itemTitles.contains(itemRelevance);
		}
	}

	private boolean checkAtomResponse(PropBagEx response, boolean single)
	{
		final String itemRandom = "SearchSettings - Random Item";
		final String itemRelevance = "SearchSettings - Relevance";

		if( single )
		{
			String title = response.getNode("entry/title");
			return title.equals(itemRandom);
		}
		else
		{
			PropBagIterator iter = response.iterator("entry");
			List<String> itemTitles = new ArrayList<String>();
			for( PropBagEx item : iter )
			{
				itemTitles.add(item.getNode("title"));
			}
			return itemTitles.contains(itemRandom) && itemTitles.contains(itemRelevance);
		}
	}

	private PropBagEx getResponse(SoapService50 soapService, String uri, Credentials creds) throws Exception
	{
		HttpGet get = new HttpGet(uri);
		if( creds != null )
		{
			get.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
		}

		HttpClient client = new DefaultHttpClient();
		InputStream in = client.execute(get).getEntity().getContent();
		try
		{
			return new PropBagEx(in);
		}
		finally
		{
			Closeables.closeQuietly(in);
		}
	}

	@BeforeClass
	public void setupSoapService() throws MalformedURLException
	{
		soapHelper = new SoapHelper(context);
		soapService = soapHelper.createSoap(SoapService50.class, "services/SoapService50",
			"http://soap.remoting.web.tle.com", null);
	}

	@SuppressWarnings("unused")
	private boolean checkItemOrder(List<ItemSearchResult> searchResults, Order order)
	{
		boolean orderMatches = true;

		switch( order )
		{
			case RANK:
				break;
			case DATEMODIFIED:
				break;
			case NAME:
				break;
			case RATING:
				break;
		}

		return orderMatches;
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");
		SettingsPage sp = new SettingsPage(context).load();

		// Load SearchSettings Page by clicking settings link
		SearchSettingsPage searchSettingsPage = sp.searchSettings();
		Order sortOption = SearchSettingsPage.Order.RANK;
		searchSettingsPage.setOrder(sortOption).save();
		searchSettingsPage.setDisableCloud(false);

		super.cleanupAfterClass();
	}
}