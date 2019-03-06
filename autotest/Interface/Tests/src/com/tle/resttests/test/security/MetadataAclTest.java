package com.tle.resttests.test.security;

import java.io.IOException;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Schemas;
import com.tle.json.entity.Users;
import com.tle.json.requests.CollectionRequests;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.SchemaRequests;
import com.tle.json.requests.SearchRequests;
import com.tle.json.requests.UserRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.UserRequestsBuilder;

public class MetadataAclTest extends AbstractEntityCreatorTest
{
	private CollectionRequests collections;
	private SchemaRequests schemas;
	private ItemRequests items;
	private SearchRequests searchAT;
	private SearchRequests searchDY;
	private UserRequests users;

	private String USER;

	private String collectionUuid;
	private String schemaId;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder().user(RestTestConstants.USERID_AUTOTEST);
		collections = builder.collections();
		schemas = builder.schemas();
		items = builder.items();
		searchAT = builder.searches();

		UserRequestsBuilder builder2 = new UserRequestsBuilder(this);
		users = builder2.users();

		USER = users.getId(users.create(Users.json("metaacl", "meta", "acl", "", "equella")));

		searchDY = builder.user(USER).searches();
	}

	@Test
	public void createCollection() throws IOException
	{
		ObjectNode schema = Schemas.json(context.getFullName("schema"), "/item/name", "/item/description");
		ObjectNode item = schema.with("definition").with("xml").with("item");
		item.with("name").put("_indexed", true);
		item.with("description").put("_indexed", true);
		item.with("boolean").put("_indexed", true);

		schemaId = schemas.getId(schemas.create(schema));
		ObjectNode collection = CollectionJson.json(context.getFullName("collection"), schemaId, null);

		ObjectNode metadataRules = collection.with("security").with("metadata");
		metadataRules.put(
			UUID.randomUUID().toString(),
			metadataRule(Files.readFile(getClass().getResourceAsStream("script.js")), "DISCOVER_ITEM", "U:" + USER,
				false));

		collectionUuid = collections.getId(collections.create(collection));
	}

	@Test(dependsOnMethods = "createCollection")
	public void testItemTrue()
	{
		String fullName = context.getFullName("An item true");
		ObjectNode item = Items.json(collectionUuid, "/item/name", fullName, "/item/boolean", "true");
		ItemId itemID = items.getId(items.create(item, 45));
		searchAT.waitForIndex(itemID, "\"" + fullName + "\"");

		ObjectNode searchResults = searchDY.search("\"" + fullName + "\"");
		Assert.assertEquals(searchResults.get("available").asInt(), 0, "Shouldn't have been able to see the item: "
			+ searchResults);
	}

	@Test(dependsOnMethods = "createCollection")
	public void testItemFalse()
	{
		String fullName = context.getFullName("An item false");
		ObjectNode item = Items.json(collectionUuid, "/item/name", fullName, "/item/boolean", "false");
		ItemId itemID = items.getId(items.create(item, 45));
		searchAT.waitForIndex(itemID, "\"" + fullName + "\"");
		searchDY.waitForIndex(itemID, "\"" + fullName + "\"");
	}

	private ObjectNode metadataRule(String script, String priv, String who, boolean grant)
	{
		ObjectNode rule = MAPPER.createObjectNode();
		rule.put("name", UUID.randomUUID().toString());
		rule.put("script", script);
		ArrayNode entries = rule.putArray("entries");
		ObjectNode entry = entries.objectNode();
		entry.put("granted", grant);
		entry.put("privilege", priv);
		entry.put("who", who);
		entries.add(entry);
		return rule;
	}

}
