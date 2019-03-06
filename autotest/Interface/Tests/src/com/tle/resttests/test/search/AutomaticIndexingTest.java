package com.tle.resttests.test.search;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.files.Attachments;

public class AutomaticIndexingTest extends AbstractItemApiTest
{

	private static final String OAUTH_CLIENT_ID = "AutomaticIndexingTestClient";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
	}

	@DataProvider(name = "filesAndQueries", parallel = false)
	public Object[][] getData()
	{
		return new Object[][]{{"DOC.doc", "msword", 1}, {"html test.html", "myhtmlTest", 2},
				{"text test.txt", "unique", 3}, {"xl2007.xlsx", "ridiculousness", 4}, {"XLS.xls", "msexcel", 5}};
	}

	/*
	 * This test is to ensure that automatic indexing of attachments works, by
	 * entering text inside a file attachment and making sure that the item is
	 * returned when that term is searched for.
	 */

	@Test(dataProvider = "filesAndQueries")
	public void autoIndexing(String filename, String query, int itemUpto) throws Exception
	{
		String token = getToken();
		ObjectNode node = mapper.createObjectNode();
		node.with("collection").put("uuid", COLLECTION_ATTACHMENTS);

		String itemName = context.getFullName("Item" + itemUpto);
		node.put("metadata", "<xml><item><name>" + itemName + "</name></item></xml>");

		ArrayNode attachments = mapper.createArrayNode();
		ObjectNode fileAttachment = mapper.createObjectNode();
		fileAttachment.put("type", "file");
		fileAttachment.put("filename", filename);
		fileAttachment.put("description", filename);
		attachments.add(fileAttachment);
		node.put("attachments", attachments);

		String[] stagingParams = createStaging(token);
		String stagingUuid = stagingParams[0];
		String stagingDirUrl = stagingParams[1];
		uploadFile(stagingDirUrl, filename, Attachments.get("indexing/" + filename));
		ObjectNode newItem = createItem(node.toString(), token, "file", stagingUuid, "draft", true, "waitforindex",
			true);

		ItemId itemId = addDeletable(newItem);
		//Just having the item name discoverable is not enough.  The indexing is broken into slow and fast, where the slow is extracting metadata from the attachment.
		waitForIndex(query, itemId.getUuid(), true);

		JsonNode basicResults = basicSearch(query, token, true);
		Assert.assertTrue(basicResults.get("length").asInt() > 0, "Zero results for " + query
			+ " but expecting to find " + itemName);

		JsonNode results = basicResults.get("results");
		Assert.assertNotNull(results, "Null results");

		JsonNode result = results.get(0);
		String resultName = result.get("name").asText();
		Assert.assertEquals(resultName, itemName, "Unexpected result name");
	}
}
