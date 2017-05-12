package com.tle.webtests.test.webservices.rest;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.tle.common.Pair;

public class ItemApiLockTest extends AbstractItemApiTest
{
	private static final String OAUTH_CLIENT_ID = "ItemApiLockTestClient";
	private String token;
	private String itemUuid;
	private int itemVersion;
	private String itemUri;

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
	}

	private HttpResponse lockItemError(String token, String itemUri) throws IOException
	{
		HttpPost lockPost = new HttpPost(itemUri + "lock");
		return execute(lockPost, true, token);
	}

	private JsonNode lockItem(String token, String itemUri) throws IOException
	{
		HttpPost lockPost = new HttpPost(itemUri + "lock");
		HttpResponse response = execute(lockPost, false, token);
		assertResponse(response, 201, "Should have locked item");
		return mapper.readTree(response.getEntity().getContent());
	}

	private void createLockItem() throws Exception
	{
		token = requestToken(OAUTH_CLIENT_ID);
		ObjectNode node = mapper.createObjectNode();
		node.with("collection").put("uuid", "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c");
		HttpResponse response = postItem(node.toString(), token);
		assertResponse(response, 201, "Expected item to be created");
		itemUri = response.getFirstHeader("Location").getValue();
		ObjectNode newItem = getItem(itemUri, null, token);
		itemUuid = newItem.get("uuid").asText();
		itemVersion = newItem.get("version").asInt();
	}

	@AfterMethod
	public void cleanupItem() throws IOException
	{
		deleteItem(itemUuid, itemVersion, token, "purge", true);
	}

	@Test
	public void edit() throws Exception
	{
		createLockItem();

		ObjectNode itemJson = getItem(itemUri, null, token);
		HttpResponse response = putItem(itemUri, itemJson.toString(), token, true);
		assertResponse(response, 200, "Should be editable");

		JsonNode lockJson = lockItem(token, itemUri);
		String lockId = lockJson.get("uuid").asText();

		response = putItem(itemUri, itemJson.toString(), token, true);
		assertResponse(response, 409, "Should not be able to edit without supplying lock");

		response = putItem(itemUri, itemJson.toString(), token, true, "lock", lockId);
		assertResponse(response, 200, "Should be editable with lock");

		lockJson = lockItem(token, itemUri);
		lockId = lockJson.get("uuid").asText();

		assertResponse(lockItemError(token, itemUri), 409, "Shouldn't be able to lock again");
	}
}
