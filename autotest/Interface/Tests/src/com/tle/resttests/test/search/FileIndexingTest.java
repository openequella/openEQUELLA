package com.tle.resttests.test.search;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.files.Attachments;
import java.util.List;
import org.testng.annotations.Test;

public class FileIndexingTest extends AbstractItemApiTest {

  private static final String OAUTH_CLIENT_ID = "FileIndexingTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
  }

  // DTEC 14899
  @Test
  public void pptIndexTest() throws Exception {
    String token = getToken();
    ObjectNode node = mapper.createObjectNode();
    node.with("collection").put("uuid", COLLECTION_ATTACHMENTS);

    final String ITEM_NAME = context.getFullName("Powerpoint Indexing");
    final List<String> SEARCH_TERMS = Lists.newArrayList("funky", "Slide two", "numero uno");
    node.put("metadata", "<xml><item><name>" + ITEM_NAME + "</name></item></xml>");

    ArrayNode attachments = mapper.createArrayNode();
    ObjectNode fileAttachment = mapper.createObjectNode();
    fileAttachment.put("type", "file");
    fileAttachment.put("filename", "pptforindexing.pptx");
    fileAttachment.put("description", "pptforindexing.pptx");
    attachments.add(fileAttachment);
    node.put("attachments", attachments);

    String[] stagingParams = createStaging(token);
    String stagingUuid = stagingParams[0];
    String stagingDirUrl = stagingParams[1];
    uploadFile(stagingDirUrl, "pptforindexing.pptx", Attachments.get("pptforindexing.pptx"));
    ObjectNode newItem =
        createItem(
            node.toString(), token, "file", stagingUuid, "draft", false, "waitforindex", true);

    ItemId itemId = addDeletable(newItem);
    waitForIndex(ITEM_NAME, itemId.getUuid());

    for (String term : SEARCH_TERMS) {
      JsonNode basicSearch = basicSearch(term, token);
      // This is a bit dodge...
      JsonNode item = basicSearch.get("results").get(0);
      assertEquals(item.get("name").asText(), ITEM_NAME);
    }
  }
}
