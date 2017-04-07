package com.tle.resttests.test.security;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractEntityApiTest;
import com.tle.resttests.util.RestTestConstants;

public class SecurityTest extends AbstractEntityApiTest
{
	protected String OAUTH_CLIENT_ID = "SecurityTestClient";
	protected String OAUTH_OTHER_USER_CLIENT_ID = "SecurityTestOtherUserClient";
	private String schemaUuid;
	private String collUuid;

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
		clients.add(new Pair<String, String>(OAUTH_OTHER_USER_CLIENT_ID, RestTestConstants.USERID_TOKENUSER));
	}

	@Test
	public void setupCollection() throws Exception
	{
		schemaUuid = createSimpleSchema();
		collUuid = createSimpleCollection(schemaUuid);
	}

	@Test(dependsOnMethods = "setupCollection")
	public void testOwnerSecurity() throws Exception
	{
		String token1 = requestToken(OAUTH_CLIENT_ID);
		String token2 = requestToken(OAUTH_OTHER_USER_CLIENT_ID);

		ItemId itemId = createSimpleItem(collUuid, context.getFullName("An Item"), token1);
		createSimpleItem(collUuid, context.getFullName("An Item"), token2);

		ObjectNode item = createItemJsonWithValues(collUuid, "item/name", context.getFullName("A Changed Item"));
		HttpResponse response = putItem(getItemUri(itemId), item.toString(), token1, "waitforindex", true);
		assertResponse(response, 200, "Should be ok");

		item = createItemJsonWithValues(collUuid, "item/name", context.getFullName("Another Changed Item"));
		response = putItem(getItemUri(itemId), item.toString(), token2, "waitforindex", true);
		assertResponse(response, 403, "Should be denied");

	}

	private ItemId createSimpleItem(String collection, String name, String token) throws IOException
	{
		ObjectNode item = createItemJsonWithValues(collection, "item/name", name);
		item = createItem(item.toString(), token, "waitforindex", true);
		return addDeletable(item);
	}

	private String createSimpleCollection(String schemaUuid) throws IOException
	{
		ObjectNode collection = mapper.createObjectNode();
		collection.put("name", "Simple collection");
		collection.with("schema").put("uuid", schemaUuid);

		ArrayNode rules = collection.with("security").putArray("rules");
		ObjectNode rule = collection.objectNode();
		rule.put("granted", false);
		rule.put("override", true);
		rule.put("privilege", "EDIT_ITEM");
		rule.put("who", "$OWNER NOT");
		rules.add(rule);

		return createCollection(collection);
	}

	private String createSimpleSchema() throws IOException
	{
		ObjectNode schema = mapper.createObjectNode();
		schema.put("namePath", "/item/name");
		schema.put("descriptionPath", "/item/description");
		schema.put("name", "Simple schema");
		schema.with("definition").with("item").with("name").put("_indexed", true);
		schema.with("definition").with("item").with("description").put("_indexed", true);
		return createSchema(schema);
	}

}
