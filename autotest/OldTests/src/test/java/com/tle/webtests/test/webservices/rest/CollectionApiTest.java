package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.tle.common.Pair;
import java.net.URI;
import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

public class CollectionApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "CollectionApiTestClient";
  private static final String API_COLLECTION_PATH = "api/collection";
  private static final String CREATE_ITEM = "CREATE_ITEM";
  private static final String SEARCH_COLLECTION = "SEARCH_COLLECTION";
  private static final String BASIC_ITEMS_COLLECTION_UUID = "b28f1ffe-2008-4f5e-d559-83c8acd79316";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void viewContributableCollectionTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_COLLECTION_PATH);
    JsonNode result = getEntity(uri.toString(), token, "privilege", CREATE_ITEM);
    // being wary of composing an unneeded string with a null pointer
    assertNull(
        result.get("error"),
        "Resonse unexpected: "
            + (result.get("error") != null ? result.get("error").asText() : "((pro forma))"));
    int available = result.get("available").asInt();
    assertTrue(available >= 11);
  }

  @Test
  public void viewSearchableCollectionTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_COLLECTION_PATH);
    JsonNode result = getEntity(uri.toString(), token, "privilege", SEARCH_COLLECTION);
    // being wary of composing an unneeded string with a null pointer
    assertNull(
        result.get("error"),
        "Resonse unexpected: "
            + (result.get("error") != null ? result.get("error").asText() : "((pro forma))"));
    int available = result.get("available").asInt();
    assertTrue(available >= 11);
  }

  // FIXME: institution needs VIEW_COLLECTION priv
  @Test
  public void viewSingleCollectionTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri =
        new URI(context.getBaseUrl() + API_COLLECTION_PATH + "/" + BASIC_ITEMS_COLLECTION_UUID);
    JsonNode result = getEntity(uri.toString(), token);
    String available = result.get("name").asText();
    assertEquals(available, "Basic Items");
  }
}
