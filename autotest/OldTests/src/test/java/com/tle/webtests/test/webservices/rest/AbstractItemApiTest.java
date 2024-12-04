package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.tle.webtests.pageobject.viewitem.ItemId;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

public abstract class AbstractItemApiTest extends AbstractRestApiTest {
  private List<ItemId> toDelete = Lists.newArrayList();
  protected static final String COLLECTION_ATTACHMENTS = "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c";
  protected static final String COLLECTION_PERMISSIONS = "a0efe7b5-f670-44b8-a84e-c6e2f42c909a";
  protected static final String COLLECTION_MODERATE = "3d31ac33-261e-404c-a157-487e51716268";
  protected static final String COLLECTION_SAVESCRIPT = "c7194cd0-f586-49b6-9fcc-4b1c5237efd9";

  protected String[] createStaging() throws IOException {
    HttpResponse stagingResponse = execute(new HttpPost(context.getBaseUrl() + "api/file/"), false);
    assertResponse(stagingResponse, 201, "201 not returned from staging creation");
    ObjectNode stagingJson = readJson(mapper, stagingResponse);
    String stagingUuid = stagingJson.get("uuid").asText();
    String stagingDirUrl = stagingJson.get("links").get("content").asText();
    return new String[] {stagingUuid, stagingDirUrl};
  }

  protected void uploadFile(String stagingDirUrl, String filename, URL resource)
      throws IOException {
    String avatarUrl = stagingDirUrl + '/' + com.tle.common.URLUtils.urlEncode(filename);
    HttpPut putfile = new HttpPut(avatarUrl);
    URLConnection file = resource.openConnection();
    InputStreamEntity inputStreamEntity =
        new InputStreamEntity(file.getInputStream(), file.getContentLength());
    inputStreamEntity.setContentType("application/octet-stream");
    putfile.setEntity(inputStreamEntity);
    HttpResponse putfileResponse = execute(putfile, true);
    assertResponse(putfileResponse, 201, "201 not returned from staging creation");
  }

  protected JsonNode getItemJson(String itemUri, String info, String token) throws IOException {
    return getEntity(itemUri, token, "info", info);
  }

  protected JsonNode getItemSubResource(
      String itemId, int version, String subResource, String token, Object... params)
      throws IOException {
    return getEntity(getItemUri(new ItemId(itemId, version)) + subResource, token, params);
  }

  protected ObjectNode getItem(String uuid, int version, String info, String token)
      throws IOException {
    return (ObjectNode) getItemJson(getItemUri(uuid, String.valueOf(version)), info, token);
  }

  protected ObjectNode getItem(ItemId itemId, String info, String token) throws IOException {
    return (ObjectNode) getItemJson(getItemUri(itemId), info, token);
  }

  protected ObjectNode getItem(String itemUri, String info, String token) throws IOException {
    return (ObjectNode) getItemJson(itemUri, info, token);
  }

  protected ArrayNode getItems(String uuid, String version, String info, String token)
      throws IOException {
    return getItems(getItemUri(uuid, version), info, token);
  }

  protected ArrayNode getItems(String itemUri, String info, String token) throws IOException {
    return (ArrayNode) getItemJson(itemUri, info, token);
  }

  protected String getItemUri(ItemId itemId) {
    return getItemUri(itemId.getUuid(), String.valueOf(itemId.getVersion()));
  }

  protected String getItemUri(String uuid, String version) {
    StringBuilder sbuf = new StringBuilder();
    sbuf.append(context.getBaseUrl());
    sbuf.append("api/item/");
    sbuf.append(uuid);
    sbuf.append('/');
    sbuf.append(version);
    if (!version.isEmpty() && !version.endsWith("/")) {
      sbuf.append('/');
    }
    return sbuf.toString();
  }

  protected ObjectNode createItem(String json, String token, Object... paramNameValues)
      throws IOException {
    HttpResponse response = postItem(json, token, paramNameValues);
    assertResponse(response, 201, "Should have created the item");
    String itemUri = response.getFirstHeader("Location").getValue();
    return getItem(itemUri, null, token);
  }

  protected ObjectNode createComment(String token, Object... paramNameValues) throws IOException {
    HttpResponse response = postItem("", token, paramNameValues);
    assertResponse(response, 201, "Should have created the item");
    return null;
  }

  protected HttpResponse postItem(String json, String token, Object... paramNameValues)
      throws IOException {
    return postEntity(json, context.getBaseUrl() + "api/item", token, true, paramNameValues);
  }

  protected HttpResponse postItem(
      String json, boolean consume, String token, Object... paramNameValues) throws IOException {
    return postEntity(json, context.getBaseUrl() + "api/item", token, consume, paramNameValues);
  }

  protected HttpResponse deleteItem(
      String uuid, int version, String token, Object... paramNameValues) throws IOException {
    return deleteResource(getItemUri(uuid, Integer.toString(version)), token, paramNameValues);
  }

