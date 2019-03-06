package com.tle.resttests.test.item;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Schemas;
import com.tle.json.requests.SearchRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

@Test(groups = "eps")
public class MoveItemTest extends AbstractEntityCreatorTest
{
	private static String collectionID = "";
	private static String schemaId = "";

	private TaskRequests tasks;
	private SearchRequests search;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder();
		tasks = builder.user(RestTestConstants.USERID_AUTOTEST).tasks();
		search = builder.user(RestTestConstants.USERID_AUTOTEST).searches();
		ObjectNode schema = Schemas.json("Very simple schema", "/name", "/description");
		schema.with("definition").with("xml").with("name").put("_indexed", true).put("_field", true)
			.put("_type", "text");
		schema.with("definition").with("xml").with("description").put("_indexed", true).put("_field", true)
			.put("_type", "text");
		schemaId = schemas.getId(schemas.create(schema));
		collectionID = collections.getId(collections.create(CollectionJson.json("Very simple collection", schemaId,
			null)));
	}

	@Test
	public void moveItem()
	{
		ObjectNode item = Items.json(RestTestConstants.COLLECTION_BASIC);
		item.put("metadata", "<xml><item><name>Item for moving</name></item><name>newName</name></xml>");
		ItemId id = items.getId(items.create(item));

		item.put("collection", collectionID);
		item.put("uuid", id.getUuid());
		item.put("version", 1);

		items.editId(item);
		item = items.get(id);
		String collection = item.get("collection").get("uuid").asText();
		String name = item.get("name").asText();
		Assert.assertEquals(collection, collectionID);
		Assert.assertEquals(name, "newName", "Name field was not updated for new schema");
	}

	// Test that the privileges are properly checked when moving (MOVE_ITEM on
	// the source and CREATE_ITEM on the destination)
	@Test
	public void testSecurity()
	{
		ObjectNode collection = CollectionJson.json("Moveprivs", schemaId, null);
		collection.with("security").putArray("rules").addObject().put("who", "R:TLE_LOGGED_IN_USER_ROLE ")
			.put("override", false).put("granted", false).put("privilege", "CREATE_ITEM");
		collection = collections.create(collection);
		String newCollection = collections.getId(collection);

		ObjectNode item = items.create(Items.jsonXml(collectionID,
			"<xml><name>MoveItemTest - SecurityTest</name></xml>"));
		ItemId itemId = items.getId(item);

		item.put("collection", newCollection);
		items.editRequest(items.accessDeniedRequest(), item);

		ObjectNode revokeNode = (ObjectNode) collection.with("security").withArray("rules").get(0);
		revokeNode.put("privilege", "MOVE_ITEM");
		collections.editId(collection);
		items.editId(item);

		item = items.get(itemId);
		Assert.assertEquals(item.get("collection").get("uuid").asText(), newCollection);

		item.put("collection", collectionID);
		items.editRequest(items.accessDeniedRequest(), item);

		revokeNode.put("privilege", "CREATE_ITEM");
		collections.editId(collection);
		items.editId(item);
	}

	// Make sure the moderation state is properly reset when changing
	// collections
	@Test
	public void testToAndFromModeration()
	{
		ObjectNode item = items.create(Items.jsonXml(RestTestConstants.COLLECTION_NOTIFICATIONS,
			"<xml><name>MoveItemTest - TestModeration</name></xml>"));
		ItemId id = items.getId(item);

		Assert.assertEquals(item.get("status").asText(), "moderating");
		ObjectNode moderation = items.moderation(id);
		Assert.assertEquals(moderation.get("nodes").get("status").asText(), "incomplete");
		Assert.assertEquals(moderation.get("nodes").get("children").get(0).get("status").asText(), "incomplete");
		tasks.accept(id, "806c8b72-d53b-49e1-8cf1-60b474d0f0ec", null);
		moderation = items.moderation(id);
		Assert.assertEquals(moderation.get("status").asText(), "live");

		item.put("collection", collectionID);
		items.editId(item);
		item = items.get(id);

		// Apparently the status should stay live
		Assert.assertEquals(item.get("status").asText(), "live");
		moderation = items.moderation(id);
		Assert.assertNull(moderation.get("nodes"));

		item.put("collection", RestTestConstants.COLLECTION_NOTIFICATIONS);
		items.editId(item);
		item = items.get(id);

		Assert.assertEquals(item.get("status").asText(), "live");
	}

	// Tests that the workflow gets reset when moving to a new workflow, but
	// stays at the same task when moving to a new collection with the same
	// workflow
	@Test
	public void testWorflowReset()
	{
		ObjectNode collection = CollectionJson.json("MoveItemTest - TestWorlfowReset", schemaId,
			RestTestConstants.WORKLFOW_COMPLEX);
		String complexWFCollection = collections.getId(collections.create(collection));
		ObjectNode item = items.create(Items.jsonXml(complexWFCollection,
			"<xml><name>MoveItemTest - TestModeration</name></xml>"));
		ItemId id = items.getId(item);

		ObjectNode moderation = items.moderation(id);
		Assert.assertEquals(moderation.get("nodes").get("children").get(0).get("status").asText(), "incomplete");
		Assert.assertEquals(moderation.get("nodes").get("children").get(1).get("status").asText(), "waiting");

		tasks.accept(id, "3c4926a7-4b28-4ea2-88b8-42da1902a9ed", null);
		moderation = items.moderation(id);
		Assert.assertEquals(moderation.get("nodes").get("children").get(0).get("status").asText(), "complete");
		Assert.assertEquals(moderation.get("nodes").get("children").get(1).get("status").asText(), "incomplete");

		item.put("collection", RestTestConstants.COLLECTION_COMPLEX_WORKFLOW);
		items.editId(item);
		item = items.get(id);

		moderation = items.moderation(id);
		Assert.assertEquals(moderation.get("nodes").get("children").get(0).get("status").asText(), "complete");
		Assert.assertEquals(moderation.get("nodes").get("children").get(1).get("status").asText(), "incomplete");

		item.put("collection", RestTestConstants.COLLECTION_NOTIFICATIONS);
		items.editId(item);
		item = items.get(id);

		moderation = items.moderation(id);
		Assert.assertEquals(moderation.get("nodes").get("children").get(0).get("status").asText(), "incomplete");
		Assert.assertNull(moderation.get("nodes").get("children").get(1));
	}

	// Tests that both indexed and non-indexed security is inherited from the
	// new collection
	@Test
	public void testSecurityInheritence()
	{
		ObjectNode collection = CollectionJson.json("Fewprivs", schemaId, null);
		ArrayNode rules = collection.with("security").putArray("rules");
		rules.addObject().put("who", "R:TLE_LOGGED_IN_USER_ROLE ").put("override", false).put("granted", false)
			.put("privilege", "VIEW_ITEM");
		rules.addObject().put("who", "R:TLE_LOGGED_IN_USER_ROLE ").put("override", false).put("granted", false)
			.put("privilege", "DISCOVER_ITEM");
		String newCollection = collections.getId(collections.create(collection));

		ObjectNode item = items
			.create(Items
				.jsonXml(
					collectionID,
					"<xml><name>MoveItemTest - TestSecurityInheritence</name><item><name>MoveItemTest - TestSecurityInheritence</name></item></xml>"));
		ItemId id = items.getId(item);

		search.waitForIndex(id, "\"MoveItemTest - TestSecurityInheritence\""); // DISCOVER_ITEM
		items.get(id); // VIEW_ITEM

		item.put("collection", newCollection);
		items.editId(item);

		search.waitForNotIndexed(id, "\"MoveItemTest - TestSecurityInheritence\""); // DISCOVER_ITEM
		items.getResponse(items.accessDeniedRequest(), id); // VIEW_ITEM
	}

	// Test that dynamic metadata rules are properly applied in the new
	// collection
	@Test
	public void testDynamicSecurity()
	{
		ObjectNode collection = CollectionJson.json(context.getFullName("collection"), schemaId, null);
		ArrayNode dynamicRules = collection.with("security").putArray("dynamicRules");
		dynamicRules.add(dynamicRule("U", "/userToRevoke", false, "DISCOVER_ITEM", "DELETE_ITEM"));
		String collectionUuid = collections.getId(collections.create(collection));

		ObjectNode item = items
			.create(Items
				.jsonXml(
					collectionID,
					"<xml><name>MoveItemTest - TestDynamicSecurity</name><item><name>MoveItemTest - TestDynamicSecurity</name></item><userToRevoke>"
						+ RestTestConstants.USERID_AUTOTEST + "</userToRevoke></xml>"));
		ItemId id = items.getId(item);

		search.waitForIndex(id, "\"MoveItemTest - TestDynamicSecurity\"");

		item.put("collection", collectionUuid);
		items.editId(item);

		items.delete(items.accessDeniedRequest(), id);
		search.waitForNotIndexed(id, "\"MoveItemTest - TestDynamicSecurity\"");
	}

	private ObjectNode dynamicRule(String type, String path, boolean grant, String... privs)
	{
		ObjectNode rule = MAPPER.createObjectNode();
		rule.put("name", UUID.randomUUID().toString());
		rule.put("path", path);
		rule.put("type", type);
		ArrayNode targetList = rule.putArray("targetList");

		for( String priv : privs )
		{
			targetList.addObject().put("granted", grant).put("privilege", priv);
		}
		return rule;
	}
}
