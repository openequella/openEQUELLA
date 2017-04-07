package com.tle.resttests.test.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.resttests.AbstractRestApiTest;
import com.tle.resttests.util.RestTestConstants;

public class MyResourcesApiTest extends AbstractRestApiTest
{
	// @formatter:off
	private final String[][] MAP = {
		{"published", "MyResourcesApiTest - Published"},
		{"draft", "MyResourcesApiTest - Draft"},
		{"modqueue", "MyResourcesApiTest - Moderating"},
		{"archived", "MyResourcesApiTest - Archived"}
	};
	// @formatter:on

	private static final String OAUTH_CLIENT_ID = "MyResourcesApiTestClient";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
	}

	private JsonNode doMyResourcesSearch(String query, String subsearch, Map<?, ?> otherParams, String token)
		throws Exception
	{
		List<NameValuePair> params = Lists.newArrayList();
		if( query != null )
		{
			params.add(new BasicNameValuePair("q", query));
		}
		for( Entry<?, ?> entry : otherParams.entrySet() )
		{
			params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
		}
		// params.add(new BasicNameValuePair("token",
		// TokenSecurity.createSecureToken(username, "token", "token", null)));
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/search/myresources/" + subsearch + "?" + paramString);
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}

	@Test
	public void testSubsearch() throws Exception // The rest is covered by
													// SearchApiTest
	{
		String token = requestToken(OAUTH_CLIENT_ID);

		for( String[] arr : MAP )
		{
			if( isEquella() && "modqueue".equals(arr[0]) )
			{
				continue;
			}
			JsonNode resultsNode = doMyResourcesSearch("MyResourcesApiTest", arr[0],
				ImmutableMap.of("order", "name", "showall", "true"), token);
			JsonNode itemsNode = resultsNode.get("results");
			Assert.assertNotNull(itemsNode, "No results node from my resources subsearch");
			JsonNode result1 = itemsNode.get(0);
			Assert.assertNotNull(result1, "At least one result was expected for search " + arr[0]);
			asserter.assertBasic((ObjectNode) result1, arr[1], null);
		}

		JsonNode resultsNode = doMyResourcesSearch("MyResourcesApiTest", "all",
			ImmutableMap.of("order", "name", "showall", "true"), token);

		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 4);
		assertEquals(resultsNode.get("available").asInt(), 4);
	}

	@Test
	public void listSearchTypes() throws Exception
	{
		JsonNode results = getEntity(context.getBaseUrl() + "api/search/myresources", getToken());
		List<String> subSearches = Lists.newArrayList();
		for( JsonNode result : results )
		{
			subSearches.add(result.get("id").asText());
		}

		ArrayList<String> expected = Lists.newArrayList("published", "draft", "archived", "all", "modqueue");
		for( String name : expected )
		{
			assertTrue(subSearches.contains(name), "Should contain '" + name + ", " + subSearches);
		}
	}
}
