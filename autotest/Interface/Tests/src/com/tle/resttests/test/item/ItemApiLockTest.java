package com.tle.resttests.test.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.util.RestTestConstants;
import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ItemApiLockTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "ItemApiLockTestClient";
  private String token;
  private String itemUuid;
  private int itemVersion;
  private String itemUri;

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
  }

  private HttpResponse lockItemError(String token, String itemUri) throws IOException {
    HttpPost lockPost = new HttpPost(PathUtils.urlPath(itemUri, "lock"));
    return execute(lockPost, true, token);
  }

  private JsonNode lockItem(String token, String itemUri) throws IOException {
    HttpPost lockPost = new HttpPost(PathUtils.urlPath(itemUri, "lock"));
    HttpResponse response = execute(lockPost, false, token);
    try {
      assertResponse(response, 201, "Should have locked item");
      return mapper.readTree(response.getEntity().getContent());
    } finally {
      EntityUtils.consume(response.getEntity());
    }
  }

  private void createLockItem() throws Exception {
    token = requestToken(OAUTH_CLIENT_ID);
    ObjectNode node = mapper.createObjectNode();
    node.with("collection").put("uuid", "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c");
    HttpResponse response = postItem(node.toString(), token);
    assertResponse(response, 201, "Expected item to be created");
    itemUri = response.getFirstHeader("Location").getValue();
    ObjectNode newItem = getItem(itemUri, null, token);
    itemUuid = newItem.get("uuid").asText();
    itemVersion = newItem.get("version").asInt();
  }

  @AfterMethod
  public void cleanupItem() throws IOException {
    deleteItem(itemUuid, itemVersion, token, "purge", true);
  }

  @Test
  public void edit() throws Exception {
    createLockItem();

    ObjectNode itemJson = getItem(itemUri, null, token);
    HttpResponse response = putItem(itemUri, itemJson.toString(), token);
    assertResponse(response, 200, "Should be editable");

    JsonNode lockJson = lockItem(token, itemUri);
    String lockId = lockJson.get("uuid").asText();

    response = putItem(itemUri, itemJson.toString(), token);
    assertResponse(response, 409, "Should not be able to edit without supplying lock");

    response = putItem(itemUri, itemJson.toString(), token, "lock", lockId);
    assertResponse(response, 200, "Should be editable with lock");

    lockJson = lockItem(token, itemUri);
    lockId = lockJson.get("uuid").asText();

    assertResponse(lockItemError(token, itemUri), 409, "Shouldn't be able to lock again");
  }
}
