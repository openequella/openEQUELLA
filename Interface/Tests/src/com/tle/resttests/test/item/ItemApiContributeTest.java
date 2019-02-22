package com.tle.resttests.test.item;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.files.Attachments;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

public class ItemApiContributeTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "ItemApiContributeTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
  }

  private ObjectNode createItemJsonUuidVersion(String uuid, int version) {
    ObjectNode item = createItemJson(COLLECTION_ATTACHMENTS);
    if (uuid != null) {
      item.put("uuid", uuid);
    }
    item.put("version", version);
    return item;
  }

  private ObjectNode createItem(String uuid, int version, boolean draft) throws Exception {
    return createItem(
        createItemJsonUuidVersion(uuid, version).toString(), getToken(), "draft", draft);
  }

  @Test
  public void createAsDraft() throws Exception {
    String token = getToken();
    ObjectNode item = createItem(null, 0, true);
    ItemId itemId = addDeletable(item);
    assertEquals(item.get("status").asText(), "draft");
    String itemUri = getItemUri(itemId);
    assertResponse(
        itemAction(itemUri, "submit", token), 200, "Should have been able to submit item");
    item = getItem(itemUri, null, token);
    assertEquals(item.get("status").asText(), "live");
  }

  @Test
  public void createWithFileAndThumbnail() throws Exception {
    String token = getToken();
    ObjectNode node = mapper.createObjectNode();
    node.with("collection").put("uuid", COLLECTION_ATTACHMENTS);

    ArrayNode attachments = mapper.createArrayNode();

    ObjectNode fileAttachment = mapper.createObjectNode();
    fileAttachment.put("type", "file");
    fileAttachment.put("filename", "avatar.png");
    fileAttachment.put("description", "New file");
    attachments.add(fileAttachment);

    ObjectNode pdfAttachment = mapper.createObjectNode();
    pdfAttachment.put("type", "file");
    pdfAttachment.put("filename", "test.pdf");
    pdfAttachment.put("description", "pdf");
    attachments.add(pdfAttachment);

    node.put("attachments", attachments);

    String[] stagingParams = createStaging(token);
    String stagingUuid = stagingParams[0];
    String stagingDirUrl = stagingParams[1];
    uploadFile(stagingDirUrl, "avatar.png", Attachments.get("avatar.png"));
    uploadFile(stagingDirUrl, "test.pdf", Attachments.get("test.pdf"));
    ObjectNode newItem =
        createItem(
            node.toString(), token, "file", stagingUuid, "draft", true, "waitforindex", true);
    ItemId itemId = addDeletable(newItem);
    attachments = (ArrayNode) newItem.get("attachments");
    ObjectNode firstAttachment = (ObjectNode) attachments.get(0);
    int tries = 0;
    while (tries++ < 4 && !firstAttachment.has("thumbFilename")) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        // dont care
      }

      newItem = getItem(itemId, "all", token);
      attachments = (ArrayNode) newItem.get("attachments");
      firstAttachment = (ObjectNode) attachments.get(0);
    }

    String attachUuid = firstAttachment.get("uuid").asText();
    assertEquals(firstAttachment.get("size").asInt(), 12627);
    assertEquals(firstAttachment.get("md5").asText(), "5a4e69eeae86aa557c4a27d52257b757");
    assertTrue(firstAttachment.has("thumbFilename"), "Thumbnail still not indexed");
    assertEquals(firstAttachment.get("thumbFilename").asText(), "_THUMBS/avatar.png.jpeg");

    if (!isEquella()) {
      HttpResponse response = null;
      try {
        response = getResponse(getItemUri(itemId) + "/thumb/" + attachUuid, token, false);
        assertResponse(response, 200, "Should be a good");
        byte[] jpegData = EntityUtils.toByteArray(response.getEntity());
        assertJpeg(jpegData);
      } finally {
        if (response != null) {
          EntityUtils.consume(response.getEntity());
        }
      }
    }

    firstAttachment.put("type", "url");
    firstAttachment.put("url", "http://www.changed.com/");
    newItem = editItem(newItem, token);
    attachments = (ArrayNode) newItem.get("attachments");
    firstAttachment = (ObjectNode) attachments.get(0);
    asserter.assertAttachmentBasics(firstAttachment, itemId, "url", attachUuid, "New file");
    assertEquals(firstAttachment.get("url").asText(), "http://www.changed.com/");
    assertEquals(attachments.size(), 2);

    if (!isEquella()) {
      HttpResponse response = getResponse(getItemUri(itemId) + "thumb/" + attachUuid, token, true);
      assertResponse(response, 404, "Should be gone");
    }
  }

  private void assertJpeg(byte[] jpegData) {
    int size = jpegData.length;

    assertEquals(jpegData[0], (byte) 0xff, "Not a jpeg");
    assertEquals(jpegData[1], (byte) 0xd8, "Not a jpeg");

    assertEquals(jpegData[size - 2], (byte) 0xff, "Not a jpeg");
    assertEquals(jpegData[size - 1], (byte) 0xd9, "Not a jpeg");
  }
}
