package com.tle.resttests.test.search;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.resttests.AbstractRestApiTest;
import com.tle.resttests.util.RestTestConstants;

public class SearchApiTest extends AbstractRestApiTest
{
	// @formatter:off
	private static final String[][] ITEMS = {
		{"SearchApiTest - Aardvark", "Description 1"},//0
		{"SearchApiTest - Basic", "Description 2"},//1
		{"SearchApiTest - Crabs", "Description 3"},//2
		{"SearchApiTest - Dogs", "Description 4"},//3
		{"SearchApiTest - Edits", "Description 5"}//4
	};
	// @formatter:on

	private static final String OAUTH_CLIENT_ID = "SearchApiTestClient";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
	}

	@DataProvider(name = "ordering")
	public Object[][] getSortData()
	{
		// TODO: Not supported by CreateFromScratchTest
		return new Object[][]{{"name", new Integer[]{0, 1, 2, 3, 4}}, {"modified", new Integer[]{3, 4, 2, 1, 0}},
		/* {"rating", new Integer[]{4, 0, 2, 1, 3}} */};
	}

	@DataProvider(name = "wheres")
	public Object[][] getWhereData()
	{
		Object[][] data;
		Object[] basicClause = new Object[]{"/xml/item/name = 'SearchApiTest - Other collection'",
				Lists.newArrayList("SearchApiTest - Other collection"), "SearchApiTest"};
		Object[] notClause = new Object[]{"/xml/item/name is not 'SearchApiTest - Other collection'",
				Lists.newArrayList("SearchApiTest - Other collection draft"), "\"SearchApiTest - Other collection\""};
		Object[] existsClause = new Object[]{"/xml/item/controls/checkboxes exists",
				Lists.newArrayList("MyResourcesApiTest - Moderating", "TaskApiTest - Unassigned, must moderate"),
				"TaskApiTest OR MyResourcesApiTest"};
		Object[] andClause = new Object[]{
				"/xml/item/name = 'SearchApiTest - Other collection' AND /xml/item/name = 'SearchApiTest - Other collection draft'",
				Lists.newArrayList(), "SearchApiTest"};
		Object[] orClause = new Object[]{
				"/xml/item/name = 'SearchApiTest - Other collection' OR /xml/item/name = 'SearchApiTest - Other collection draft'",
				Lists.newArrayList("SearchApiTest - Other collection", "SearchApiTest - Other collection draft"),
				"SearchApiTest"};
		Object[] dateGTClause = new Object[]{
				"/xml/item/mod_date > '2032-10-28T00:00:00+00:00'",
				Lists.newArrayList("TaskApiTest - High priority, long due date",
					"TaskApiTest - Low priority, long due date"), "TaskApiTest"};
		Object[] dateGTEClause = new Object[]{
				"/xml/item/mod_date >= '2032-10-28T13:00:00+00:00'",
				Lists.newArrayList("TaskApiTest - High priority, long due date",
					"TaskApiTest - Low priority, long due date"), "TaskApiTest"};
		Object[] dateLTClause;
		if( isEquella() )
		{
			//The extra 2 items here have a mod_date = "" which is indeed less than the supplied query 
			//(it doesn't do a date comparison)
			dateLTClause = new Object[]{
					"/xml/item/mod_date < '2025-10-29T00:00:00+00:00'",
					Lists.newArrayList("TaskApiTest - High priority, short due date",
						"TaskApiTest - Low priority, short due date", "TaskApiTest - Task assigned to me",
						"TaskApiTest - Task assigned to someone else"), "TaskApiTest"};
		}
		else
		{
			dateLTClause = new Object[]{
					"/xml/item/mod_date < '2025-10-29T00:00:00+00:00'",
					Lists.newArrayList("TaskApiTest - High priority, short due date",
						"TaskApiTest - Low priority, short due date"), "TaskApiTest"};
		}

		Object[] fromWhereParserTest = new Object[]{
				"(/xml/item/@id = 'blah' AND /xml/item/something = 'blah2')"
					+ " OR (/xml/item/another NOT IN ('v1', 'v2','v3') AND (NOT /xml/item/a LIKE 'part%' AND  /xml/item/@id = 'agg')"
					+ " OR (((((/xml/item/who.cares EXISTS)))) AND /xml/item/null IS NULL))"
					+ " OR (/xml/item/notnull IS NOT NULL AND /xml/item/@id = 'tsf')", Lists.newArrayList(), ""};

		data = new Object[][]{basicClause, notClause, existsClause, andClause, orClause, dateGTClause, dateGTEClause,
				dateLTClause, fromWhereParserTest};
		return data;
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
		// params.add(new BasicNameValuePair("token",
		// TokenSecurity.createSecureToken(username, "token", "token", null)));
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/search?" + paramString);
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}

	@Test(dataProvider = "ordering")
	public void testOrdering(String order, Integer[] itemIndexes) throws Exception
	{
		String token = requestToken(OAUTH_CLIENT_ID);
		JsonNode resultsNode = doSearch("all", "SearchApiTest",
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
		resultsNode = doSearch("basic", "SearchApiTest",
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
		JsonNode resultsNode = doSearch("basic", "SearchApiTest",
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
		JsonNode resultsNode = doSearch("basic", "\"SearchApiTest - Other collection\"",
			ImmutableMap.of("order", "name", "showall", "true"), token);

		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), 2);
		assertEquals(resultsNode.get("available").asInt(), 2);
		JsonNode itemsNode = resultsNode.get("results");
		asserter.assertBasic((ObjectNode) itemsNode.get(0), "SearchApiTest - Other collection", null);
		asserter.assertBasic((ObjectNode) itemsNode.get(1), "SearchApiTest - Other collection draft", null);
	}

	@Test(dataProvider = "wheres")
	public void testWhereClause(String where, List<String> names, String query) throws Exception
	{
		String token = requestToken(OAUTH_CLIENT_ID);
		JsonNode resultsNode = doSearch("basic", query,
			ImmutableMap.of("order", "name", "showall", "true", "where", where), token);

		assertEquals(resultsNode.get("start").asInt(), 0);
		assertEquals(resultsNode.get("length").asInt(), names.size());
		assertEquals(resultsNode.get("available").asInt(), names.size());
		ArrayNode itemsNode = (ArrayNode) resultsNode.get("results");
		for( int i = 0; i < names.size(); i++ )
		{
			asserter.assertBasic((ObjectNode) itemsNode.get(i), names.get(i), null);
		}
	}
}
