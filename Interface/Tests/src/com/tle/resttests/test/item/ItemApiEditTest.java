package com.tle.resttests.test.item;

import static com.tle.resttests.util.RestTestConstants.USERID_AUTOTEST;
import static com.tle.resttests.util.RestTestConstants.USERID_RESTNOPRIV;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.files.Attachments;

public class ItemApiEditTest extends AbstractItemApiTest
{
	// private static final String DRMitemId =
	// "4de9dc1e-9740-4d6a-b848-cf5712882ce6";
	// private static final String itemId =
	// "2f6e9be8-897d-45f1-98ea-7aa31b449c0e";

	private static final String DESCRIPTION = "This is a description";

	// private static final String ITEM_UUID =
	// "13e7a560-6755-11e1-b733-bc0a4924019b";

	private static final String OAUTH_OTHER_CLIENT_ID = "IAETOtherClient";
	private static final String OAUTH_NOPRIVS_CLIENT_ID = "IAETestNoPrivsClient";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>("IAETClient", USERID_AUTOTEST));
		clients.add(new Pair<String, String>(OAUTH_OTHER_CLIENT_ID, RestTestConstants.USERID_TOKENUSER));
		clients.add(new Pair<String, String>(OAUTH_NOPRIVS_CLIENT_ID, RestTestConstants.USERID_RESTNOPRIV));
	}

	private ItemId createItem(String token) throws Exception
	{
		String[] stagingParams = createStaging(token);
		String stagingUuid = stagingParams[0];
		String stagingDirUrl = stagingParams[1];
		uploadFile(stagingDirUrl, "avatar.png", Attachments.get("avatar.png"));
		uploadFile(stagingDirUrl + "/_mypages/78a2de74-96de-48de-8865-22856c22ae49", "page.html",
			Attachments.get("page.html"));
		uploadFile(stagingDirUrl + "/_IMS", "The vile vendor go figure.zip",
			Attachments.get("The vile vendor go figure.zip"), "unzipto", "The vile vendor go figure.zip");

		ObjectNode item = createItemFromFile("newitem.json", token, stagingUuid, 201, false);
		return addDeletable(item);
	}

	private ObjectNode createItemFromString(String newItemJson, String token, String stagingUuid, int responseCode,
		boolean returnError) throws Exception
	{
		if( !returnError )
		{
			if( stagingUuid != null )
			{
				return createItem(newItemJson, token, "file", stagingUuid);
			}
			else
			{
				return createItem(newItemJson, token);
			}
		}
		else
		{
			if( stagingUuid != null )
			{
				return postItemError(newItemJson, token, responseCode, "file", stagingUuid);
			}
			else
			{
				return postItemError(newItemJson, token, responseCode);
			}
		}

	}

	private ObjectNode createItemFromFile(String jsonFile, String token, String stagingUuid, int responseCode,
		boolean returnError) throws Exception
	{
		final StringWriter sw = new StringWriter();
		final InputStream input = ItemApiEditTest.class.getResourceAsStream(jsonFile);
		try
		{
			CharStreams.copy(new InputStreamReader(input), sw);
		}
		finally
		{
			Closeables.close(input, true);
		}
		String newItemJson = sw.toString();

		return createItemFromString(newItemJson, token, stagingUuid, responseCode, returnError);

	}

	@Test
	public void testEditMetadata() throws Exception
	{
		// get an item
		final String token = getToken();
		ItemId itemId = createItem(token);
		ObjectNode item = getItem(itemId, "metadata", token);

		// modifiy the value in the name xpath
		final String xml = item.get("metadata").asText();
		final PropBagEx meta = new PropBagEx(xml);
		meta.setNode("item/name", "New name");
		item.put("metadata", meta.toString());

		// save it
		putItem(itemId, item.toString(), token);

		// get it again, ensure the *name* has been modified as well as the XML
		item = getItem(itemId, "basic", token);

		asserter.assertBasic(item, "New name", DESCRIPTION);
	}

	@Test
	public void testEditOwner() throws Exception
	{
		// create item
		final String token = getToken();
		ItemId itemId = createItem(token);
		// get item
		ObjectNode item = getItem(itemId, "detail", token);
		asserter.assertUser(item.get("owner"), RestTestConstants.USERID_AUTOTEST);

		// modifiy the value in the name xpath
		// final String oldOwner = item.get("owner").asText();
		final String newOwner = RestTestConstants.USERID_TOKENUSER;
		item.put("owner", newOwner);

		// save it
		putItem(itemId, item.toString(), token);

		// get it again, ensure the *name* has been modified as well as the XML
		item = getItem(itemId, "detail", token);

		asserter.assertUser(item.get("owner"), newOwner);
	}

	@Test
	public void testEditDRM() throws Exception
	{
		// FIXME can't test DRM
		// final String token = getToken();
		// createDrmItem(token);
		// // get item
		// ObjectNode item = getItem(itemId, "drm,basic", token);
		//
		// String itemName = item.get("name").textValue();
		// String itemUuid = item.get("uuid").textValue();
		// int itemVersion = item.get("version").intValue();
		//
		// logon("AutoTest", "automated");
		// context.getDriver().get(context.getBaseUrl() + "items/" + itemUuid +
		// "/" + itemVersion);
		// assertTrue(new
		// DRMAgreementPage(context).get().hasTerms("Verifiable use statement"));
		//
		// // modify some of the DRM settings and save it
		// ObjectNode drmNode = (ObjectNode) item.get("drm").get("options");
		// drmNode.put("termsOfAgreement", "A different verifiable statement");
		// putItem(itemId, item.toString(), token);
		//
		// // check DRM has changed
		// logon("AutoTest", "automated");
		// context.getDriver().get(context.getBaseUrl() + "items/" + itemUuid +
		// "/" + itemVersion);
		// assertTrue(new
		// DRMAgreementPage(context).get().hasTerms("A different verifiable statement"));
		//
		// // remove drm completely
		// item.put("drm", item.objectNode());
		//
		// // save it
		// putItem(itemId, item.toString(), token);
		//
		// // get it, check DRM has changed
		// item = getItem(itemId, "drm", token);
		//
		// logon("AutoTest", "automated");
		// context.getDriver().get(context.getBaseUrl() + "items/" + itemUuid +
		// "/" + itemVersion);
		// assertEquals(new SummaryPage(context).get().getItemTitle(),
		// itemName);
	}

	@Test
	public void testEditNavigation() throws Exception
	{
		final String token = getToken();
		ItemId itemId = createItem(token);
		// get item
		ObjectNode item = getItem(itemId, "navigation", token);

		ObjectNode nav = (ObjectNode) item.get("navigation");
		assertFalse(nav.get("hideUnreferencedAttachments").asBoolean());
		ArrayNode nodes = (ArrayNode) nav.get("nodes");
		assertFirstNavNode(nodes, "avatar.png");

		// modify navigation
		ObjectNode node = (ObjectNode) nodes.get(0);
		node.put("name", "An awesome image");
		nav.put("hideUnreferencedAttachments", true);
		putItem(itemId, item.toString(), token);

		// get item again
		nav = (ObjectNode) getItem(itemId, "all", token).get("navigation");
		JsonNode newNodes = nav.get("nodes");
		assertTrue(nav.get("hideUnreferencedAttachments").asBoolean());
		assertFirstNavNode(newNodes, "An awesome image");

	}

	@Test
	public void testEditAttachments() throws Exception
	{
		final String token = getToken();
		ItemId itemId = createItem(token);
		// get item
		ObjectNode item = getItem(itemId, "attachment", token);
		ArrayNode attachments = (ArrayNode) item.get("attachments");
		ObjectNode urlAttachment = (ObjectNode) attachments.get(2);
		assertUrlAttachment(urlAttachment, itemId);
		asserter.assertViewerAndPreview(urlAttachment, null, false);

		// modify it
		urlAttachment.put("viewer", "file");
		urlAttachment.put("preview", true);
		urlAttachment.put("description", "Yahoo");
		urlAttachment.put("url", "http://www.yahoo.com");

		// save it
		putItem(itemId, item.toString(), token);

		// get it again
		urlAttachment = (ObjectNode) getItem(itemId, "attachment", token).get("attachments").get(2);

		assertUrlAttachment(urlAttachment, itemId, "Yahoo", "http://www.yahoo.com");
		asserter.assertViewerAndPreview(urlAttachment, "file", true);
	}

	@Test
	public void testEditEverything() throws Exception
	{
		final String token = getToken();
		ItemId itemId = createItem(token);
		// get item
		ObjectNode item = getItem(itemId, "all", token);

		// check all nodes
		assertNameVersionStatus(item, "ItemApiEditTest - All attachments from JSON", 1, "live");
		assertMetadata(item, "item/name", "ItemApiEditTest - All attachments from JSON");
		asserter.assertUser(item.get("owner"), USERID_AUTOTEST);
		asserter.assertUser(item.get("collaborators").get(0), USERID_RESTNOPRIV);
		asserter.assertCollection(item.get("collection"), COLLECTION_ATTACHMENTS);
		assertUrlAttachment(item.get("attachments").get(2), itemId);
		assertFirstNavNode(item.get("navigation").get("nodes"), "avatar.png");

		// modify metadata
		PropBagEx metadata = new PropBagEx(item.get("metadata").textValue());
		metadata.setNode("item/name", "ItemApiEditTest - All attachments from JSON - Edited");
		item.put("metadata", metadata.toString());

		// modify owner
		ObjectNode owner = item.objectNode();
		owner.put("id", USERID_RESTNOPRIV);
		item.put("owner", owner);

		// modify collaborators
		ArrayNode collaborators = item.arrayNode();
		ObjectNode collaborator = item.objectNode();
		collaborator.put("id", USERID_AUTOTEST);
		collaborators.add(collaborator);
		item.put("collaborators", collaborators);

		// Future enhancement
		// modify collection
		// ObjectNode collection = item.objectNode();
		// collection.put("uuid", PERMISSIONS_COLLECTION);
		// item.put("collection", collection);

		// modify attachments
		ObjectNode attachment = (ObjectNode) item.get("attachments").get(2);
		attachment.put("description", "Yahoo");
		attachment.put("url", "http://www.yahoo.com");

		// modify navigation
		ObjectNode node = (ObjectNode) item.get("navigation").get("nodes").get(0);
		node.put("name", "An awesome image");

		// save
		HttpResponse response = putItem(itemId, item.toString(), token);
		assertResponse(response, 200, "Save should work");

		// check that its correct
		item = getItem(itemId, "all", token);
		assertNameVersionStatus(item, "ItemApiEditTest - All attachments from JSON - Edited", 1, "live");
		assertMetadata(item, "item/name", "ItemApiEditTest - All attachments from JSON - Edited");
		asserter.assertUser(item.get("owner"), USERID_RESTNOPRIV);
		asserter.assertUser(item.get("collaborators").get(0), USERID_AUTOTEST);

		// Future enhancement
		// asserter.assertCollection(item.get("collection"),
		// PERMISSIONS_COLLECTION);

		assertUrlAttachment(item.get("attachments").get(2), itemId, "Yahoo", "http://www.yahoo.com");
		assertFirstNavNode(item.get("navigation").get("nodes"), "An awesome image");

	}

	@Test
	public void testEditNoPrivs() throws Exception
	{
		PropBagEx metadata = new PropBagEx(
			"<xml><item><name>ItemApiEditTest - No Permissions from JSON</name><description>Description</description><controls></controls></item></xml>");
		PropBagEx permissions = metadata.aquireSubtree("item/controls");

		final String token = getToken();

		ObjectNode item = createItemObject(COLLECTION_PERMISSIONS, metadata, false);

		ItemId itemId = addDeletable(createItem(item.toString(), token));

		item = getItem(itemId, "all", token);
		assertNameVersionStatus(item, "ItemApiEditTest - No Permissions from JSON", 1, "live");

		// revoke change owner
		permissions.setNode("checkboxes", "REASSIGN_OWNERSHIP_ITEM");
		item.put("metadata", metadata.toString());
		HttpResponse response = putItem(itemId, item.toString(), token);
		assertResponse(response, 200, "200 not returned from item editing");

		item.put("owner", USERID_RESTNOPRIV);
		ObjectNode error = putItemError(getItemUri(itemId), item.toString(), token, 403);
		assertError(error, 403, "Forbidden",
			"Privilege REASSIGN_OWNERSHIP_ITEM is required to perform the requested operation");
		item = getItem(itemId, "all", token);
		ArrayNode collabs = item.arrayNode();
		collabs.add(USERID_RESTNOPRIV);
		collabs.add(USERID_AUTOTEST);
		item.put("collaborators", collabs);
		error = putItemError(getItemUri(itemId), item.toString(), token, 403);
		assertError(error, 403, "Forbidden",
			"Privilege REASSIGN_OWNERSHIP_ITEM is required to perform the requested operation");
		item = getItem(itemId, "all", token);

		// revoke edit
		permissions.setNode("checkboxes", "EDIT_ITEM");
		item.put("metadata", metadata.toString());
		response = putItem(itemId, item.toString(), token);
		assertResponse(response, 200, "200 not returned from item editing");

		// try to change the name
		PropBagEx editNameMetadata = new PropBagEx(metadata.toString());
		editNameMetadata.setNode("item/name", "ItemApiEditTest - No Permissions from JSON - Edited");
		item.put("metadata", editNameMetadata.toString());
		error = putItemError(getItemUri(itemId), item.toString(), token, 403);
		assertError(error, 403, "Forbidden", "Privilege EDIT_ITEM is required to perform the requested operation");

		// try to edit drm
		item.put("metadata", metadata.toString());
		item.with("drm").with("options").put("termsOfAgreement", "Terms");
		error = putItemError(getItemUri(itemId), item.toString(), token, 403);
		assertError(error, 403, "Forbidden", "Privilege EDIT_ITEM is required to perform the requested operation");

		item = getItem(itemId, "all", token);
		assertNameVersionStatus(item, "ItemApiEditTest - No Permissions from JSON", 1, "live");
	}

	@Test
	public void testPOSTToExisting() throws Exception
	{
		final String token = getToken();

		// Create the item
		ItemId itemId = createItem(token);

		// POST to existing item URL

		ObjectNode error = createItemFromString(getItem(itemId, "all", token).toString(), token, null, 400, true);

		// ensure error
		assertError(error, 400, "Bad Request", "Version 1 of the item '" + itemId.getUuid() + "' already exists");

	}

	@Test
	public void testPUTToNonExisting() throws Exception
	{
		final String token = getToken();

		// Create the item
		ItemId itemId = createItem(token);

		ObjectNode item = getItem(itemId, "basic", token);

		// ensure saving works
		HttpResponse response = putItem(itemId, item.toString(), token);
		assertResponse(response, 200, "Should be editable");

		// Try to save the item to the wrong uuid
		response = putItem("aaaaaaaa-6755-11e1-b733-bc0a4924019b", 1, item.toString(), token);
		assertResponse(response, 404, "Cant edit a non-existing item");

	}

	@Test
	public void testOnePermissionAllowedButNotTheOther() throws Exception
	{
		final String token = getToken();

		PropBagEx metadata = new PropBagEx(
			"<xml><item><name>ItemApiEditTest - Permissions from JSON</name><description>Description</description><controls></controls></item></xml>");
		PropBagEx permissions = metadata.aquireSubtree("item/controls");
		ObjectNode item = createItemObject(COLLECTION_PERMISSIONS, metadata, true);

		ItemId itemId = addDeletable(createItem(item.toString(), token));

		item = getItem(itemId, "all", token);
		assertTrue(item.has("attachments"));
		assertEquals(item.get("attachments").get(0).get("url").textValue(), "http://google.com.au/");

		// revoke view item
		permissions.createNode("checkboxes", "VIEW_ITEM");
		item.put("metadata", metadata.toString());
		HttpResponse response = putItem(itemId, item.toString(), token, "waitforindex", true);
		assertResponse(response, 200, "200 not returned from item creation");

		// make sure we cant see the attachments
		item = getItem(itemId, "all", token);
		assertFalse(item.has("attachments"));

		// ensure discoverable
		JsonNode searchResults = doSearch("ItemApiEditTest - Permissions from JSON", token);
		assertEquals(searchResults.get("available").intValue(), 1);

		// restore view item and revoke discover
		permissions.setNode("checkboxes", "DISCOVER_ITEM");
		item.put("metadata", metadata.toString());
		response = putItem(itemId, item.toString(), token, "waitforindex", true);
		assertResponse(response, 200, "200 not returned from item creation");

		// make sure we can see attachments
		item = getItem(itemId, "all", token);
		assertTrue(item.has("attachments"));
		assertEquals(item.get("attachments").get(0).get("url").textValue(), "http://google.com.au/");

		// ensure not discoverable
		searchResults = doSearch("ItemApiEditTest - Permissions from JSON", token);
		assertEquals(searchResults.get("available").intValue(), 0);
	}

	private HttpResponse lock(ItemId itemId, String token, int assertCode) throws Exception
	{
		final HttpPost request = new HttpPost(context.getBaseUrl() + "api/item/" + itemId.getUuid() + "/" + 1 + "/lock");
		final HttpResponse response = execute(request, false, token);
		assertResponse(response, assertCode, "Lock response was not " + assertCode);
		return response;
	}

	private void unlock(ItemId itemId, String token, int assertCode) throws Exception
	{
		final HttpDelete request = new HttpDelete(context.getBaseUrl() + "api/item/" + itemId.getUuid() + "/" + 1
			+ "/lock");
		final HttpResponse response = execute(request, true, token);
		assertResponse(response, assertCode, "Unlock response was not " + assertCode);
	}

	private HttpResponse getLock(ItemId itemId, String token, int assertCode) throws Exception
	{
		final HttpGet request = new HttpGet(context.getBaseUrl() + "api/item/" + itemId.getUuid() + "/" + 1 + "/lock");
		final HttpResponse response = execute(request, false, token);
		return response;
	}

	@Test
	public void testLockAndSaveWithNoLock() throws Exception
	{
		final String token = getToken();
		ItemId itemId = createItem(token);

		// lock it
		HttpResponse response = lock(itemId, token, 201);
		EntityUtils.consume(response.getEntity());

		// get it
		final ObjectNode item = getItem(itemId, "detail", token);

		// try to PUT it with no lock param
		response = putItem(itemId, item.toString(), token);
		try
		{
			// asert 409 (conflict) error code
			assertResponse(response, 409, "Save response with missing lock did not return " + 409);
		}
		finally
		{
			// unlock it
			unlock(itemId, token, 204);
		}
	}

	@Test
	public void testLockAndSaveWithLock() throws Exception
	{
		final String token = getToken();
		ItemId itemId = createItem(token);

		// lock it
		final ObjectNode lock = readJson(mapper, lock(itemId, token, 201));

		// get it
		final ObjectNode item = getItem(itemId, "detail", token);

		// try to PUT it with lock param
		HttpResponse response = putItem(itemId, item.toString(), token, "lock", lock.get("uuid").asText());
		assertResponse(response, 200, "200 not returned from legit lock usage");

		// ensure lock is no longer there
		unlock(itemId, token, 404);
		response = getLock(itemId, token, 404);
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void testLockALockedItem() throws Exception
	{
		final String token = getToken();
		ItemId itemId = createItem(token);

		// lock it
		HttpResponse response = null;
		try
		{
			response = lock(itemId, token, 201);
		}
		finally
		{
			if( response != null )
			{
				EntityUtils.consume(response.getEntity());
			}
		}

		// try to lock it again as a different user
		final String otherToken = requestToken(OAUTH_OTHER_CLIENT_ID);
		try
		{
			response = lock(itemId, otherToken, 409); // 409 == conflict
		}
		finally
		{
			if( response != null )
			{
				EntityUtils.consume(response.getEntity());
			}

			// unlock it
			unlock(itemId, token, 204);
		}
	}

	@Test
	public void testAutoUuidGeneration() throws Exception
	{
		final String token = getToken();
		PropBagEx metadata = new PropBagEx(
			"<xml><item><name>ItemApiEditTest - Auto UUID generation from JSON</name><description>Description</description><controls><editbox>uuid:0</editbox></controls></item></xml>");
		ObjectNode item = createItemObject(COLLECTION_PERMISSIONS, metadata, true);

		ObjectNode navigation = item.objectNode();
		ArrayNode nodes = navigation.arrayNode();
		ObjectNode node = nodes.objectNode();
		node.put("name", "Auto created uuid test");
		ArrayNode tabs = node.arrayNode();
		ObjectNode tab = tabs.objectNode();
		tab.put("name", "Tab 1");
		ObjectNode attachmentRef = tab.objectNode();
		attachmentRef.put("$ref", "uuid:0");
		tab.put("attachment", attachmentRef);
		tabs.add(tab);
		node.put("tabs", tabs);
		nodes.add(node);
		navigation.put("nodes", nodes);

		item.put("navigation", navigation);

		ObjectNode createItem = createItem(item.toString(), token);
		ItemId itemId = addDeletable(createItem);

		item = getItem(itemId, "all", token);

		String attachmentUuid = item.get("attachments").findValue("uuid").textValue();
		assertFalse(attachmentUuid.equals("uuid:0"), "The attachment should have been given a correct uuid");
		String navAttachmentUuid = item.get("navigation").findValue("$ref").textValue();
		assertEquals(attachmentUuid, navAttachmentUuid, "The uuids should be the same");

		attachmentRef = (ObjectNode) navigation.findValue("attachment");
		attachmentRef.put("$ref", "uuid:0");
		item.put("navigation", navigation);
		ObjectNode error = putItemError(getItemUri(itemId), item.toString(), token, 400);
		assertError(error, 400, "Bad Request",
			"Trying to associate navigation node tab with attachment which doesn't exist: node:.* attachment:uuid:0",
			true);

		ArrayNode attachments = (ArrayNode) item.get("attachments");
		ObjectNode attachment = (ObjectNode) item.get("attachments").get(0);
		attachment.put("uuid", "uuid:0");
		attachments.add(attachment);

		error = putItemError(getItemUri(itemId), item.toString(), token, 400);
		assertError(error, 400, "Bad Request", "Another attachment is already using this placeholder: uuid:0");
	}

	// EQUELLA doesn't implement item/copy
	@Test(groups = "eps")
	public void editScormAndZip() throws IOException, Exception
	{
		String token = getToken();
		ItemId itemId = createItem(token);
		ObjectNode item = getItem(itemId, null, token);

		String[] stagingParams = copyStaging(itemId, token);
		String stagingUuid = stagingParams[0];
		String stagingDirUrl = stagingParams[1];
		uploadFile(stagingDirUrl, "_SCORM/nonscorm.zip", Attachments.get("avatar.png"));

		ArrayNode attachments = (ArrayNode) item.get("attachments");
		ObjectNode scormAtt = item.objectNode();
		ObjectNode zipAtt = item.objectNode();
		ObjectNode scormResAtt = item.objectNode();

		int index = attachments.size();
		scormAtt.put("type", "scorm");
		scormAtt.put("description", "scorm-desc");
		scormAtt.put("packageFile", "nonscorm.zip");
		zipAtt.put("type", "zip");
		zipAtt.put("description", "zip-desc");
		zipAtt.put("folder", "non-existant");
		scormResAtt.put("type", "scorm-res");
		scormResAtt.put("description", "scorm-res-desc");
		scormResAtt.put("filename", "file");
		attachments.add(scormAtt);
		attachments.add(zipAtt);
		attachments.add(scormResAtt);
		item = editItem(item, token, "file", stagingUuid);
		attachments = (ArrayNode) item.get("attachments");

		scormAtt = (ObjectNode) attachments.get(index);
		asserter.assertAttachmentBasics(scormAtt, itemId, "scorm", null, "scorm-desc");
		assertEquals(scormAtt.get("packageFile").asText(), "nonscorm.zip");

		zipAtt = (ObjectNode) attachments.get(index + 1);
		asserter.assertAttachmentBasics(zipAtt, itemId, "zip", null, "zip-desc");
		assertEquals(zipAtt.get("folder").asText(), "non-existant");

		scormResAtt = (ObjectNode) attachments.get(index + 2);
		asserter.assertAttachmentBasics(scormResAtt, itemId, "scorm-res", null, "scorm-res-desc");
		assertEquals(scormResAtt.get("filename").asText(), "file");

		attachments.remove(index + 2);
		item = editItem(item, token);
		assertEquals(item.get("attachments").size(), index + 2);
	}

	private void assertFirstNavNode(JsonNode parentNode, String title)
	{
		asserter.assertNavNode(parentNode, 0, "49ffd9d1-cc47-4fce-ae49-897ca0a54024", title, "Tab 1",
			"63862d54-1b6d-4dce-9a79-44b3a8c9e107", "");
	}

	private void assertError(JsonNode error, int code, String title, String message)
	{
		assertError(error, code, title, message, false);
	}

	private void assertError(JsonNode error, int code, String title, String message, boolean regex)
	{
		assertEquals(error.get("code").intValue(), code);
		assertEquals(error.get("error").textValue(), title);
		String errorDescription = error.get("error_description").textValue();
		if( regex )
		{
			Pattern pattern = Pattern.compile(message);
			Matcher matcher = pattern.matcher(errorDescription);
			assertTrue(matcher.matches(), "Was expencting " + message + " to match " + errorDescription);
		}
		else
		{
			assertEquals(errorDescription, message);
		}

	}

	private ObjectNode createItemObject(String collectionUuid, PropBagEx metadata, boolean withAttachment)
	{
		ObjectNode item = mapper.createObjectNode();

		ObjectNode collection = item.objectNode();
		collection.put("uuid", collectionUuid);
		item.put("collection", collection);
		item.put("metadata", metadata.toString());
		if( withAttachment )
		{
			ArrayNode attachments = item.arrayNode();
			ObjectNode attachment = item.objectNode();
			attachment.put("type", "url");
			attachment.put("description", "Google");
			attachment.put("url", "http://google.com.au/");
			attachment.put("uuid", "uuid:0");
			attachments.add(attachment);
			item.put("attachments", attachments);
		}
		return item;
	}

	private JsonNode doSearch(String itemName, String token) throws Exception
	{
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/search?" + queryString("q", "\"" + itemName + "\""));
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}
}
