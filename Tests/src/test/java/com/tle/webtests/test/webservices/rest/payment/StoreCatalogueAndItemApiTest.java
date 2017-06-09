package com.tle.webtests.test.webservices.rest.payment;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.test.files.Attachments;
import com.tle.webtests.test.webservices.rest.AbstractRestApiTest;

/**
 * @see Redmine: #6738, #6739 #6891
 */

@TestInstitution("storebackendssl")
public class StoreCatalogueAndItemApiTest extends AbstractRestApiTest
{
	private static final String TOKEN = "763c8c11-078f-4a2e-b8b7-9aa11d99fa7d";
	private static final String CATALOGUE_NAME = "cat1";
	private static final String CATALOGUE_UUID = "3ea6cabf-3ca2-486f-b877-66278d9ac987";

	@Test
	public void testListOfCatalogues() throws Exception
	{
		boolean found = false;
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/catalogue");

		JsonNode catalogueLists = getJsonNodes(get, TOKEN);
		for( Iterator iterator = catalogueLists.iterator(); iterator.hasNext(); )
		{
			JsonNode cat = (JsonNode) iterator.next();
			if( CATALOGUE_NAME.equals(cat.get("name").asText()) )
			{
				found = true;
				break;
			}
		}
		assertTrue(found, "Expected to find '" + CATALOGUE_NAME + "' in " + catalogueLists);
	}

	@Test
	public void testCatalogueInfo() throws Exception
	{
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/catalogue/" + CATALOGUE_UUID);

		JsonNode catalogue = getJsonNodes(get, TOKEN);
		assertEquals(catalogue.get("name").asText(), CATALOGUE_NAME);
	}

