package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public class ItemApiRelationTest extends AbstractItemApiTest
{
	private static final String OAUTH_CLIENT_ID = "ItemApiRelationTestClient";
	private static final String ITEMNAME_HOLDING = "ItemApiRelationTest - Book Holding";
	private static final String ITEMNAME_PORTION = "ItemApiRelationTest - Book Portion";
	private static final String COLLECTION_HOLDING = "4c147089-cddb-e67c-b5ab-189614eb1463";
	private static final String COLLECTION_PORTION = "bfad7ea0-983f-dd58-6b7d-ed5f346e24ab";
	private ItemId holdingId;
	private ItemId portionId;

	@Test
	public void testRelations() throws Exception
	{
		String token = getToken();
		ObjectNode item = createItem(token);
		ItemId fromId = addDeletable(item);
		item = createItem(token);
		ItemId toId = addDeletable(item);

		String relationUri = createRelation(getItemUri(fromId), token, null, toId, "RandomType");
		JsonNode relationJson = getEntity(relationUri, token);
		AssertJUnit.assertEquals(relationJson.get("relation").asText(), "RandomType");
		JsonNode toJson = relationJson.get("to");
		AssertJUnit.assertEquals(toJson.get("uuid").asText(), toId.getUuid());
		AssertJUnit.assertEquals(toJson.get("version").asInt(), toId.getVersion());
		relationUri = createRelation(getItemUri(fromId), token, toId, null, "OtherType");

		JsonNode relations = getEntity(getItemUri(fromId) + "relation", token);
		AssertJUnit.assertEquals(relations.size(), 2);
		assertResponse(deleteResource(relationUri, token), 200, "Can't delete relation");
		relations = getEntity(getItemUri(fromId) + "relation", token);
		AssertJUnit.assertEquals(relations.size(), 1);
	}

	private String createRelation(String itemUri, String token, ItemId fromId, ItemId toId, String type)
		throws IOException
	{
		ObjectNode relation = mapper.createObjectNode();
		relation.put("relation", type);
		if( fromId != null )
		{
			relation.put("from", createItemRef(fromId));
		}
		if( toId != null )
		{
			relation.put("to", createItemRef(toId));
		}
		HttpResponse response = postEntity(relation.toString(), itemUri + "relation", token, true);
		assertResponse(response, 201, "Should be able to create relation");
		return response.getFirstHeader("Location").getValue();
	}

	private ObjectNode createItemRef(ItemId itemId)
	{
		ObjectNode item = mapper.createObjectNode();
		item.put("uuid", itemId.getUuid());
		item.put("version", itemId.getVersion());
		return item;
	}

	private ObjectNode createItem(String token) throws IOException
	{
		ObjectNode item = mapper.createObjectNode();
		item.with("collection").put("uuid", "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c");
		HttpResponse response = postItem(item.toString(), token);
		assertResponse(response, 201, "Should be able to create item");
		String itemUri = response.getFirstHeader("Location").getValue();
		return getItem(itemUri, null, token);
	}

	@Test
	public void testHolding() throws IOException
	{
		String token = getToken();
		ObjectNode item = mapper.createObjectNode();
		item.with("collection").put("uuid", COLLECTION_HOLDING);
		PropBagEx metadata = new PropBagEx();
		PropBagEx itemXml = metadata.aquireSubtree("item");
		PropBagEx copyright = itemXml.aquireSubtree("copyright");
		copyright.setNode("@type", "book");
		itemXml.setNode("itembody/name", ITEMNAME_HOLDING);
		copyright.setNode("title", ITEMNAME_HOLDING);
		copyright.setNode("pages", "100");
		item.put("metadata", metadata.toString());
		HttpResponse response = postItem(item.toString(), token);
		assertResponse(response, 201, "Should be able to create item");
		String itemUri = response.getFirstHeader("Location").getValue();
		item = getItem(itemUri, null, token);
		holdingId = addDeletable(item);
	}

	@Test
	public void testPortion() throws IOException
	{
		String token = getToken();
		ObjectNode item = mapper.createObjectNode();
		item.with("collection").put("uuid", COLLECTION_PORTION);
		PropBagEx metadata = new PropBagEx();
		PropBagEx itemXml = metadata.aquireSubtree("item");
		PropBagEx copyright = itemXml.aquireSubtree("copyright");
		copyright.setNode("@parenttype", "book");
		itemXml.setNode("itembody/name", ITEMNAME_PORTION);
		copyright.setNode("title", ITEMNAME_PORTION);
		PropBagEx portion = copyright.aquireSubtree("portions/portion");
		portion.setNode("title", "Portion 1");
		portion.setNode("number", "1");
		PropBagEx section = portion.aquireSubtree("sections/section");
		section.setNode("pages", "10");
		item.put("metadata", metadata.toString());
		HttpResponse response = postItem(item.toString(), token);
		assertResponse(response, 201, "Should be able to create item");
		String itemUri = response.getFirstHeader("Location").getValue();
		item = getItem(itemUri, null, token);
		portionId = addDeletable(item);
	}

	@Test(dependsOnMethods = {"testHolding", "testPortion"})
	public void relationHoldingAndPortion() throws IOException
	{
		String portionUri = getItemUri(portionId);
		String token = getToken();
		String relationUri = createRelation(portionUri, token, null, holdingId, "CAL_HOLDING");
		ObjectNode item = getItem(portionUri, "basic", token);
		logon("AutoTest", "automated");
		context.getDriver().get(item.get("links").get("view").asText());
		SummaryPage summaryTab = new SummaryPage(context).get();
		CALSummaryPage calSummaryPage = summaryTab.cal();
		assertResponse(deleteResource(relationUri, token), 200, "Couldn't delete relation");
		context.getDriver().get(item.get("links").get("view").asText());
		summaryTab.get();
		assertFalse(CALSummaryPage.isDisplayed(summaryTab));
		
	}

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
	}
}
