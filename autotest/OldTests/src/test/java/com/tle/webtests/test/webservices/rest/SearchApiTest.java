package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.tle.common.Pair;

public class SearchApiTest extends AbstractRestApiTest
{
	public static final String SEARCH_API_TEST = SearchApiTest.class.getSimpleName();
	// @formatter:off
	private static final String[][] ITEMS = {
		{SEARCH_API_TEST + " - Aardvark", "Description 1"},
		{SEARCH_API_TEST + " - Basic", "Description 2"}, 
		{SEARCH_API_TEST + " - Crabs", "Description 3"},
		{SEARCH_API_TEST + " - Dogs", "Description 4"}, 
		{SEARCH_API_TEST + " - Edits", "Description 5"}
	};
	// @formatter:on
	private static final String API_SEARCH_PATH = "api/search?";
	private static final String BASIC = "basic";
	private static final String MODIFIEDSINCE = "modifiedsince";

	// also used as key search term in the dynamic collections / modified since tests
	public static final String OAUTH_CLIENT_ID = SEARCH_API_TEST + "Client";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
	}

	@DataProvider(name = "ordering")
	public Object[][] getSortData()
	{
		return new Object[][]{{"name", new Integer[]{0, 1, 2, 3, 4}}, {"modified", new Integer[]{3, 4, 2, 1, 0}},
				{"rating", new Integer[]{4, 0, 2, 1, 3}},};
	}

	private JsonNode doSearch(String info, String query, Map<?, ?> otherParams, String token) throws Exception
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
		//params.add(new BasicNameValuePair("token", TokenSecurity.createSecureToken(username, "token", "token", null)));
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + API_SEARCH_PATH + paramString);
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}

	@Test(dataProvider = "ordering")
	public void testOrdering(String order, Integer[] itemIndexes) throws Exception
	{
		String token = requestToken(OAUTH_CLIENT_ID);
		JsonNode resultsNode = doSearch(BASIC, SEARCH_API_TEST,
			ImmutableMap.of("order", order, "collections", "b28f1ffe-2008-4f5e-d559-83c8acd79316"), token);
		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 5);
		assertEquals(resultsNode.get("available").asInt(), 5);
		JsonNode itemsNode = resultsNode.get("results");
		int i = 0;
		for( JsonNode itemNode : itemsNode )
		{
			String[] itemDeets = ITEMS[itemIndexes[i]];
			asserter.assertBasic((ObjectNode) itemNode, itemDeets[0], itemDeets[1]);
			i++;
		}
		resultsNode = doSearch(BASIC, SEARCH_API_TEST,
			ImmutableMap.of("order", order, "collections", "b28f1ffe-2008-4f5e-d559-83c8acd79316", "reverse", "true"),
			token);
		itemsNode = resultsNode.get("results");
		for( JsonNode itemNode : itemsNode )
		{
			i--;
			String[] itemDeets = ITEMS[itemIndexes[i]];
			asserter.assertBasic((ObjectNode) itemNode, itemDeets[0], itemDeets[1]);
		}
	}

	@Test
	public void testStartAndLength() throws Exception
	{
		String token = requestToken(OAUTH_CLIENT_ID);
		JsonNode resultsNode = doSearch(BASIC, SEARCH_API_TEST,
			ImmutableMap.of("order", "name", "start", 1, "length", 2), token);

		assertEquals(resultsNode.get("start").asInt(), 1);
		assertEquals(resultsNode.get("length").asInt(), 2);
		assertEquals(resultsNode.get("available").asInt(), 6);
		JsonNode itemsNode = resultsNode.get("results");
		asserter.assertBasic((ObjectNode) itemsNode.get(0), ITEMS[1][0], ITEMS[1][1]);
		asserter.assertBasic((ObjectNode) itemsNode.get(1), ITEMS[2][0], ITEMS[2][1]);
	}

	@Test
	public void testShowall() throws Exception
	{
		String token = requestToken(OAUTH_CLIENT_ID);
		JsonNode resultsNode = doSearch(BASIC, "\"" + SEARCH_API_TEST + " - Other collection\"",
			ImmutableMap.of("order", "name", "showall", "true"), token);

		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 2);
		assertEquals(resultsNode.get("available").asInt(), 2);
		JsonNode itemsNode = resultsNode.get("results");
		asserter.assertBasic((ObjectNode) itemsNode.get(0), SEARCH_API_TEST + " - Other collection", null);
		asserter.assertBasic((ObjectNode) itemsNode.get(1), SEARCH_API_TEST + " - Other collection draft", null);
	}

	@Test
	public void testWhereClause() throws Exception
	{
		String token = requestToken(OAUTH_CLIENT_ID);
		JsonNode resultsNode = doSearch(
			BASIC,
			null,
			ImmutableMap.of("order", "name", "showall", "true", "where", "/xml/item/name = '" + SEARCH_API_TEST
				+ " - Other collection'"), token);

		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 1);
		assertEquals(resultsNode.get("available").asInt(), 1);
		JsonNode itemsNode = resultsNode.get("results");
		asserter.assertBasic((ObjectNode) itemsNode.get(0), SEARCH_API_TEST + " - Other collection", null);
	}

	/**
	 * The pre-defined dynamic collection 'SearchApiTest' is expected, with 4 items,
	 * 2 modified before 15th March 2013, and 2 after.
	 * @throws Exception
	 */
	@Test
	public void testSearchDynaCollContents() throws Exception
	{
		// retrieve our dynamic Collections, find the one which matches our expectations
		String token = requestToken(OAUTH_CLIENT_ID);
		URI dynaCollQueryUri = new URI(context.getBaseUrl() + "api/dynacollection");

		String dynaCollUuid = confirmDynaCollectionByName(dynaCollQueryUri.toString(), OAUTH_CLIENT_ID, token);
		Map<String, String> otherParams = new HashMap<String, String>();
		otherParams.put("dynacollection", dynaCollUuid);
		// modified since date in yyyy-MM-dd format
		otherParams.put(MODIFIEDSINCE, "1066-10-15");
		JsonNode result = doSearch(BASIC, OAUTH_CLIENT_ID, otherParams, token);
		assertTrue(result.get("available").asInt() == 4);

		// modified since date in yyyy-MM-dd format
		// otherParams.put(MODIFIEDSINCE, "2013-03-15");
		// result = doSearch(BASIC, OAUTH_CLIENT_ID, otherParams, token);
		//
		// assertTrue(result.get("available").asInt() == 2);
		otherParams.clear();
		dynaCollUuid = DynaCollectionApiTest.KNOWN_MANUAL_VIRTUAL_DYNACOLL_UUID + ':' + DynaCollectionApiTest.GIANT;
		otherParams.put("dynacollection", dynaCollUuid);
		result = doSearch(BASIC, "", otherParams, token);
		// we expect 2 'giants'
		assertEquals(result.get("available").asInt(), 2);
	}

	/**
	 * The pre-defined dynamic collection 'SearchApiTestDynamic' is expected,
	 * 2 items with the virtual value 'giant', none with 'average' and 1 with 'miniature'.
	 * @throws Exception
	 */
	@Test
	public void testSearchDynaCollContentsWithVirtualValue() throws Exception
	{
		// retrieve our dynamic Collections, find the one which matches our expectations
		String token = requestToken(OAUTH_CLIENT_ID);
		URI uri = new URI(context.getBaseUrl() + "api/dynacollection");

		String dynaBaseName = OAUTH_CLIENT_ID + "Dynamic";

		String dynaCollUuid = confirmDynaCollectionByName(uri.toString(), dynaBaseName + " (giant)", token);
		Map<String, String> otherParams = new HashMap<String, String>();
		otherParams.put("dynacollection", dynaCollUuid);

		// we expect 2 'giants'
		JsonNode result = doSearch(BASIC, null, otherParams, token);

		assertEquals(result.get("available").asInt(), 2);

		// now look for 'average'
		dynaCollUuid = confirmDynaCollectionByName(uri.toString(), dynaBaseName + " (average)", token);
		otherParams.put("dynacollection", dynaCollUuid);

		// we expect no 'average'
		result = doSearch(BASIC, null, otherParams, token);
		assertEquals(result.get("available").asInt(), 0);

		// now look for 'miniature'
		dynaCollUuid = confirmDynaCollectionByName(uri.toString(), dynaBaseName + " (miniature)", token);
		otherParams.put("dynacollection", dynaCollUuid);

		// we expect 1 'miniature'
		result = doSearch(BASIC, null, otherParams, token);

		assertEquals(result.get("available").asInt(), 1);
	}

	/**
	 * Assert the existence of a named dynamic collection and return the compoundId
	 * @param uriStr
	 * @param target
	 * @param token
	 * @return
	 * @throws Exception
	 */
	private String confirmDynaCollectionByName(String uriStr, String target, String token) throws Exception
	{
		JsonNode resultComposite = getEntity(uriStr, token);
		JsonNode result = resultComposite.get("results");
		String dynaCollCompoundId = null;

		for( Iterator<JsonNode> iter = result.getElements(); iter.hasNext(); )
		{
			JsonNode aDynaColl = iter.next();
			if( target.equalsIgnoreCase(aDynaColl.get("name").asText()) )
			{
				dynaCollCompoundId = aDynaColl.get("compoundId").asText();
			}
		}

		assertNotNull(dynaCollCompoundId, "Test precondition not met: couldn't find dynamic collection named " + target);
		return dynaCollCompoundId;
	}
}
