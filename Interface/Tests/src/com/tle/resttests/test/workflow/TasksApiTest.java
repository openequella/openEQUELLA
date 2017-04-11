package com.tle.resttests.test.workflow;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.ItemId;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.RequestsBuilder;

public class TasksApiTest extends AbstractRestAssuredTest
{
	private TaskRequests tasks;
	private ItemRequests items;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder();
		tasks = builder.tasks();
		items = builder.items();
	}

	private JsonNode doTasksSearch(String query, String subsearch, Map<String, ?> otherParams)
		throws Exception
	{
		RequestSpecification request = tasks.searchRequest(query, subsearch, null, "50");
		request.queryParams(otherParams);
		return tasks.search(request);
	}

	@DataProvider(name = "filters")
	public Object[][] filterProvider()
	{
		// @formatter:off
		return new Object[][] { //The tasks call won't get item names so we need to match on UUID
			// TODO: Not supported by CreateFromScratchTest
			/*{ "assignedme", new String[]{"d7f554ee-d50f-4567-a85e-5e62578d10ed"}, new String[]{"9424a318-0c16-4080-a737-dc141cd46837"} },
			{ "assignedothers", new String[]{"9424a318-0c16-4080-a737-dc141cd46837"}, new String[]{"d7f554ee-d50f-4567-a85e-5e62578d10ed"} },
			{ "assignednone", new String[]{"ed883bc3-0a7a-48c4-acac-4b1e3c038f47", "24b977ec-4df4-4a43-8922-8ca6f82a296a",
				"e48edefa-e1fc-4ebe-946f-ff65938c9fae", "d6518347-366b-46db-8b53-327e7381a1e6", "cc7a3e13-4f44-4481-8bc2-881cecfbed96"}, new String[]{"d7f554ee-d50f-4567-a85e-5e62578d10ed"} },*/
			{ "mustmoderate", new String[]{"ed883bc3-0a7a-48c4-acac-4b1e3c038f47", "24b977ec-4df4-4a43-8922-8ca6f82a296a",
				"e48edefa-e1fc-4ebe-946f-ff65938c9fae", "d6518347-366b-46db-8b53-327e7381a1e6", "cc7a3e13-4f44-4481-8bc2-881cecfbed96"}, new String[]{"d7f554ee-d50f-4567-a85e-5e62578d10ed"}}
		};
		// @formatter:on

	}

	@Test(dataProvider = "filters")
	public void taskSubsearchTest(String filter, String[] includes, String[] excludes) throws Exception
	{
		JsonNode resultsNode = doTasksSearch("TaskApiTest", filter, ImmutableMap.of("order", "name"));

		for( int i = 0; i < includes.length; i++ )
		{
			assertTrue(containsResult(resultsNode, includes[i], "uuid"), "Result " + includes[i] + " not found in "
				+ filter);
		}
		for( int i = 0; i < excludes.length; i++ )
		{
			assertFalse(containsResult(resultsNode, excludes[i], "uuid"), "Result " + excludes[i] + " included in "
				+ filter);
		}
	}

	@Test
	public void taskSortTest() throws Exception
	{
		JsonNode resultsNode = doTasksSearch("TaskApiTest", "all", ImmutableMap.of("order", "name"));
		assertInNameOrder(resultsNode);
		resultsNode = doTasksSearch("TaskApiTest", "all", ImmutableMap.of("order", "priority"));
		assertInPriorityOrder(resultsNode);
		resultsNode = doTasksSearch("TaskApiTest", "all", ImmutableMap.of("order", "duedate"));
		assertInDueDateOrder(resultsNode);
		resultsNode = doTasksSearch("TaskApiTest", "all", ImmutableMap.of("order", "waiting"));
		assertInWaitingOrder(resultsNode);
	}

	@Test
	public void taskFilterTest() throws Exception
	{
		JsonNode resultsNode = doTasksSearch("TaskApiTest", "all",
			ImmutableMap.of("collections", "6bf51943-9d20-4934-a228-8edcec6691ae"));
		// Complex workflow collection
		assertTrue(containsResult(resultsNode, "24b977ec-4df4-4a43-8922-8ca6f82a296a", "uuid"));
		assertTrue(containsResult(resultsNode, "cc7a3e13-4f44-4481-8bc2-881cecfbed96", "uuid"));
		assertFalse(containsResult(resultsNode, "9424a318-0c16-4080-a737-dc141cd46837", "uuid"));
	}

	private void assertInNameOrder(JsonNode jsonNode) throws Exception
	{
		String first = "aaa"; // It'll do

		JsonNode results = jsonNode.get("results");
		assertTrue(results.size() > 1, "Must have at least 2 results");
		for( JsonNode result : results )
		{
			JsonNode item = result.get("item");
			String name = getName(item.get("uuid").asText(), item.get("version").asInt());
			assertTrue(name.compareToIgnoreCase(first) >= 0);
			first = name;
		}
	}

	private void assertInPriorityOrder(JsonNode jsonNode) throws Exception
	{
		int first = 90000; // It'll do

		JsonNode results = jsonNode.get("results");
		assertTrue(results.size() > 1, "Must have at least 2 results");
		for( JsonNode result : results )
		{
			int priority = result.get("task").get("priority").asInt();
			assertTrue(priority <= first);
			first = priority;
		}
	}

	private void assertInDueDateOrder(JsonNode jsonNode) throws Exception
	{
		Date first = new Date(-3177517600000l); // 1869
		JsonNode results = jsonNode.get("results");
		assertTrue(results.size() > 1, "Must have at least 2 results");
		for( JsonNode result : results )
		{
			JsonNode dateNode = result.get("dueDate");
			if( dateNode == null )
			{
				first = new Date(2298153600000l); // 2042
				continue;
			}
			Date due = ISO8601Utils.parse(dateNode.asText());
			assertTrue(due.after(first) || due.equals(first), "Results were not sorted by due date: " + first
				+ " before " + due);
			first = due;
		}
	}

	private void assertInWaitingOrder(JsonNode jsonNode) throws Exception
	{
		Date first = new Date(0l); // 1970

		JsonNode results = jsonNode.get("results");
		assertTrue(results.size() > 1, "Must have at least 2 results");
		for( JsonNode result : results )
		{
			JsonNode dateNode = result.get("startDate");
			Date started = ISO8601Utils.parse(dateNode.asText());
			assertTrue(started.after(first) || started.equals(first), "Results were not sorted by waiting date");
			first = started;
		}
	}

	private String getName(String uuid, int version) throws Exception
	{
		ObjectNode item = items.get(new ItemId(uuid, version));
		return item.get("name").asText();
	}

	// A bit crude, but should do the trick
	private boolean containsResult(JsonNode json, String lookFor, String lookIn)
	{
		ArrayNode results = (ArrayNode) json.get("results");

		for( JsonNode result : results )
		{
			if( result.get("item").get(lookIn).asText().equals(lookFor) )
			{
				return true;
			}
		}

		return false;
	}
}
