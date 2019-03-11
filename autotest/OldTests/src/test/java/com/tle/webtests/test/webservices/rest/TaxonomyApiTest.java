package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.tle.common.Pair;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

public class TaxonomyApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "TaxonomyApiTestClient";
  private static final String TAXONOMY_UUID = "a8475ae1-0382-a258-71c3-673e4597c3d2";
  private static final String TERM_1_UUID = "abbd2610-1c3e-489a-a107-1c16fa22b0a0";
  private static final String API_TAXONOMY_PATH = "api/taxonomy";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testGetTerm() {
    JsonNode result;
    try {
      String token = requestToken(OAUTH_CLIENT_ID);
      URI uri =
          new URI(
              context.getBaseUrl()
                  + API_TAXONOMY_PATH
                  + "/"
                  + TAXONOMY_UUID
                  + "/term/"
                  + TERM_1_UUID);
      result = getEntity(uri.toString(), token);
      assertEquals(result.get("uuid").asText(), TERM_1_UUID);
    } catch (Exception e) {
    }
  }

  @Test
  public void testInsertAndDeleteTerm() throws IOException, URISyntaxException {
    unlock();
    lock();
    ObjectNode node = mapper.createObjectNode();
    node.put("index", 0);
    String termValue = "TEST TERM NODE";
    node.put("term", termValue);
    HttpResponse response =
        postEntity(
            node.toString(),
            context.getBaseUrl() + API_TAXONOMY_PATH + "/" + TAXONOMY_UUID + "/term",
            getToken(),
            true);
    assertResponse(response, 201, "failed to create term");
    String termUrl = response.getFirstHeader("Location").getValue();
    JsonNode termNode = getEntity(termUrl, getToken());
    assertEquals(termValue, termNode.get("term").asText());
    response = deleteResource(termUrl, getToken());
    assertResponse(response, 200, "failed to delete term");

    unlock();
  }

  @Test
  public void testMoveTerm() throws IOException, URISyntaxException {
    unlock();
    lock();
    ObjectNode node = mapper.createObjectNode();
    node.put("index", 0);
    String termValue = "TEST TERM NODE";
    node.put("term", termValue);
    HttpResponse response =
        postEntity(
            node.toString(),
            context.getBaseUrl() + API_TAXONOMY_PATH + "/" + TAXONOMY_UUID + "/term",
            getToken(),
            true);
    assertResponse(response, 201, "failed to create term");
    String termUrl = response.getFirstHeader("Location").getValue();

    JsonNode termNode = getEntity(termUrl, getToken());
    assertEquals(termValue, termNode.get("term").asText());

    node.put("parentUuid", TERM_1_UUID);
    String putUrl =
        context.getBaseUrl()
            + API_TAXONOMY_PATH
            + "/"
            + TAXONOMY_UUID
            + "/term/"
            + termNode.get("uuid").getTextValue();
    HttpResponse putRequest = getPut(putUrl, node, getToken());
    assertResponse(putRequest, 200, "failed to update term");

    response = deleteResource(termUrl, getToken());
    assertResponse(response, 200, "failed to delete term");

    unlock();
  }

  @Test
  public void testSearchTerms() throws IOException {
    JsonNode entity =
        getEntity(
            context.getBaseUrl() + API_TAXONOMY_PATH + "/" + TAXONOMY_UUID + "/search",
            getToken(),
            "q",
            "*REST*",
            "restriction",
            "UNRESTRICTED",
            "limit",
            20);
    int length = entity.get("length").asInt();
    assertEquals(length, 5);
  }

  private void unlock() throws IOException {
    String uri = context.getBaseUrl() + API_TAXONOMY_PATH + "/" + TAXONOMY_UUID + "/lock";
    deleteResource(uri, getToken(), "force", true);
  }

  private void lock() throws IOException, URISyntaxException {
    String uri = context.getBaseUrl() + API_TAXONOMY_PATH + "/" + TAXONOMY_UUID + "/lock";
    final HttpPost request = new HttpPost(new URI(uri));
    execute(request, true, getToken());
  }
}
