package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.webtests.test.files.Attachments;
import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

public class ScrapbookApiTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "ScrapbookApiTest";
  private static final String TYPE_FILE = "myresource";
  private static final String TYPE_PAGE = "mypages";
  private List<String> itemUuids = Lists.newArrayList();

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  private ObjectNode buildScrapbookItem(String type, String filename, String fileUuid) {
    ObjectNode node = mapper.createObjectNode();
    node.put("title", "scrapbookItemTestTitle" + type);
    node.put("type", type);
    node.put("keywords", "key word");
    if (type.equals(TYPE_PAGE)) {
      ArrayNode pages = mapper.createArrayNode();
      ObjectNode page1 = mapper.createObjectNode();
      page1.put("title", "page1");
      page1.put("html", "<h2>page 1 header</h2>");
      pages.add(page1);
      ObjectNode page2 = mapper.createObjectNode();
      page2.put("title", "page2");
      page2.put("html", "<h2>Page 2 header</h2>");
      pages.add(page2);
      node.put("pages", pages);
    } else {
      ObjectNode file = mapper.createObjectNode();
      file.put("stagingUuid", fileUuid);
      file.put("filename", filename);
      node.put("file", file);
    }

    return node;
  }

  private String createScrapbookItem(String type, String filename)
      throws ClientProtocolException, IOException {
    String stagingUuid = null;
    if (type.equals(TYPE_FILE)) {
      String[] stagingParams = createStaging();
      stagingUuid = stagingParams[0];
      String stagingDirUrl = stagingParams[1];
      uploadFile(stagingDirUrl, filename, Attachments.get(filename));
    }
    ObjectNode node = buildScrapbookItem(type, filename, stagingUuid);
    HttpResponse response =
        postEntity(node.toString(), context.getBaseUrl() + "api/scrapbook", getToken(), true);
    assertResponse(response, 201, "failed to create scrapbook item");
    return response.getFirstHeader("Location").getValue();
  }

  private ObjectNode getCreatedScrapbookItem(String type, String filename)
      throws ClientProtocolException, IOException {
    String itemUrl = createScrapbookItem(type, filename);
    ObjectNode node = (ObjectNode) getEntity(itemUrl, getToken());
    itemUuids.add(node.get("uuid").asText());
    return node;
  }

  private HttpResponse deleteScrapbookItem(String uuid) throws IOException {
    String url = context.getBaseUrl() + "api/scrapbook/" + uuid;
    return deleteResource(url, getToken());
  }

  @Test
  public void testCreateScrapbookItem() throws Exception {
    String filename = "avatar.png";
    ObjectNode node = getCreatedScrapbookItem(TYPE_FILE, filename);
    assertEquals(node.get("file").get("filename").asText(), filename);
  }

  @Test
  public void testCreatePages() throws IOException {
    getCreatedScrapbookItem(TYPE_PAGE, null);
  }

  public void deleteScrapbookItem() throws Exception {
    String filename = "avatar.png";
    ObjectNode node = getCreatedScrapbookItem(TYPE_FILE, filename);
    String uuid = node.get("uuid").asText();
    HttpResponse response = deleteScrapbookItem(uuid);
    assertResponse(response, 200, "");
  }

  @Test
  public void testUpdateScrapbookItem() throws Exception {
    String filename = "B.txt";
    String itemUrl = createScrapbookItem(TYPE_FILE, filename);
    ObjectNode node = (ObjectNode) getEntity(itemUrl, getToken());
    assertEquals(node.get("file").get("filename").asText(), filename);

    String uuid = node.get("file").get("uuid").asText();
    itemUrl = node.get("links").get("self").asText();
    itemUuids.add(uuid);
    node.remove("title");
    node.remove("file");
    node.remove("links");
    node.remove("pages");
    String newTitle = "new title";
    node.put("title", "new title");
    HttpResponse response = putItem(itemUrl, node.toString(), getToken(), true);
    assertResponse(response, 200, "");
    itemUrl = response.getFirstHeader("Location").getValue();
    ObjectNode newNode = (ObjectNode) getEntity(itemUrl, getToken());
    assertEquals(newNode.get("title").asText(), newTitle);
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    for (String uuid : itemUuids) {
      try {
        deleteScrapbookItem(uuid);
      } catch (Exception ex) {

      }
    }
    super.cleanupAfterClass();
  }
}