	@Test
	public void testCatalogueSearchItem() throws Exception
	{
		JsonNode resultsNode = searchCatalogueItems("basic", "AlreadyPurchasedTest",
			ImmutableMap.of("order", "name", "showall", "true"), CATALOGUE_UUID, TOKEN);

		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 2);
		assertEquals(resultsNode.get("available").asInt(), 2);
		JsonNode itemsNode = resultsNode.get("results");
		assertEquals(itemsNode.get(0).get("name").asText(), "AlreadyPurchasedTest Outright Purchased Item");
		assertEquals(itemsNode.get(1).get("name").asText(), "AlreadyPurchasedTest Subscribed Item");
	}

	@Test(dataProvider = "ordering")
	public void testStoreItem(String order, Integer[] itemIndexes) throws Exception
	{
		// @formatter:off
		final String[][] storeItems = {
			{"REST API - Store Item 1", "This an item from storebackendssl"},
			{"REST API - Store Item 2", "Another item from storebackendssl"}, 
			{"REST API - Store Item 3", "One more item from storebackendssl"}
		};
		
		final String[][] pricePeriod = {
			{"1","WEEKS","Week","2500","AUD"},
			{"1","MONTHS","Month","5000","AUD"},
			{"3","MONTHS","3 Months","10000","AUD"},
			{"6","MONTHS","6 Months","15000","AUD"},
			{"1","YEARS","Year","20000","AUD"}
		};
		// @formatter:on

		JsonNode resultsNode = searchCatalogueItems("basic", "REST API - Store Item",
			ImmutableMap.of("order", order, "name", "true"), CATALOGUE_UUID, TOKEN);
		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 3);
		assertEquals(resultsNode.get("available").asInt(), 3);

		JsonNode itemsNode = resultsNode.get("results");
		int i = 0;
		String itemUuids[] = new String[3];
		for( JsonNode itemNode : itemsNode )
		{
			String[] itemDeets = storeItems[itemIndexes[i]];
			asserter.assertBasic((ObjectNode) itemNode, itemDeets[0], itemDeets[1]);

			String itemUuid = itemNode.get("uuid").asText();
			itemUuids[i] = itemUuid;
			i++;
		}

		String baseApiCallUrl = context.getBaseUrl() + "api/store/catalogue/" + CATALOGUE_UUID + "/item/";

		// first item is free
		HttpGet getFirstItem = new HttpGet(baseApiCallUrl + itemUuids[0]);
		JsonNode firstItem = getJsonNodes(getFirstItem, TOKEN);
		asserter.assertBasic((ObjectNode) firstItem, storeItems[0][0], storeItems[0][1]);
		assertTrue(firstItem.get("free").asBoolean());

		JsonNode attachmentNode1 = firstItem.get("attachments");
		String attachmentDownloadUrl = baseApiCallUrl + itemUuids[0] + "/attachment/"
			+ attachmentNode1.get(0).get("uuid").asText();

		final File file = File.createTempFile("AAAA", "txt");
		file.deleteOnExit();
		// attachment has marked as preview
		download(attachmentDownloadUrl, file, TOKEN);

		final File original = new File(AbstractPage.getPathFromUrl(Attachments.get("A.txt")));
		final String origMd5 = md5(original);
		final String echoedMd5 = md5(file);
		assertEquals(origMd5, echoedMd5);

		// second item has outright purchase tier
		HttpGet getSecondItem = new HttpGet(baseApiCallUrl + itemUuids[1]);
		JsonNode secondItem = getJsonNodes(getSecondItem, TOKEN);
		asserter.assertBasic((ObjectNode) secondItem, storeItems[1][0], storeItems[1][1]);
		assertFalse(secondItem.get("free").asBoolean());

		JsonNode attachmentNode2 = secondItem.get("attachments");
		String downloadUrl = baseApiCallUrl + itemUuids[1] + "/attachment/"
			+ attachmentNode2.get(0).get("uuid").asText();

		final File file1 = File.createTempFile("BBBB", "txt");
		file1.deleteOnExit();
		HttpResponse response = download(downloadUrl, file1, TOKEN);
		// file hasn't marked as preview
		assertResponse(response, 404, "Not Found");

		JsonNode purchaseTierNode = secondItem.get("purchaseTier");
		assertEquals(purchaseTierNode.get("name").asText(), "Outright Purchase");
		assertTrue(purchaseTierNode.get("perUser").asBoolean());
		assertEquals(purchaseTierNode.get("price").get("value").get("value").asInt(), 500);
		assertEquals(purchaseTierNode.get("price").get("currency").asText(), "AUD");

		// third item has subscription tier
		HttpGet getThirdItem = new HttpGet(baseApiCallUrl + itemUuids[2]);
		JsonNode thirdItem = getJsonNodes(getThirdItem, TOKEN);
		asserter.assertBasic((ObjectNode) thirdItem, storeItems[2][0], storeItems[2][1]);
		assertFalse(thirdItem.get("free").asBoolean());

		JsonNode subScriptionTierNode = thirdItem.get("subscriptionTier");
		assertEquals(subScriptionTierNode.get("name").asText(), "Subscription");
		assertTrue(subScriptionTierNode.get("perUser").asBoolean());
		JsonNode periodNodes = subScriptionTierNode.get("prices");

		int j = 0;
		for( JsonNode jsonNode : periodNodes )
		{
			JsonNode period = jsonNode.get("period");
			assertEquals(period.get("duration").asText(), pricePeriod[j][0]);
			assertEquals(period.get("durationUnit").asText(), pricePeriod[j][1]);
			assertEquals(period.get("name").asText(), pricePeriod[j][2]);
			assertEquals(jsonNode.get("value").get("value").asText(), pricePeriod[j][3]);
			assertEquals(jsonNode.get("currency").asText(), pricePeriod[j][4]);
			j++;
		}
	}

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		// don't need to add a new user, just use the registered store front
		// token ;
	}

	private JsonNode getJsonNodes(HttpGet get, String token) throws Exception
	{
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}

	private JsonNode searchCatalogueItems(String info, String query, Map<?, ?> otherParams, String catUuid, String token)
		throws Exception
	{
		List<NameValuePair> params = Lists.newArrayList();
		if( info != null )
		{
			params.add(new BasicNameValuePair("info", info));
		}
		if( query != null )
		{
			params.add(new BasicNameValuePair("q", query));
		}
		for( Entry<?, ?> entry : otherParams.entrySet() )
		{
			params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
		}
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/catalogue/" + catUuid + "/search?" + paramString);

		return getJsonNodes(get, token);
	}

	@DataProvider(name = "ordering")
	public Object[][] getSortData()
	{
		return new Object[][] { { "name", new Integer[] { 0, 1, 2 } }, };
	}

	private String md5(File file) throws IOException
	{
		InputStream inp = new FileInputStream(file);
		try
		{
			return DigestUtils.md5Hex(inp);
		}
		finally
		{
			Closeables.closeQuietly(inp);
		}
	}

}
