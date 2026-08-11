package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.viewitem.ItemId;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;

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
    assertEquals(childNode.get("uuid").asText(), uuid);
    assertEquals(childNode.get("name").asText(), name);
    JsonNode tabsNode = childNode.get("tabs");
    int tabIndex = 0;
    for (String[] tab : tabs) {
      JsonNode tab1 = tabsNode.get(tabIndex);
      assertEquals(tab1.get("name").asText(), tab[0]);
      assertEquals(tab1.get("attachment").get("$ref").asText(), tab[1]);
      JsonNode viewer = tab1.get("viewer");
      if (viewer == null && tab[2] != null) {
        assertTrue(tab[2].isEmpty());
      } else {
        assertEquals(viewer.asText(), tab[2]);
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
      String collectionUuid)
      throws ParseException {
    assertEquals(tree.get("status").asText(), status);
    assertEquals(tree.get("rating").asDouble(), rating);
    assertDate(tree.get("createdDate"), created);
    assertDate(tree.get("modifiedDate"), modified);
    assertUser(tree.get("owner"), ownerId);
    assertCollection(tree.get("collection"), collectionUuid);
  }

  public void assertHistory(
      JsonNode historyNode, String userId, Object time, String type, String state)
      throws ParseException {
    assertUser(historyNode.get("user"), userId);
    assertDate(historyNode.get("date"), time);
    assertEquals(historyNode.get("type").asText(), type);
    assertEquals(historyNode.get("state").asText(), state);
  }

  public void assertAttachmentBasics(
      JsonNode attachment, ItemId itemId, String type, String uuid, String description) {
    assertEquals(attachment.get("type").asText(), type);
    String actualUuid = attachment.get("uuid").asText();
    if (uuid != null) {
      assertEquals(actualUuid, uuid);
    }
    assertEquals(attachment.get("description").asText(), description);
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
    assertEquals(linksNode.get(rel).asText(), href);
  }

  public void assertUser(JsonNode userNode, String ownerId) {
    assertEquals(userNode.get("id").asText(), ownerId);
  }

  public void assertCollection(JsonNode userNode, String collectionUuid) {
    assertEquals(userNode.get("uuid").asText(), collectionUuid);
  }

  public void assertDate(JsonNode dateNode, Object time) throws ParseException {
    Date parsed = ISO8601Utils.parse(dateNode.asText(), new ParsePosition(0));
    if (time instanceof String) {
      time = ISO8601Utils.parse((String) time, new ParsePosition(0)).getTime();
    }
    if (Math.abs(((Long) time) - parsed.getTime()) > 3) {
      assertEquals(
          dateNode.asText(),
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
      Object time)
      throws ParseException {
    assertEquals(commentNode.get("uuid").asText(), uuid);
    assertEquals(commentNode.get("rating").asInt(), rating);
    if (userId != null) {
      assertUser(commentNode.get("postedBy"), userId);
    }
    assertEquals(commentNode.get("anonymous").asBoolean(), anonymous);
    assertEquals(commentNode.get("comment").asText(), comment);
    assertDate(commentNode.get("postedDate"), time);
  }

  public void assertDate(JsonNode dateNode, String iso8601Date) {
    assertEquals(dateNode.asText(), iso8601Date);
  }
}
