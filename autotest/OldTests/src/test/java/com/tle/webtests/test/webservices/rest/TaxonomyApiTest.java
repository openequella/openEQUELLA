package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.tle.common.Pair;
import com.tle.common.PathUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
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

  @Test
  public void testSortRootTerms() throws IOException, URISyntaxException {
    final String taxonomyUuid = UUID.randomUUID().toString();
    // Create a new taxonomy
    createTaxononmy(taxonomyUuid, "Sort Roots Test Taxonomy");

    // Lock
    lock();

    final List<String> rootTerms = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      final String termName = randomString(8);
      rootTerms.add(termName);
      createTerm(taxonomyUuid, null, termName, null);
    }

    // These are the orders we will expect
    // Sort using same mechanism as server
    final Collator collator = Collator.getInstance(Locale.getDefault());
    collator.setStrength(Collator.PRIMARY);
    Collections.sort(rootTerms, collator);

    // Sort root
    final HttpResponse sortRootsResponse =
        postEntity(
            null,
            PathUtils.urlPath(
                context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "sortchildren"),
            getToken(),
            true);
    assertResponse(sortRootsResponse, 200, "failed to sort root terms");

    final List<String> returnedSortedRootTerms = new ArrayList<>(10);
    final ArrayNode arrayNode =
        (ArrayNode)
            getEntity(
                PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "term"),
                getToken());
    for (int i = 0; i < arrayNode.size(); i++) {
      final JsonNode node = arrayNode.get(i);
      final String termName = node.get("term").getTextValue();
      returnedSortedRootTerms.add(termName);
    }
    assertEquals(rootTerms, returnedSortedRootTerms);

    unlock();

    // Delete the taxonomy
    final String taxonomyUri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid);
    deleteResource(taxonomyUri, getToken());
  }

  @Test
  public void testSortChildTerms() throws IOException, URISyntaxException {

    final String taxonomyUuid = UUID.randomUUID().toString();
    // Create a new taxonomy
    createTaxononmy(taxonomyUuid, "Sort Children Test Taxonomy");

    // Lock
    lock();

    final List<String> rootTerms = new ArrayList<>();
    final List<String> testTermChildren = new ArrayList<>();

    final String testTermUuid = UUID.randomUUID().toString();
    final int testTermIndex = 5;
    String testTermPath = null;

    for (int i = 0; i < 10; i++) {
      final String termName = randomString(8);
      final boolean isTestTerm = (i == testTermIndex);
      final int childCount = (isTestTerm ? 100 : 8);
      final String termUuid = (isTestTerm ? testTermUuid : UUID.randomUUID().toString());
      if (isTestTerm) {
        testTermPath = termName;
      }
      rootTerms.add(termName);
      createTerm(taxonomyUuid, termUuid, termName, null);

      for (int j = 0; j < childCount; j++) {
        final String subTermName = termName + "-" + randomString(8);
        if (isTestTerm) {
          testTermChildren.add(subTermName);
        }
        createTerm(taxonomyUuid, null, subTermName, termUuid);
      }
    }

    // These are the orders we will expect
    // Sort using same mechanism as server
    final Collator collator = Collator.getInstance(Locale.getDefault());
    collator.setStrength(Collator.PRIMARY);
    Collections.sort(rootTerms, collator);
    Collections.sort(testTermChildren, collator);

    // Sort root
    final HttpResponse sortRootsResponse =
        postEntity(
            null,
            PathUtils.urlPath(
                context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "sortchildren"),
            getToken(),
            true);
    assertResponse(sortRootsResponse, 200, "failed to sort root terms");
    // Sort test terms children
    final HttpResponse sortTestTermChildrenResponse =
        postEntity(
            null,
            PathUtils.urlPath(
                context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "sortchildren"),
            getToken(),
            true,
            "path",
            testTermPath);
    assertResponse(sortTestTermChildrenResponse, 200, "failed to sort test term children");

    // Check root sorted
    final List<String> returnedSortedRootTerms = new ArrayList<>(10);
    final ArrayNode arrayNode =
        (ArrayNode)
            getEntity(
                PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "term"),
                getToken());
    for (int i = 0; i < arrayNode.size(); i++) {
      final JsonNode node = arrayNode.get(i);
      final String termName = node.get("term").getTextValue();
      returnedSortedRootTerms.add(termName);
    }
    assertEquals(rootTerms, returnedSortedRootTerms);

    // Check test terms children sorted
    final List<String> returnedSortedChildTerms = new ArrayList<>(10);
    final ArrayNode testTermSortedChildren =
        (ArrayNode)
            getEntity(
                PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "term"),
                getToken(),
                "path",
                testTermPath);
    for (int i = 0; i < testTermSortedChildren.size(); i++) {
      final JsonNode node = testTermSortedChildren.get(i);
      final String termName = node.get("term").getTextValue();
      returnedSortedChildTerms.add(termName);
    }
    assertEquals(testTermChildren, returnedSortedChildTerms);

    unlock();

    // Delete the taxonomy
    final String taxonomyUri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid);
    deleteResource(taxonomyUri, getToken());
  }

  private void createTaxononmy(String uuid, String name) throws IOException {
    final String uri = PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH);
    final ObjectNode jsonObj = mapper.createObjectNode();
    jsonObj.put("uuid", uuid);
    jsonObj.put("name", name);
    jsonObj.put("readonly", false);
    final String jsonStr = jsonObj.toString();
    HttpResponse response = postEntity(jsonStr, uri, getToken(), true);
    assertResponse(response, 201, "failed to create taxonomy");
  }

  private void createTerm(String taxonomyUuid, String termUuid, String term, String parentTermUuid)
      throws IOException {
    final String uri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "term");
    final ObjectNode jsonObj = mapper.createObjectNode();
    jsonObj.put("uuid", termUuid);
    jsonObj.put("term", term);
    jsonObj.put("parentUuid", parentTermUuid);
    final String jsonStr = jsonObj.toString();
    final HttpResponse response = postEntity(jsonStr, uri, getToken(), true);
    assertResponse(response, 201, "failed to create term");
  }

  private void unlock() throws IOException {
    unlock(TAXONOMY_UUID);
  }

  private void unlock(String taxonomyUuid) throws IOException {
    String uri = context.getBaseUrl() + API_TAXONOMY_PATH + "/" + TAXONOMY_UUID + "/lock";
    deleteResource(uri, getToken(), "force", true);
  }

  private void lock() throws IOException, URISyntaxException {
    lock(TAXONOMY_UUID);
  }

  private void lock(String taxonomyUuid) throws IOException, URISyntaxException {
    String uri = context.getBaseUrl() + API_TAXONOMY_PATH + "/" + taxonomyUuid + "/lock";
    final HttpPost request = new HttpPost(new URI(uri));
    execute(request, true, getToken());
  }
}
