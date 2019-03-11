package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.viewitem.ItemId;
import java.util.Date;
import java.util.TimeZone;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.util.ISO8601Utils;
import org.codehaus.jackson.node.ObjectNode;

public class ApiAssertions {
  private final PageContext context;

  public ApiAssertions(PageContext context) {
    this.context = context;
  }

  public void assertStatus(ObjectNode item, String status) {
    assertEquals(item.get("status").asText(), status);
  }

  public void assertBasic(ObjectNode tree, String name, String description) {
    assertEquals(tree.get("name").asText(), name);
    JsonNode descriptionNode = tree.get("description");
    if (description != null) {
      assertEquals(descriptionNode.asText(), description);
    } else {
      assertNull(descriptionNode);
    }
  }

  public JsonNode assertNavNode(
      JsonNode parentNode, int index, String uuid, String name, String[][] tabs) {
    JsonNode childNode = parentNode.get(index);
    assertEquals(childNode.get("uuid").getTextValue(), uuid);
    assertEquals(childNode.get("name").getTextValue(), name);
    JsonNode tabsNode = childNode.get("tabs");
    int tabIndex = 0;
    for (String[] tab : tabs) {
      JsonNode tab1 = tabsNode.get(tabIndex);
      assertEquals(tab1.get("name").getTextValue(), tab[0]);
      assertEquals(tab1.get("attachment").get("$ref").getTextValue(), tab[1]);
      JsonNode viewer = tab1.get("viewer");
      if (viewer == null && tab[2] != null) {
        assertTrue(tab[2].isEmpty());
      } else {
        assertEquals(viewer.getTextValue(), tab[2]);
      }
      tabIndex++;
    }
    return childNode.get("nodes");
  }

  public JsonNode assertNavNode(
      JsonNode parentNode,
      int index,
      String uuid,
      String name,
      String tabName,
      String attachment,
      String viewer) {
    return assertNavNode(
        parentNode, index, uuid, name, new String[][] {{tabName, attachment, viewer}});
  }

  public void assertDetails(
      ObjectNode tree,
      String status,
      double rating,
      Object created,
      Object modified,
      String ownerId,
      String collectionUuid) {
    assertEquals(tree.get("status").getTextValue(), status);
    assertEquals(tree.get("rating").getDoubleValue(), rating);
    assertDate(tree.get("createdDate"), created);
    assertDate(tree.get("modifiedDate"), modified);
    assertUser(tree.get("owner"), ownerId);
    assertCollection(tree.get("collection"), collectionUuid);
  }

  public void assertHistory(
      JsonNode historyNode, String userId, Object time, String type, String state) {
    assertUser(historyNode.get("user"), userId);
    assertDate(historyNode.get("date"), time);
    assertEquals(historyNode.get("type").getTextValue(), type);
    assertEquals(historyNode.get("state").getTextValue(), state);
  }

  public void assertAttachmentBasics(
      JsonNode attachment, ItemId itemId, String type, String uuid, String description) {
    assertEquals(attachment.get("type").getTextValue(), type);
    String actualUuid = attachment.get("uuid").asText();
    if (uuid != null) {
      assertEquals(actualUuid, uuid);
    }
    assertEquals(attachment.get("description").getTextValue(), description);
    JsonNode linksNode = attachment.get("links");
    assertLink(
        linksNode,
        "view",
        context.getBaseUrl() + "items/" + itemId + "/?attachment.uuid=" + actualUuid);
  }

  public void assertViewerAndPreview(JsonNode attachment, String viewer, boolean preview) {
    JsonNode viewerNode = attachment.get("viewer");
    if (viewer == null) {
      assertNull(viewerNode);
    } else {
      assertEquals(viewerNode.asText(), viewer);
    }
    assertEquals(attachment.get("preview").asBoolean(), preview);
  }

  public void assertLink(JsonNode linksNode, String rel, String href) {
    assertEquals(linksNode.get(rel).getTextValue(), href);
  }

  public void assertUser(JsonNode userNode, String ownerId) {
    assertEquals(userNode.get("id").getTextValue(), ownerId);
  }

  public void assertCollection(JsonNode userNode, String collectionUuid) {
    assertEquals(userNode.get("uuid").getTextValue(), collectionUuid);
  }

  public void assertDate(JsonNode dateNode, Object time) {
    Date parsed = ISO8601Utils.parse(dateNode.getTextValue());
    if (time instanceof String) {
      time = ISO8601Utils.parse((String) time).getTime();
    }
    if (Math.abs(((Long) time) - parsed.getTime()) > 3) {
      assertEquals(
          dateNode.getTextValue(),
          ISO8601Utils.format(
              new Date((Long) time), true, TimeZone.getTimeZone("America/Chicago")));
    }
  }

  public void assertComment(
      JsonNode commentNode,
      String uuid,
      int rating,
      String userId,
      boolean anonymous,
      String comment,
      Object time) {
    assertEquals(commentNode.get("uuid").getTextValue(), uuid);
    assertEquals(commentNode.get("rating").getIntValue(), rating);
    if (userId != null) {
      assertUser(commentNode.get("postedBy"), userId);
    }
    assertEquals(commentNode.get("anonymous").getBooleanValue(), anonymous);
    assertEquals(commentNode.get("comment").getTextValue(), comment);
    assertDate(commentNode.get("postedDate"), time);
  }

  public void assertDate(JsonNode dateNode, String iso8601Date) {
    assertEquals(dateNode.getTextValue(), iso8601Date);
  }
}