  protected HttpResponse itemAction(
      String itemUri, String action, String token, Object... paramNameValues)
      throws ClientProtocolException, IOException {
    HttpPost request =
        new HttpPost(itemUri + "action/" + action + "?" + queryString(paramNameValues));
    return execute(request, true, token);
  }

  protected ObjectNode editItem(ObjectNode item, String token, Object... paramNameValues)
      throws IOException {
    ItemId itemId = new ItemId(item.get("uuid").asText(), item.get("version").asInt());
    String itemUri = getItemUri(itemId);
    HttpResponse putItem = putItem(itemUri, item.toString(), token, false, paramNameValues);
    try {
      assertResponse(
          putItem, 200, "Should have been able to edit. " + superSerialResponse(putItem));
    } finally {
      EntityUtils.consume(putItem.getEntity());
    }
    return getItem(itemUri, null, token);
  }

  protected HttpResponse putItem(
      String uuid, int version, String json, String token, Object... paramNameValues)
      throws Exception {
    return putItem(getItemUri(uuid, String.valueOf(version)), json, token, true, paramNameValues);
  }

  protected ObjectNode putItemError(
      String uuid,
      int version,
      String json,
      String token,
      int responseCode,
      Object... paramNameValues)
      throws Exception {
    return putItemError(
        getItemUri(uuid, String.valueOf(version)), json, token, responseCode, paramNameValues);
  }

  protected HttpResponse putItem(
      String itemUri, String json, String token, boolean consume, Object... paramNameValues)
      throws IOException {
    final HttpPut request = new HttpPut(appendQueryString(itemUri, queryString(paramNameValues)));
    final StringEntity ent = new StringEntity(json, "UTF-8");
    ent.setContentType("application/json");
    request.setEntity(ent);
    return execute(request, consume, token);
  }

  protected ObjectNode putItemError(
      String itemUri, String json, String token, int responseCode, Object... paramNameValues)
      throws Exception {
    final HttpPut request = new HttpPut(appendQueryString(itemUri, queryString(paramNameValues)));
    final StringEntity ent = new StringEntity(json, "UTF-8");
    ent.setContentType("application/json");
    request.setEntity(ent);
    HttpResponse response = execute(request, false, token);
    ObjectNode error = (ObjectNode) mapper.readTree(response.getEntity().getContent());
    assertResponse(response, responseCode, "Incorrect response code");
    return error;
  }

  protected void assertNameVersionStatus(
      JsonNode itemNode, String name, int version, String status) {
    assertEquals(itemNode.get("name").asText(), name);
    assertEquals(itemNode.get("version").asInt(), version);
    assertEquals(itemNode.get("status").asText(), status);
  }

  protected void assertNulls(JsonNode tree, String... nodes) {
    for (String node : nodes) {
      assertNull(tree.get(node));
    }
  }

  protected void assertMetadata(JsonNode tree, String... pathsAndValues) {
    PropBagEx metaXml = new PropBagEx(tree.get("metadata").asText());
    for (int i = 0; i < pathsAndValues.length; i += 2) {
      String path = pathsAndValues[i];
      String value = pathsAndValues[i + 1];
      assertEquals(metaXml.getNode(path), value);
    }
  }

  protected void assertLinks(JsonNode tree, ItemId itemId) {
    JsonNode linksNode = tree.get("links");
    asserter.assertLink(linksNode, "self", getItemUri(itemId));
    asserter.assertLink(linksNode, "view", context.getBaseUrl() + "items/" + itemId + "/");
  }

  // TODO: add params to all below

  protected void assertUrlAttachment(JsonNode urlAttachment, ItemId itemId) {
    assertUrlAttachment(urlAttachment, itemId, "Google", "http://google.com.au/");
  }

  protected void assertUrlAttachment(
      JsonNode urlAttachment, ItemId itemId, String title, String url) {
    asserter.assertAttachmentBasics(
        urlAttachment, itemId, "url", "32a79ea6-8b67-4b38-af85-341b2d512f09", title);
    assertEquals(urlAttachment.get("url").asText(), url);
    assertFalse(urlAttachment.get("disabled").asBoolean());
  }

  protected ItemId addDeletable(ObjectNode item) {
    ItemId itemId = new ItemId(item.get("uuid").asText(), item.get("version").asInt());
    toDelete.add(itemId);
    return itemId;
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    for (ItemId itemId : toDelete) {
      deleteItem(itemId.getUuid(), itemId.getVersion(), getToken(), "purge", true);
    }
    super.cleanupAfterClass();
  }

  protected ObjectNode createItemJson(String collection) {
    ObjectNode item = mapper.createObjectNode();
    item.with("collection").put("uuid", collection);
    return item;
  }
}
