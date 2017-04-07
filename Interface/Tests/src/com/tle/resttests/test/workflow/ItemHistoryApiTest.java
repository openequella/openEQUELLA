package com.tle.resttests.test.workflow;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

public class ItemHistoryApiTest extends AbstractEntityCreatorTest
{
	private TaskRequests tasks;
	private TaskRequests tasksMod;
	private ItemRequests itemsGuest;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		itemsGuest = builder().items();
		RequestsBuilder builder = builder().user(RestTestConstants.USERID_AUTOTEST);
		tasks = builder.tasks();
		items = builder.items();
		workflows = builder.workflows();
		collections = builder.collections();
		tasksMod = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
	}

	@Test
	public void testHistory() throws Exception
	{
		ObjectNode workflow = Workflows.json("History workflow");
		String step1 = UUID.randomUUID().toString();
		String step2 = UUID.randomUUID().toString();
		ObjectNode task1 = Workflows.task(step1, "Step 1", false, RestTestConstants.USERID_AUTOTEST);
		task1.put("rejectPoint", true);
		ObjectNode task2 = Workflows.task(step2, "Step 2", false, RestTestConstants.USERID_MODERATOR1);
		Workflows.rootChild(workflow, task1);
		Workflows.rootChild(workflow, task2);

		String workflowUuid = workflows.createId(workflow);
		String collUuid = collections.createId(CollectionJson.json("Item history test",
			RestTestConstants.SCHEMA_BASIC, workflowUuid));

		ItemId itemId = createSimpleItem(collUuid);
		ObjectNode item = items.get(itemId, "detail");
		asserter.assertStatus(item, "moderating");

		ObjectNode task = tasks.findTaskToModerate(itemId, context.getNamePrefix(), null);
		tasks.accept(task, "looks good");
		task = tasksMod.findTaskToModerate(itemId, context.getNamePrefix(), null);
		tasksMod.reject(task, "meh", step1);

		JsonNode historyNode = items.history(itemId);
		
		Date date = new Date(0);
		
		// Make sure they are in date order
		for( JsonNode history : historyNode )
		{
			Date thisDate = ISO8601Utils.parse(history.get("date").asText());
			Assert.assertTrue(date.before(thisDate) || date.equals(thisDate));
			date = thisDate;
		}

		assertHistory(historyNode.get(0), "edit", RestTestConstants.USERID_AUTOTEST, "draft", null, null, null, null,
			null);
		assertHistory(historyNode.get(1), "statechange", RestTestConstants.USERID_AUTOTEST, "moderating", null, null,
			null, null, null);
		assertHistory(historyNode.get(2), "resetworkflow", RestTestConstants.USERID_AUTOTEST, "moderating", null, null,
			"start", null, null);
		assertHistory(historyNode.get(3), "approved", RestTestConstants.USERID_AUTOTEST, "moderating", "looks good",
			step1, "Step 1", null, null);
		assertHistory(historyNode.get(4), "rejected", RestTestConstants.USERID_MODERATOR1, "moderating", "meh", step2,
			"Step 2", step1, "Step 1");
	}

	@Test
	public void testSecurity() throws Exception
	{
		ItemId itemId = createSimpleItem(RestTestConstants.COLLECTION_BASIC);
		items.history(itemId);
		itemsGuest.history(itemsGuest.accessDeniedRequest(), itemId);
	}

	private void assertHistory(JsonNode history, String type, String userId, String state, String comment, String step,
		String stepname, String toStep, String toStepName)
	{
		Assert.assertEquals(history.get("type").asText(), type);
		Assert.assertEquals(userId, history.get("user").get("id").asText());
		Assert.assertEquals(state, history.get("state").asText());

		if( step != null )
		{
			Assert.assertEquals(step, history.get("step").asText());
		}
		if( toStep != null )
		{
			Assert.assertEquals(toStep, history.get("toStep").asText());
		}
		if( stepname != null )
		{
			Assert.assertEquals(stepname, history.get("stepName").asText());
		}
		if( toStepName != null )
		{
			Assert.assertEquals(toStepName, history.get("toStepName").asText());
		}
		if( comment != null )
		{
			Assert.assertEquals(comment, history.get("comment").asText());
		}
	}

	private ItemId createSimpleItem(String collection) throws IOException
	{
		ObjectNode item = Items.json(collection, "item/name", context.getFullName("An item"));
		item = items.create(item, true);
		return items.getId(item);
	}

	@Override
	public String getDefaultUser()
	{
		// Guest
		return null;
	}
}
