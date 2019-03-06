package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.test.files.Attachments;

public class ItemApiContributeTest extends AbstractItemApiTest
{
	private static final String OAUTH_CLIENT_ID = "ItemApiContributeTestClient";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
	}

	private ObjectNode createItemJsonUuidVersion(String uuid, int version)
	{
		ObjectNode item = createItemJson(COLLECTION_ATTACHMENTS);
		if( uuid != null )
		{
			item.put("uuid", uuid);
		}
		item.put("version", version);
		return item;		
	}
	
	private HttpResponse createItemResponse(String uuid, int version, boolean draft) throws Exception
	{
		String token = getToken();
		ObjectNode item = createItemJsonUuidVersion(uuid, version);
		return postItem(item.toString(), token, "draft", draft);
	}

	private ObjectNode createItem(String uuid, int version, boolean draft) throws Exception
	{
		return createItem(createItemJsonUuidVersion(uuid, version).toString(), getToken(), "draft", draft);
	}

	@Test
	public void createAsDraft() throws Exception
	{
		String token = getToken();
		ObjectNode item = createItem(null, 0, true);
		ItemId itemId = addDeletable(item);
		assertEquals(item.get("status").asText(), "draft");
		String itemUri = getItemUri(itemId);
		assertResponse(itemAction(itemUri, "submit", token), 200, "Should have been able to submit item");
		item = getItem(itemUri, null, token);
		assertEquals(item.get("status").asText(), "live");
	}

	@Test
	public void createAndSubmit() throws Exception
	{
		assertResponse(createItemResponse(null, 2, false), 400, "Shouldn't be able to create v2 of non-existant item");
		ObjectNode item = createItem(null, 0, false);
		ItemId itemId = addDeletable(item);
		assertEquals(item.get("status").asText(), "live");
		String newUuid = itemId.getUuid();
		assertResponse(createItemResponse(newUuid, 3, false), 400, "Shouldn't be able to create v3");
		item = createItem(newUuid, 0, false);
		addDeletable(item);
		assertEquals(item.get("version").asInt(), 2);
		assertResponse(createItemResponse(newUuid, 2, false), 400, "Item should already exist");
	}

	@Test
	public void createWithFileAndThumbnail() throws Exception
	{
		String token = getToken();
		ObjectNode node = mapper.createObjectNode();
		node.with("collection").put("uuid", COLLECTION_ATTACHMENTS);
		ArrayNode attachments = mapper.createArrayNode();
		ObjectNode fileAttachment = mapper.createObjectNode();
		fileAttachment.put("type", "file");
		fileAttachment.put("filename", "avatar.png");
		fileAttachment.put("description", "New file");
		attachments.add(fileAttachment);
		node.put("attachments", attachments);

		String[] stagingParams = createStaging();
		String stagingUuid = stagingParams[0];
		String stagingDirUrl = stagingParams[1];
		uploadFile(stagingDirUrl, "avatar.png", Attachments.get("avatar.png"));
		ObjectNode newItem = createItem(node.toString(), token, "file", stagingUuid, "draft", true, "waitforindex", true);
		ItemId itemId = addDeletable(newItem);
		attachments = (ArrayNode) newItem.get("attachments");
		ObjectNode firstAttachment = (ObjectNode) attachments.get(0);
		String attachUuid = firstAttachment.get("uuid").asText();
		assertEquals(firstAttachment.get("size").asInt(), 12627);
		assertEquals(firstAttachment.get("md5").asText(), "5a4e69eeae86aa557c4a27d52257b757");
		assertEquals(firstAttachment.get("thumbFilename").asText(), "_THUMBS/avatar.png.jpeg");
		
		firstAttachment.put("type", "url");
		firstAttachment.put("url", "http://www.changed.com/");
		newItem = editItem(newItem, token);
		attachments = (ArrayNode) newItem.get("attachments");
		firstAttachment = (ObjectNode) attachments.get(0);
		asserter.assertAttachmentBasics(firstAttachment, itemId, "url", attachUuid, "New file");
		assertEquals(firstAttachment.get("url").asText(), "http://www.changed.com/");
		assertEquals(attachments.size(), 1);
	}
	
	@Test
	public void createWithSaveScript() throws IOException
	{
		String token = getToken();
		ObjectNode item = createItemJson(COLLECTION_SAVESCRIPT);
		item = createItem(item.toString(), token);
		ItemId itemId = addDeletable(item);
		PropBagEx metadata = new PropBagEx(item.get("metadata").asText());
		assertEquals(metadata.nodeCount("editedbysavescript"), 1);
		assertEquals(metadata.getIntNode("editedbysavescript"), 0);
		JsonNode attachments = item.get("attachments");
		assertEquals(attachments.size(), 1);
		asserter.assertAttachmentBasics(attachments.get(0), itemId, "url", null, "Link 0");
		item = editItem(item, token);
		metadata = new PropBagEx(item.get("metadata").asText());
		assertEquals(metadata.nodeCount("editedbysavescript"), 2);
		assertEquals(metadata.getIntNode("editedbysavescript"), 0);
		assertEquals(metadata.getIntNode("editedbysavescript[1]"), 1);
		attachments = item.get("attachments");
		assertEquals(attachments.size(), 2);
		asserter.assertAttachmentBasics(attachments.get(0), itemId, "url", null, "Link 0");
		asserter.assertAttachmentBasics(attachments.get(1), itemId, "url", null, "Link 1");

	}
}
