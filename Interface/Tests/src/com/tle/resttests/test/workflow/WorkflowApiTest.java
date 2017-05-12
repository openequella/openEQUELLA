package com.tle.resttests.test.workflow;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.AclLists;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Schemas;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

public class WorkflowApiTest extends AbstractEntityCreatorTest
{
	private TaskRequests tasks;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder();
		tasks = builder.tasks();
	}

	@Test
	public void testMoveLive() throws Exception
	{
		String schemaUuid = createSimpleSchema();
		String workflowUuid = createSimpleWorkflow(true);
		String collUuid = createSimpleCollection(schemaUuid, workflowUuid);
		ItemId itemId = createSimpleItem(collUuid);
		ObjectNode item = items.get(itemId, "detail");
		asserter.assertStatus(item, "live");
	}

	@Test
	public void testWorkflow() throws Exception
	{
		String schemaUuid = createSimpleSchema();
		String workflowUuid = createSimpleWorkflow();
		String collUuid = createSimpleCollection(schemaUuid, workflowUuid);
		ItemId itemId = createSimpleItem(collUuid);
		ObjectNode item = items.get(itemId, "detail");
		asserter.assertStatus(item, "moderating");

		PropBagEx meta = new PropBagEx();
		meta.setNode("item/name", context.getFullName("An edited item"));
		item.put("metadata", meta.toString());
		items.editNoPermission(item);

		ObjectNode task = findTaskToModerate(itemId);
		String taskUuid = task.get("task").get("uuid").asText();
		items.editId(item, "taskUuid", taskUuid);

		tasks.accept(task, "looks good");

		item = items.get(itemId, "detail");
		asserter.assertStatus(item, "live");

		JsonNode results = workflows.list();
		JsonNode resultsNode = results.get("results");

		List<String> workflows = Lists.newArrayList();
		for( JsonNode result : resultsNode )
		{
			workflows.add(result.get("name").asText());
		}

		assertTrue(workflows.contains("Simple workflow"), "Workflow not found in " + workflows);

		JsonNode historyNode = items.history(itemId);
		List<String> historyEvents = Lists.newArrayList();
		for( JsonNode history : historyNode )
		{
			historyEvents.add(history.get("type").asText());
		}

		List<String> expectEvents = Lists.newArrayList("edit", "statechange", "resetworkflow", "approved",
			"statechange");
		for( String event : expectEvents )
		{
			assertTrue(historyEvents.contains(event), "Item history should contain '" + event + "', list: "
				+ historyEvents);
		}

		// FIXME add some comments
		JsonNode commentsNode = items.comments(itemId);
	}

	private void acceptTaskForItem(ItemId itemId) throws IOException
	{
		ObjectNode task = findTaskToModerate(itemId);
		tasks.accept(task, null);
	}

	@Test
	public void testMetadataUsers() throws IOException
	{
		String schemaUuid = createSimpleSchema();
		ObjectNode workflow = Workflows.json("Metadata user workflow");
		ObjectNode metaTask = Workflows.task("Metadata user task", false);
		metaTask.put("userPath", "/item/moderators");
		Workflows.child(workflow.with("root"), metaTask);
		String workflowUuid = workflows.createId(workflow);
		String collUuid = createSimpleCollection(schemaUuid, workflowUuid);

		ObjectNode item = Items.json(collUuid, "item/name", context.getFullName("An item"),
			"item/moderators", RestTestConstants.USERID_AUTOTEST);
		item = items.create(item, true);
		asserter.assertStatus(item, "moderating");
		ItemId itemId = items.getId(item);
		acceptTaskForItem(itemId);
		item = items.get(itemId, "detail");
		asserter.assertStatus(item, "live");
	}

	private ObjectNode findTaskToModerate(ItemId itemId) throws IOException
	{
		return tasks.findTaskToModerate(itemId, context.getNamePrefix(), null);
	}

	private ItemId createSimpleItem(String collection) throws IOException
	{
		ObjectNode item = Items.json(collection, "item/name", context.getFullName("An item"));
		item = items.create(item, true);
		return items.getId(item);
	}

	private String createSimpleSchema() throws IOException
	{
		ObjectNode schema = Schemas.basicJson("Simple schema");
		return schemas.createId(schema);
	}

	private String createSimpleWorkflow() throws IOException
	{
		return createSimpleWorkflow(false);
	}

	private String createSimpleWorkflow(boolean movelive) throws IOException
	{
		ObjectNode workflow = Workflows.json("Simple workflow");
		workflow.put("moveLive", movelive);
		Workflows.child(workflow.with("root"), Workflows.task("Task", true, RestTestConstants.USERID_AUTOTEST));
		return workflows.createId(workflow);
	}

	private String createSimpleCollection(String schemaUuid, String workflowUuid) throws IOException
	{
		ObjectNode collection = CollectionJson.json("Simple collection", schemaUuid, workflowUuid);
		ArrayNode rules = collection.with("security").putArray("rules");
		rules.add(AclLists.userRule("EDIT_ITEM", false, false, RestTestConstants.USERID_AUTOTEST));
		return collections.createId(collection);
	}
}
