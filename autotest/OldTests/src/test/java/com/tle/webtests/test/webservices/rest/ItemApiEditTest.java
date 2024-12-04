package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.annotation.Nullable;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.viewitem.DRMAgreementPage;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.files.Attachments;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

public class ItemApiEditTest extends AbstractItemApiTest {
  // private static final String DRMITEM_ID =
  // "4de9dc1e-9740-4d6a-b848-cf5712882ce6";
  // private static final String ITEM_ID =
  // "2f6e9be8-897d-45f1-98ea-7aa31b449c0e";
  private static final String USERID_AUTOTEST = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
  private static final String USERID_RESTNOPRIV = "c4c9471b-d3e1-4157-9847-2e6d1e8b9b4e";

  private static final String DESCRIPTION = "This is a description";

  private static final String ITEM_UUID = "13e7a560-6755-11e1-b733-bc0a4924019b";

  private static final String OAUTH_OTHER_CLIENT_ID = "IAETOtherClient";
  private static final String OAUTH_NOPRIVS_CLIENT_ID = "IAETestNoPrivsClient";

  private static final ItemId ITEM_ID = new ItemId(ITEM_UUID, 1);

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>("IAETClient", "AutoTest"));
    clients.add(new Pair<String, String>(OAUTH_OTHER_CLIENT_ID, "tokenuser"));
    clients.add(new Pair<String, String>(OAUTH_NOPRIVS_CLIENT_ID, "RESTNoPrivs"));
  }

  private void createItem(String token) throws IOException {
    createItem(token, false);
  }

  private void createItem(String token, boolean withFile) throws IOException {
    String stagingUuid = null;
    if (withFile) {
      String[] stagingParams = createStaging();
      stagingUuid = stagingParams[0];
      String stagingDirUrl = stagingParams[1];
      uploadFile(stagingDirUrl, "avatar.png", Attachments.get("avatar.png"));
    }

    createItemFromJsonFile("newitem.json", token, stagingUuid);
  }

  private void createDrmItem(String token) throws Exception {
    createItemFromJsonFile("drmitem.json", token);
  }

  private void createItemFromJsonFile(String json, String token) throws IOException {
    createItemFromJsonFile(json, token, null);
  }

  private void createItemFromJsonFile(String json, String token, String stagingUuid)
      throws IOException {
    createItemFromJsonFile(json, token, stagingUuid, 201, false, standardParams());
  }

  private ObjectNode createItemFromJsonFile(
      String json,
      String token,
      String stagingUuid,
      int responseCode,
      boolean returnError,
      Map<String, String> nameValues)
      throws IOException {
    ObjectNode jsonObject = (ObjectNode) mapper.readTree(readJsonFile(json, nameValues));
    return createItemFromJsonObject(jsonObject, token, stagingUuid, responseCode, returnError);
  }

  private String readJsonFile(String jsonFile, Map<String, String> nameValues) throws IOException {
    final StringWriter sw = new StringWriter();
    final InputStream input = ItemApiEditTest.class.getResourceAsStream(jsonFile);
    try {
      CharStreams.copy(new InputStreamReader(input), sw);
    } finally {
      Closeables.closeQuietly(input);
    }
    String json = sw.toString();
    for (Entry<String, String> nv : nameValues.entrySet()) {
      json = json.replace("${" + nv.getKey() + "}", nv.getValue());
    }
    return json;
  }

  private Map<String, String> standardParams(String... p) {
    Map<String, String> pm = params(p);
    pm.put("uuid", ITEM_UUID);
    pm.put("version", "1");
    return pm;
  }

  private Map<String, String> params(String... p) {
    if (p.length % 2 != 0) {
      throw new Error("Invalid param list length");
    }

    Map<String, String> pm = Maps.newHashMap();
    for (int i = 0; i < p.length; i += 2) {
      pm.put(p[i], p[i + 1]);
    }
    return pm;
  }

  private ObjectNode createItemFromJsonObject(
      ObjectNode newItem, String token, String stagingUuid, int responseCode, boolean returnError)
      throws IOException {
    final HttpResponse response;
    if (stagingUuid != null) {
      response = postItem(newItem.toString(), false, token, "file", stagingUuid);
    } else {
      response = postItem(newItem.toString(), false, token);
    }
    ObjectNode error = null;
    if (returnError) {
      error = (ObjectNode) mapper.readTree(response.getEntity().getContent());
    } else {
      EntityUtils.consume(response.getEntity());
    }
    assertResponse(response, responseCode, responseCode + " not returned from item creation");
    return error;
  }

  @Nullable
  public ObjectNode getAttachment(ObjectNode item, String uuid) {
    ArrayNode attachments = (ArrayNode) item.get("attachments");
    for (JsonNode attachment : attachments) {
      ObjectNode att = (ObjectNode) attachment;
      if (att.get("uuid").asText().equals(uuid)) {
        return att;
      }
    }
    return null;
  }

  @AfterMethod
  public void deleteItem() throws Exception {
    if (context == null) {
      return;
    }
    String token;

    token = getToken();

    final HttpResponse response = deleteItem(ITEM_UUID, 1, token, "purge", "true");
    EntityUtils.consume(response.getEntity());
  }

  @Test
  public void testEditMetadata() throws Exception {
    // get an item
    final String token = getToken();
    createItem(token, true);
    ObjectNode item = getItem(ITEM_UUID, 1, "metadata", token);

    // modifiy the value in the name xpath
    final String xml = item.get("metadata").asText();
    final PropBagEx meta = new PropBagEx(xml);
    meta.setNode("item/name", "New name");
    item.put("metadata", meta.toString());

    // save it
    putItem(ITEM_UUID, 1, item.toString(), token);

    // get it again, ensure the *name* has been modified as well as the XML
    item = getItem(ITEM_UUID, 1, "basic", token);

    asserter.assertBasic(item, "New name", DESCRIPTION);
  }

  @Test
  public void testEditOwner() throws Exception {
    // create item
    final String token = getToken();
    createItem(token);
    // get item
    ObjectNode item = getItem(ITEM_UUID, 1, "detail", token);

    // modifiy the value in the name xpath
    // final String oldOwner = item.get("owner").asText();
    final String newOwner = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
    item.put("owner", newOwner);

    // save it
    putItem(ITEM_UUID, 1, item.toString(), token);

    // get it again, ensure the *name* has been modified as well as the XML
    item = getItem(ITEM_UUID, 1, "detail", token);

    asserter.assertUser(item.get("owner"), newOwner);
  }

  @Test
  public void testEditDRM() throws Exception {
    final String token = getToken();
    createDrmItem(token);
    // get item
    ObjectNode item = getItem(ITEM_UUID, 1, "drm,basic", token);

    String itemName = item.get("name").asText();
    String itemUuid = item.get("uuid").asText();
    int itemVersion = item.get("version").asInt();

    logon("AutoTest", "automated");
    context.getDriver().get(context.getBaseUrl() + "items/" + itemUuid + "/" + itemVersion);
    assertTrue(new DRMAgreementPage(context).get().hasTerms("Verifiable use statement"));

    // modify some of the DRM settings and save it
    ObjectNode drmNode = (ObjectNode) item.get("drm").get("options");
    drmNode.put("termsOfAgreement", "A different verifiable statement");
    putItem(ITEM_UUID, 1, item.toString(), token);

    // check DRM has changed
    logon("AutoTest", "automated");
    context.getDriver().get(context.getBaseUrl() + "items/" + itemUuid + "/" + itemVersion);
    assertTrue(new DRMAgreementPage(context).get().hasTerms("A different verifiable statement"));

    // remove drm completely
    item.put("drm", item.objectNode());

    // save it
    putItem(ITEM_UUID, 1, item.toString(), token);

    // get it, check DRM has changed
    item = getItem(ITEM_UUID, 1, "drm", token);

    logon("AutoTest", "automated");
    context.getDriver().get(context.getBaseUrl() + "items/" + itemUuid + "/" + itemVersion);
    assertEquals(new SummaryPage(context).get().getItemTitle(), itemName);
  }

  @Test
  public void testEditNavigation() throws Exception {
    final String token = getToken();
    createItem(token, true);
    // get item
    ObjectNode item = getItem(ITEM_UUID, 1, "navigation", token);

    ObjectNode nav = (ObjectNode) item.get("navigation");
    assertFalse(nav.get("hideUnreferencedAttachments").asBoolean());
    ArrayNode nodes = (ArrayNode) nav.get("nodes");
    assertFirstNavNode(nodes, "avatar.png");

    // modify navigation
    ObjectNode node = (ObjectNode) nodes.get(0);
    node.put("name", "An awesome image");
    nav.put("hideUnreferencedAttachments", true);
    putItem(ITEM_UUID, 1, item.toString(), token);

    // get item again
    nav = (ObjectNode) getItem(ITEM_UUID, 1, "all", token).get("navigation");
    JsonNode newNodes = nav.get("nodes");
    assertTrue(nav.get("hideUnreferencedAttachments").asBoolean());
    assertFirstNavNode(newNodes, "An awesome image");
  }

  @Test
  public void testEditAttachments() throws Exception {
    final String token = getToken();
    createItem(token, true);
    // get item
    ObjectNode item = getItem(ITEM_UUID, 1, "attachment", token);
    ArrayNode attachments = (ArrayNode) item.get("attachments");
    ObjectNode urlAttachment = (ObjectNode) attachments.get(2);
    assertUrlAttachment(urlAttachment, ITEM_ID);
    asserter.assertViewerAndPreview(urlAttachment, null, false);

    // modify it
    urlAttachment.put("viewer", "file");
    urlAttachment.put("preview", true);
    urlAttachment.put("description", "Yahoo");
    urlAttachment.put("url", "http://www.yahoo.com");

    // save it
    putItem(ITEM_UUID, 1, item.toString(), token);

    // get it again
    urlAttachment =
        (ObjectNode) getItem(ITEM_UUID, 1, "attachment", token).get("attachments").get(2);

    assertUrlAttachment(urlAttachment, ITEM_ID, "Yahoo", "http://www.yahoo.com");
    asserter.assertViewerAndPreview(urlAttachment, "file", true);
  }

  @Test
  public void testEditEverything() throws Exception {
    final String token = getToken();
    createItem(token);
    // get item
    ObjectNode item = getItem(ITEM_UUID, 1, "all", token);

    // check all nodes
    assertNameVersionStatus(item, "ItemApiEditTest - All attachments from JSON", 1, "live");
    assertMetadata(item, "item/name", "ItemApiEditTest - All attachments from JSON");
    asserter.assertUser(item.get("owner"), USERID_AUTOTEST);
    asserter.assertUser(item.get("collaborators").get(0), USERID_RESTNOPRIV);
    asserter.assertCollection(item.get("collection"), COLLECTION_ATTACHMENTS);
    assertUrlAttachment(item.get("attachments").get(2), ITEM_ID);
    assertFirstNavNode(item.get("navigation").get("nodes"), "avatar.png");

    // modify metadata
    PropBagEx metadata = new PropBagEx(item.get("metadata").asText());
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
    HttpResponse response = putItem(getItemUri(ITEM_UUID, "1"), item.toString(), token, false);
    try {
      assertResponse(
          response, 200, "Save should work, but didn't: " + superSerialResponse(response));
    } finally {
      EntityUtils.consume(response.getEntity());
    }

    // check that its correct
    item = getItem(ITEM_UUID, 1, "all", token);
    assertNameVersionStatus(
        item, "ItemApiEditTest - All attachments from JSON - Edited", 1, "live");
    assertMetadata(item, "item/name", "ItemApiEditTest - All attachments from JSON - Edited");
    asserter.assertUser(item.get("owner"), USERID_RESTNOPRIV);
    asserter.assertUser(item.get("collaborators").get(0), USERID_AUTOTEST);

    // Future enhancement
    // asserter.assertCollection(item.get("collection"),
    // PERMISSIONS_COLLECTION);

    assertUrlAttachment(item.get("attachments").get(2), ITEM_ID, "Yahoo", "http://www.yahoo.com");
    assertFirstNavNode(item.get("navigation").get("nodes"), "An awesome image");
  }

  @Test
  public void testEditNoPrivs() throws Exception {
    PropBagEx metadata =
        new PropBagEx(
            "<xml><item><name>ItemApiEditTest - Permissions from"
                + " JSON</name><description>Description</description><controls></controls></item></xml>");
    PropBagEx permissions = metadata.aquireSubtree("item/controls");

    final String token = getToken();

    ObjectNode item = createItemObject(COLLECTION_PERMISSIONS, metadata, false);

    HttpResponse response = postItem(item.toString(), token);
    assertResponse(response, 201, "201 not returned from item creation");

    item = getItem(ITEM_UUID, 1, "all", token);
    assertNameVersionStatus(item, "ItemApiEditTest - Permissions from JSON", 1, "live");

    // revoke change owner
    permissions.setNode("checkboxes", "REASSIGN_OWNERSHIP_ITEM");
    item.put("metadata", metadata.toString());
    response = putItem(ITEM_UUID, 1, item.toString(), token);
    assertResponse(response, 200, "200 not returned from item editing");

    item.put("owner", USERID_RESTNOPRIV);
    ObjectNode error = putItemError(ITEM_UUID, 1, item.toString(), token, 403);
    assertError(
        error,
        403,
        "Forbidden",
        "Privilege REASSIGN_OWNERSHIP_ITEM is required to perform the requested operation");
    item = getItem(ITEM_UUID, 1, "all", token);
    ArrayNode collabs = item.arrayNode();
    collabs.add(USERID_RESTNOPRIV);
    collabs.add(USERID_AUTOTEST);
    item.put("collaborators", collabs);
    error = putItemError(ITEM_UUID, 1, item.toString(), token, 403);
    assertError(
        error,
        403,
        "Forbidden",
        "Privilege REASSIGN_OWNERSHIP_ITEM is required to perform the requested operation");
    item = getItem(ITEM_UUID, 1, "all", token);

    // revoke edit
    permissions.setNode("checkboxes", "EDIT_ITEM");
    item.put("metadata", metadata.toString());
    response = putItem(ITEM_UUID, 1, item.toString(), token);
    assertResponse(response, 200, "200 not returned from item editing");

    // try to change the name
    PropBagEx editNameMetadata = new PropBagEx(metadata.toString());
    editNameMetadata.setNode("item/name", "ItemApiEditTest - Permissions from JSON - Edited");
    item.put("metadata", editNameMetadata.toString());
    error = putItemError(ITEM_UUID, 1, item.toString(), token, 403);
    assertError(
        error,
        403,
        "Forbidden",
        "Privilege EDIT_ITEM is required to perform the requested operation");

    // try to edit drm
    item.put("metadata", metadata.toString());
    item.with("drm").with("options").put("termsOfAgreement", "Terms");
    error = putItemError(ITEM_UUID, 1, item.toString(), token, 403);
    assertError(
        error,
        403,
        "Forbidden",
        "Privilege EDIT_ITEM is required to perform the requested operation");

    item = getItem(ITEM_UUID, 1, "all", token);
    assertNameVersionStatus(item, "ItemApiEditTest - Permissions from JSON", 1, "live");
  }

  @Test
  public void testPOSTToExisting() throws Exception {
    final String token = getToken();

    // Create the item
    createItem(token);

    // POST to existing item URL
    ObjectNode error =
        createItemFromJsonFile("newitem.json", token, null, 400, true, standardParams());

    // ensure error
    assertError(
        error, 400, "Bad Request", "Version 1 of the item '" + ITEM_UUID + "' already exists");
  }

  @Test
  public void testThumbnailFieldsReadWrite() throws Exception {
    final String token = getToken();

    // Create the item, first image suppressed thumb, other not.
    String[] stagingParams = createStaging();

    String stagingDirUrl = stagingParams[1];
    uploadFile(stagingDirUrl, "avatar.png", Attachments.get("avatar.png"));
    uploadFile(stagingDirUrl, "shopimage.jpeg", Attachments.get("shopimage.jpeg"));

    ObjectNode item =
        (ObjectNode) mapper.readTree(readJsonFile("thumbitem.json", standardParams()));
    item.put("thumbnail", "custom:6f80e4aa-560d-4977-9020-366279f82280");
    // suppress first attachment (avatar) which would have been the default thumb
    ObjectNode attachment = getAttachment(item, "63862d54-1b6d-4dce-9a79-44b3a8c9e107");
    attachment.put("thumbnail", "suppress");

    createItemFromJsonObject(item, token, stagingParams[0], 201, false);
    // if you want to break here you can check the UI to ensure the shop thumb is the gallery icon
    // (via explicit selection)
    item = getItem(ITEM_UUID, 1, "all", token);
    String thumb = item.get("thumbnail").asText();
    assertEquals(thumb, "custom:6f80e4aa-560d-4977-9020-366279f82280", "Item thumbnail incorrect");

    // get attachment with UUID (avatar) and assert suppress thumb
    attachment = getAttachment(item, "63862d54-1b6d-4dce-9a79-44b3a8c9e107");
    assertNotNull(attachment, "Can't find attachment");
    assertEquals(attachment.get("thumbnail").asText(), "suppress", "Attachment thumb incorrect");

    attachment = getAttachment(item, "6f80e4aa-560d-4977-9020-366279f82280");
    assertNotNull(attachment, "Can't find attachment");
    // auto generated thumb
    assertEquals(
        attachment.get("thumbnail").asText(),
        "_THUMBS/shopimage.jpeg.jpeg",
        "Attachment thumb incorrect");

    // set item to default thumb, suppress number 2 and unsupress number 1
    item.put("thumbnail", "default");
    attachment = getAttachment(item, "63862d54-1b6d-4dce-9a79-44b3a8c9e107");
    attachment.remove("thumbnail");
    attachment = getAttachment(item, "6f80e4aa-560d-4977-9020-366279f82280");
    attachment.put("thumbnail", "suppress");

    putItem(ITEM_UUID, 1, item.toString(), token);
    // if you want to break here you can check the UI to ensure the shop thumb is the avatar icon
    // (via "default" selection)

    item = getItem(ITEM_UUID, 1, "all", token);
    thumb = item.get("thumbnail").asText();
    assertEquals(thumb, "default", "Item thumbnail incorrect");
    attachment = getAttachment(item, "63862d54-1b6d-4dce-9a79-44b3a8c9e107");
    assertEquals(attachment.get("thumbnail").asText(), "_THUMBS/avatar.png.jpeg");
    attachment = getAttachment(item, "6f80e4aa-560d-4977-9020-366279f82280");
    assertEquals(attachment.get("thumbnail").asText(), "suppress");
  }

  @Test
  public void testPUTToNonExisting() throws Exception {
    final String token = getToken();

    // Create the item
    createItem(token);

    ObjectNode item = getItem(ITEM_UUID, 1, "basic", token);

    // ensure saving works
    HttpResponse response = putItem(ITEM_UUID, 1, item.toString(), token);
    assertResponse(response, 200, "Should be editable");

    // Try to save the item to the wrong uuid
    response = putItem("aaaaaaaa-6755-11e1-b733-bc0a4924019b", 1, item.toString(), token);
    assertResponse(response, 404, "Cant edit a non-existing item");
  }

  @Test
  public void testOnePermissionAllowedButNotTheOther() throws Exception {
    final String token = getToken();

    PropBagEx metadata =
        new PropBagEx(
            "<xml><item><name>ItemApiEditTest - Permissions from"
                + " JSON</name><description>Description</description><controls></controls></item></xml>");
    PropBagEx permissions = metadata.aquireSubtree("item/controls");
    ObjectNode item = createItemObject(COLLECTION_PERMISSIONS, metadata, true);

    HttpResponse response = postItem(item.toString(), token);
    assertResponse(response, 201, "201 not returned from item creation");

    item = getItem(ITEM_UUID, 1, "all", token);
    assertTrue(item.has("attachments"));
    assertEquals(item.get("attachments").get(0).get("url").asText(), "http://google.com.au/");

    // revoke view item
    permissions.createNode("checkboxes", "VIEW_ITEM");
    item.put("metadata", metadata.toString());
    response = putItem(ITEM_UUID, 1, item.toString(), token, "waitforindex", true);
    assertResponse(response, 200, "200 not returned from item creation");

    // make sure we cant see the attachments
    item = getItem(ITEM_UUID, 1, "all", token);
    assertFalse(item.has("attachments"));

    // ensure discoverable
    JsonNode searchResults = doSearch("ItemApiEditTest - Permissions from JSON", token);
    assertEquals(searchResults.get("available").asInt(), 1);

    // restore view item and revoke discover
    permissions.setNode("checkboxes", "DISCOVER_ITEM");
    item.put("metadata", metadata.toString());
    response = putItem(ITEM_UUID, 1, item.toString(), token, "waitforindex", true);
    assertResponse(response, 200, "200 not returned from item creation");

    // make sure we can see attachments
    item = getItem(ITEM_UUID, 1, "all", token);
    assertTrue(item.has("attachments"));
    assertEquals(item.get("attachments").get(0).get("url").asText(), "http://google.com.au/");

    // ensure not discoverable
    searchResults = doSearch("ItemApiEditTest - Permissions from JSON", token);
    assertEquals(searchResults.get("available").asInt(), 0);
  }

  private HttpResponse lock(String uuid, int version, String token, int assertCode)
      throws Exception {
    final HttpPost request =
        new HttpPost(context.getBaseUrl() + "api/item/" + ITEM_UUID + "/" + 1 + "/lock");
    final HttpResponse response = execute(request, false, token);
    assertResponse(response, assertCode, "Lock response was not " + assertCode);
    return response;
  }

  private void unlock(String uuid, int version, String token, int assertCode) throws Exception {
    final HttpDelete request =
        new HttpDelete(context.getBaseUrl() + "api/item/" + ITEM_UUID + "/" + 1 + "/lock");
    final HttpResponse response = execute(request, true, token);
    assertResponse(response, assertCode, "Unlock response was not " + assertCode);
  }

  private HttpResponse getLock(String uuid, int version, String token, int assertCode)
      throws Exception {
    final HttpGet request =
        new HttpGet(context.getBaseUrl() + "api/item/" + ITEM_UUID + "/" + 1 + "/lock");
    final HttpResponse response = execute(request, false, token);
    return response;
  }

  @Test
  public void testLockAndSaveWithNoLock() throws Exception {
    final String token = getToken();
    createItem(token);

    // lock it
    HttpResponse response = lock(ITEM_UUID, 1, token, 201);
    EntityUtils.consume(response.getEntity());

    // get it
    final ObjectNode item = getItem(ITEM_UUID, 1, "detail", token);

    // try to PUT it with no lock param
    response = putItem(ITEM_UUID, 1, item.toString(), token);
    try {
      // asert 409 (conflict) error code
      assertResponse(response, 409, "Save response with missing lock did not return " + 409);
    } finally {
      // unlock it
      unlock(ITEM_UUID, 1, token, 204);
    }
  }

  @Test
  public void testLockAndSaveWithLock() throws Exception {
    final String token = getToken();
    createItem(token);

    // lock it
    final ObjectNode lock = readJson(mapper, lock(ITEM_UUID, 1, token, 201));

    // get it
    final ObjectNode item = getItem(ITEM_UUID, 1, "detail", token);

    // try to PUT it with lock param
    HttpResponse response =
        putItem(ITEM_UUID, 1, item.toString(), token, "lock", lock.get("uuid").asText());
    assertResponse(response, 200, "200 not returned from legit lock usage");

    // ensure lock is no longer there
    unlock(ITEM_UUID, 1, token, 404);
    response = getLock(token, 1, token, 404);
    EntityUtils.consume(response.getEntity());
  }

  @Test
  public void testLockALockedItem() throws Exception {
    final String token = getToken();
    createItem(token);

    // lock it
    HttpResponse response = lock(ITEM_UUID, 1, token, 201);
    EntityUtils.consume(response.getEntity());

    // try to lock it again as a different user
    final String otherToken = requestToken(OAUTH_OTHER_CLIENT_ID);
    try {
      response = lock(ITEM_UUID, 1, otherToken, 409); // 409 == conflict
      EntityUtils.consume(response.getEntity());
    } finally {
      // unlock it
      unlock(ITEM_UUID, 1, token, 204);
    }
  }

  @Test
  public void testAutoUuidGeneration() throws Exception {
    final String token = getToken();
    PropBagEx metadata =
        new PropBagEx(
            "<xml><item><name>ItemApiEditTest - Auto UUID generation from"
                + " JSON</name><description>Description</description><controls><editbox>uuid:0</editbox></controls></item></xml>");
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

    HttpResponse response = postItem(item.toString(), token);
    assertResponse(response, 201, "201 Created was expected");

    item = getItem(ITEM_ID, "all", token);

    String attachmentUuid = item.get("attachments").findValue("uuid").asText();
    assertFalse(
        attachmentUuid.equals("uuid:0"), "The attachment should have been given a correct uuid");
    String navAttachmentUuid = item.get("navigation").findValue("$ref").asText();
    assertEquals(attachmentUuid, navAttachmentUuid, "The uuids should be the same");

    attachmentRef = (ObjectNode) navigation.findValue("attachment");
    attachmentRef.put("$ref", "uuid:0");
    item.put("navigation", navigation);
    ObjectNode error = putItemError(ITEM_UUID, 1, item.toString(), token, 400);
    assertError(
        error,
        400,
        "Bad Request",
        "Trying to associate navigation node tab with attachment which doesn't exist: node:.*"
            + " attachment:uuid:0",
        true);

    ArrayNode attachments = (ArrayNode) item.get("attachments");
    ObjectNode attachment = (ObjectNode) item.get("attachments").get(0);
    attachment.put("uuid", "uuid:0");
    attachments.add(attachment);

    error = putItemError(ITEM_UUID, 1, item.toString(), token, 400);
    assertError(
        error, 400, "Bad Request", "Another attachment is already using this placeholder: uuid:0");
  }

  @Test
  public void editScormAndZip() throws IOException, Exception {
    String token = getToken();
    createItem(token);
    ObjectNode item = getItem(ITEM_ID, null, token);
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
    item = editItem(item, token);
    attachments = (ArrayNode) item.get("attachments");

    scormAtt = (ObjectNode) attachments.get(index);
    asserter.assertAttachmentBasics(scormAtt, ITEM_ID, "scorm", null, "scorm-desc");
    assertEquals(scormAtt.get("packageFile").asText(), "nonscorm.zip");

    zipAtt = (ObjectNode) attachments.get(index + 1);
    asserter.assertAttachmentBasics(zipAtt, ITEM_ID, "zip", null, "zip-desc");
    assertEquals(zipAtt.get("folder").asText(), "non-existant");

    scormResAtt = (ObjectNode) attachments.get(index + 2);
    asserter.assertAttachmentBasics(scormResAtt, ITEM_ID, "scorm-res", null, "scorm-res-desc");
    assertEquals(scormResAtt.get("filename").asText(), "file");

    attachments.remove(index + 2);
    item = editItem(item, token);
    assertEquals(item.get("attachments").size(), index + 2);
  }

  private void assertFirstNavNode(JsonNode parentNode, String title) {
    asserter.assertNavNode(
        parentNode,
        0,
        "49ffd9d1-cc47-4fce-ae49-897ca0a54024",
        title,
        "Tab 1",
        "63862d54-1b6d-4dce-9a79-44b3a8c9e107",
        "");
  }

  private void assertError(JsonNode error, int code, String title, String message) {
    assertError(error, code, title, message, false);
  }

  private void assertError(JsonNode error, int code, String title, String message, boolean regex) {
    assertEquals(error.get("code").asInt(), code);
    assertEquals(error.get("error").asText(), title);
    String errorDescription = error.get("error_description").asText();
    if (regex) {
      Pattern pattern = Pattern.compile(message);
      Matcher matcher = pattern.matcher(errorDescription);
      assertTrue(matcher.matches(), "Was expencting " + message + " to match " + errorDescription);
    } else {
      assertEquals(errorDescription, message);
    }
  }

  private ObjectNode createItemObject(
      String collectionUuid, PropBagEx metadata, boolean withAttachment) {
    ObjectNode item = mapper.createObjectNode();

    item.put("uuid", ITEM_UUID);
    item.put("version", 1);
    ObjectNode collection = item.objectNode();
    collection.put("uuid", collectionUuid);
    item.put("collection", collection);
    item.put("metadata", metadata.toString());
    if (withAttachment) {
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

  private JsonNode doSearch(String itemName, String token) throws Exception {
    HttpGet get =
        new HttpGet(
            context.getBaseUrl() + "api/search?" + queryString("q", "\"" + itemName + "\""));
    HttpResponse response = execute(get, false, token);
    return mapper.readTree(response.getEntity().getContent());
  }
}
