package com.tle.webtests.test.webservices.rest.payment;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.webservices.rest.AbstractRestApiTest;

/*
 * Redmine #6742, #6740, #6746 #7404
 */
@TestInstitution("storebackend2ssl")
public class StoreHarvestApiTest extends AbstractRestApiTest
{
	private static final String TOKEN = "763c8c11-078f-4a2e-b8b7-9aa11d99fa7d";
	private static final String harvestDownloadUrl = "api/store/harvest/item/0a2890b7-aa3c-4c88-9b1d-552436b6f316/30e41c10-27ad-4f15-a22d-53f028ea249e";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		// don't need to add new one
	}

	@Test
	public void testHarvestCreatedDateSearch() throws Exception
	{
		final String itemBeforeTheDate = "10897ebe-aed0-4003-a925-e4b21cb4e23c";
		final String itemAfterTheDate = "0a2890b7-aa3c-4c88-9b1d-552436b6f316";

		JsonNode itemNodes = harvestSearch("harvest",
			ImmutableMap.of("createdSince", "2012-09-20T13:52:25.825+10:00", "new", false), TOKEN);
		JsonNode itemUuids = itemNodes.get("results");
		boolean itemAfterDateIsExist = false;
		boolean itemBeforeDateIsExist = false;
		for( JsonNode node : itemUuids )
		{
			if( node.get("uuid").asText().equals(itemAfterTheDate) )
			{
				itemAfterDateIsExist = true;
			}
			else if( node.get("uuid").asText().equals(itemBeforeTheDate) )
			{
				itemBeforeDateIsExist = true;
			}
		}
		assertTrue(itemAfterDateIsExist);
		assertFalse(itemBeforeDateIsExist);
	}

	@Test
	public void testStartAndLength() throws Exception
	{
		JsonNode itemNodes = harvestSearch("harvest", ImmutableMap.of("start", 1, "length", 1), TOKEN);
		assertEquals(itemNodes.get("start").asInt(), 1);
		assertEquals(itemNodes.get("length").asInt(), 1);
	}

	@Test
	public void testHarvestUnpaidItem() throws Exception
	{
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/harvest/item/f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		HttpResponse response = execute(get, false, TOKEN);
		assertResponse(response, 402, "Payment required");
	}

	@Test
	public void testHarvestPaidItem() throws Exception
	{
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/harvest/item/0a2890b7-aa3c-4c88-9b1d-552436b6f316");
		HttpResponse response = execute(get, false, TOKEN);
		JsonNode node = mapper.readTree(response.getEntity().getContent());

		assertEquals(node.get("name").asText(), "AlreadyPurchasedTest Outright Purchased Item flat rate");
		assertMetadata(node, "item/attachments_list/attachment", "30e41c10-27ad-4f15-a22d-53f028ea249e");
		assertMetadata(node, "item/name", "AlreadyPurchasedTest Outright Purchased Item flat rate");

		boolean isDownloadUrlExist = false;
		JsonNode attachments = node.get("attachments");
		for( JsonNode jsonNode : attachments )
		{
			if( jsonNode.get("links").get("harvest").asText().equals(context.getBaseUrl() + harvestDownloadUrl) )
			{
				isDownloadUrlExist = true;
			}
		}
		assertTrue(isDownloadUrlExist);
	}

	@Test
	public void testHarvestDownload() throws Exception
	{

		final File harvestFile = File.createTempFile("harvestDownload", "xlsx");
		harvestFile.deleteOnExit();
		// attachment has marked as preview
		download(context.getBaseUrl() + harvestDownloadUrl, harvestFile, TOKEN);

		String baseApiDownloadUrl = context.getBaseUrl()
			+ "api/store/catalogue/3ea6cabf-3ca2-486f-b877-66278d9ac987/item/";
		String attachmentDownloadUrl = baseApiDownloadUrl + "0a2890b7-aa3c-4c88-9b1d-552436b6f316/attachment/"
			+ "30e41c10-27ad-4f15-a22d-53f028ea249e";

		final File downloadFile = File.createTempFile("download", "xlsx");
		downloadFile.deleteOnExit();
		download(attachmentDownloadUrl, downloadFile, TOKEN);

		final String echoedMd5HarvestFile = md5(harvestFile);
		final String echoedMd5DownloadFile = md5(downloadFile);
		assertEquals(echoedMd5HarvestFile, echoedMd5DownloadFile);
	}

	@Test
	public void testSubscriptionExpiredPeriod() throws Exception
	{
		// sub dates for 10897ebe-aed0-4003-a925-e4b21cb4e23c
		// "subscriptionStartDate":"2012-09-10T00:00:00.000+10:00"
		// "subscriptionEndDate":"2020-12-10T00:00:00.000+11:00"
		JsonNode result;
		result = expiringSearch("",
			ImmutableMap.of("from", "2012-09-10T00:00:00.000+00:00", "until", "2020-12-10T00:00:00.000+00:00"),
			TOKEN);
		assertTrue(isTheItemExisted(result));

		result = expiringSearch("",
			ImmutableMap.of("from", "2012-10-10T00:00:00.000+00:00", "until", "2020-12-30T00:00:00.000+00:00"), TOKEN);
		assertTrue(isTheItemExisted(result));

		result = expiringSearch("",
			ImmutableMap.of("from", "2012-09-09T00:00:00.000+00:00", "until", "2020-11-10T00:00:00.000+00:00"), TOKEN);
		assertTrue(isTheItemExisted(result));

		result = expiringSearch("",
			ImmutableMap.of("from", "2020-12-11T00:00:00.000+00:00", "until", "2021-12-11T00:00:00.000+00:00"), TOKEN);
		assertFalse(isTheItemExisted(result));

		result = expiringSearch("",
			ImmutableMap.of("from", "2011-09-10T00:00:00.000+00:00", "until", "2012-09-09T00:00:00.000+00:00"), TOKEN);
		assertFalse(isTheItemExisted(result));
	}

	@Test
	public void testSubscriptionActivePeriod() throws Exception
	{
		JsonNode result;
		result = expiringSearch("", ImmutableMap.of("expired", true, "from", "2020-09-10T00:00:00.000+10:00", "until",
			"2020-12-10T00:00:00.000+11:00"), TOKEN);
		assertTrue(isTheItemExisted(result));

		result = expiringSearch("", ImmutableMap.of("expired", true, "from", "2020-10-10T00:00:00.000+10:00", "until",
			"2020-12-30T00:00:00.000+11:00"), TOKEN);
		assertTrue(isTheItemExisted(result));

		result = expiringSearch("", ImmutableMap.of("expired", true, "from", "2020-09-09T00:00:00.000+10:00", "until",
			"2020-11-10T00:00:00.000+11:00"), TOKEN);
		assertFalse(isTheItemExisted(result));

		result = expiringSearch("", ImmutableMap.of("expired", true, "from", "2019-09-10T00:00:00.000+10:00", "until",
			"2020-09-09T00:00:00.000+11:00"), TOKEN);
		assertFalse(isTheItemExisted(result));

		result = expiringSearch("", ImmutableMap.of("expired", true, "from", "2020-12-11T00:00:00.000+10:00", "until",
			"2021-09-09T00:00:00.000+11:00"), TOKEN);
		assertFalse(isTheItemExisted(result));
	}

	private boolean isTheItemExisted(JsonNode node)
	{
		for( JsonNode item : node.get("results") )
		{
			if( item.get("uuid").asText().equals("10897ebe-aed0-4003-a925-e4b21cb4e23c") )
			{
				return true;
			}
		}
		return false;
	}

	private void assertMetadata(JsonNode tree, String... pathsAndValues)
	{
		PropBagEx metaXml = new PropBagEx(tree.get("metadata").getTextValue());
		for( int i = 0; i < pathsAndValues.length; i += 2 )
		{
			String path = pathsAndValues[i];
			String value = pathsAndValues[i + 1];
			assertEquals(metaXml.getNode(path), value);
		}
	}

	private JsonNode harvestSearch(String info, Map<?, ?> otherParams, String token) throws Exception
	{
		List<NameValuePair> params = Lists.newArrayList();
		if( info != null )
		{
			params.add(new BasicNameValuePair("info", info));
		}
		for( Entry<?, ?> entry : otherParams.entrySet() )
		{
			params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
		}
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/harvest?" + paramString);
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}

	private JsonNode expiringSearch(String info, Map<?, ?> otherParams, String token) throws Exception
	{
		List<NameValuePair> params = Lists.newArrayList();
		if( info != null )
		{
			params.add(new BasicNameValuePair("info", info));
		}
		for( Entry<?, ?> entry : otherParams.entrySet() )
		{
			params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
		}
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/store/harvest/subscription?" + paramString);
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
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