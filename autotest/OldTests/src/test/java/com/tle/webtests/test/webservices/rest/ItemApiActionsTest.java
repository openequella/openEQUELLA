package com.tle.webtests.test.webservices.rest;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.viewitem.ItemId;
import java.io.IOException;
import java.util.List;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

public class ItemApiActionsTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "ItemApiActionsTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  private String createSimpleItem() throws IOException {
    String token = getToken();
    ObjectNode item = createItemJson(COLLECTION_ATTACHMENTS);
    item = createItem(item.toString(), token);
    asserter.assertStatus(item, "live");
    ItemId itemId = addDeletable(item);
    return getItemUri(itemId);
  }

  @Test
  public void testRedraft() throws IOException {
    String token = getToken();
    String itemUri = createSimpleItem();
    assertResponse(itemAction(itemUri, "redraft", token), 200, "Couldn't redraft");
    ObjectNode item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "draft");
  }

  @Test
  public void testArchive() throws IOException {
    String token = getToken();
    String itemUri = createSimpleItem();
    assertResponse(itemAction(itemUri, "archive", token), 200, "Couldn't redraft");
    ObjectNode item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "archived");
    assertResponse(itemAction(itemUri, "reactivate", token), 200, "Couldn't reactivate");
    item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "live");
  }

  @Test
  public void testSuspend() throws IOException {
    String token = getToken();
    String itemUri = createSimpleItem();
    assertResponse(itemAction(itemUri, "suspend", token), 200, "Couldn't suspend");
    ObjectNode item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "suspended");
    assertResponse(itemAction(itemUri, "resume", token), 200, "Couldn't resume");
    item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "live");
  }

  @Test
  public void testRestore() throws IOException {
    String token = getToken();
    String itemUri = createSimpleItem();
    assertResponse(deleteResource(itemUri, token), 204, "Couldn't delete");
    ObjectNode item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "deleted");
    assertResponse(itemAction(itemUri, "restore", token), 200, "Couldn't restore");
    item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "live");
  }

  @Test
  public void testReset() throws IOException {
    ObjectNode item = createItemJson(COLLECTION_MODERATE);
    String token = getToken();
    item = createItem(item.toString(), token);
    asserter.assertStatus(item, "live");
    ItemId itemId = addDeletable(item);
    String itemUri = getItemUri(itemId);

    // Edit and turn on moderation
    PropBagEx metadata = new PropBagEx();
    metadata.setNode("item/controls/checkboxes", true);
    item.put("metadata", metadata.toString());
    assertResponse(putItem(itemUri, item.toString(), token, true), 200, "Couldn't edit item");
    item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "live");

    assertResponse(itemAction(itemUri, "reset", token), 200, "Couldn't reset");
    item = getItem(itemUri, null, token);
    asserter.assertStatus(item, "moderating");
  }
}
