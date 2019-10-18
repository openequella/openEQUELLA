package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.tle.annotation.Nullable;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
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
  private static final String API_TERM_PATH_PART = "term";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testGetTerm() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri =
        new URI(
            PathUtils.urlPath(
                context.getBaseUrl(),
                API_TAXONOMY_PATH,
                TAXONOMY_UUID,
                API_TERM_PATH_PART,
                TERM_1_UUID));
    final JsonNode result = getEntity(uri.toString(), token);
    assertEquals(result.get("uuid").asText(), TERM_1_UUID);
  }

  @Test
  public void testInsertAndDeleteTerm() throws IOException, URISyntaxException {
    unlock(TAXONOMY_UUID);
    lock(TAXONOMY_UUID);

    final String termValue = "TEST TERM NODE";
    final String termUrl = createTerm(TAXONOMY_UUID, null, termValue, null, 0);

    JsonNode termNode = getEntity(termUrl, getToken());
    assertEquals(termValue, termNode.get("term").asText());
    HttpResponse response = deleteResource(termUrl, getToken());
    assertResponse(response, 200, "failed to delete term");

    unlock(TAXONOMY_UUID);
  }

  @Test
  public void testMoveTerm() throws IOException, URISyntaxException {
    unlock(TAXONOMY_UUID);
    lock(TAXONOMY_UUID);

    final String termValue = "TEST TERM NODE";
    final String termUrl = createTerm(TAXONOMY_UUID, null, termValue, null, 0);

    ObjectNode termNode = (ObjectNode) getEntity(termUrl, getToken());
    assertEquals(termValue, termNode.get("term").asText());

    termNode.put("parentUuid", TERM_1_UUID);
    String putUrl =
        PathUtils.urlPath(
            context.getBaseUrl(),
            API_TAXONOMY_PATH,
            TAXONOMY_UUID,
            API_TERM_PATH_PART,
            termNode.get("uuid").getTextValue());
    HttpResponse putRequest = getPut(putUrl, termNode, getToken());
    assertResponse(putRequest, 200, "failed to update term");

    HttpResponse response = deleteResource(termUrl, getToken());
    assertResponse(response, 200, "failed to delete term");

    unlock(TAXONOMY_UUID);
  }

  @Test
  public void testSearchTerms() throws IOException {
    JsonNode entity =
        getEntity(
            PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, TAXONOMY_UUID, "search"),
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
    lock(taxonomyUuid);

    final List<String> rootTerms = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      final String termName = randomString(8);
      rootTerms.add(termName);
      createTerm(taxonomyUuid, null, termName);
    }

    // These are the orders we will expect
    // Sort using same mechanism as server
    final Collator collator = Collator.getInstance(Locale.getDefault());
    collator.setStrength(Collator.PRIMARY);
    rootTerms.sort(collator);

    // Sort root
    final HttpResponse sortRootsResponse = sortChildren(taxonomyUuid, null);
    assertResponse(sortRootsResponse, 200, "failed to sort root terms");

    final List<String> returnedSortedRootTerms = new ArrayList<>(10);
    final ArrayNode arrayNode = getChildren(taxonomyUuid, null);
    for (int i = 0; i < arrayNode.size(); i++) {
      final JsonNode node = arrayNode.get(i);
      final String termName = node.get("term").getTextValue();
      returnedSortedRootTerms.add(termName);
    }
    assertEquals(rootTerms, returnedSortedRootTerms);

    unlock(taxonomyUuid);

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
    lock(taxonomyUuid);

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
      createTerm(taxonomyUuid, termUuid, termName);

      for (int j = 0; j < childCount; j++) {
        final String subTermName = termName + "-" + randomString(8);
        if (isTestTerm) {
          testTermChildren.add(subTermName);
        }
        createTerm(taxonomyUuid, null, subTermName, termUuid, -1);
      }
    }

    // These are the orders we will expect
    // Sort using same mechanism as server
    final Collator collator = Collator.getInstance(Locale.getDefault());
    collator.setStrength(Collator.PRIMARY);
    rootTerms.sort(collator);
    testTermChildren.sort(collator);

    // Sort root
    final HttpResponse sortRootsResponse = sortChildren(taxonomyUuid, null);
    assertResponse(sortRootsResponse, 200, "failed to sort root terms");
    // Sort test terms children
    final HttpResponse sortTestTermChildrenResponse = sortChildren(taxonomyUuid, testTermPath);
    assertResponse(sortTestTermChildrenResponse, 200, "failed to sort test term children");

    // Check root sorted
    final List<String> returnedSortedRootTerms = new ArrayList<>(10);
    final ArrayNode arrayNode = getChildren(taxonomyUuid, null);
    for (int i = 0; i < arrayNode.size(); i++) {
      final JsonNode node = arrayNode.get(i);
      final String termName = node.get("term").getTextValue();
      returnedSortedRootTerms.add(termName);
    }
    assertEquals(rootTerms, returnedSortedRootTerms);

    // Check test terms children sorted
    final List<String> returnedSortedChildTerms = new ArrayList<>(10);
    final ArrayNode testTermSortedChildren = getChildren(taxonomyUuid, testTermPath);

    for (int i = 0; i < testTermSortedChildren.size(); i++) {
      final JsonNode node = testTermSortedChildren.get(i);
      final String termName = node.get("term").getTextValue();
      returnedSortedChildTerms.add(termName);
    }
    assertEquals(testTermChildren, returnedSortedChildTerms);

    unlock(taxonomyUuid);

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

  private String createTerm(String taxonomyUuid, @Nullable String termUuid, String term)
      throws IOException {
    return createTerm(taxonomyUuid, termUuid, term, null, -1);
  }

  private String createTerm(
      String taxonomyUuid,
      @Nullable String termUuid,
      String term,
      @Nullable String parentTermUuid,
      int index)
      throws IOException {
    final String uri =
        PathUtils.urlPath(
            context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, API_TERM_PATH_PART);
    final ObjectNode jsonObj = mapper.createObjectNode();
    jsonObj.put("uuid", termUuid);
    jsonObj.put("term", term);
    jsonObj.put("parentUuid", parentTermUuid);
    if (index >= 0) {
      jsonObj.put("index", index);
    }
    final String jsonStr = jsonObj.toString();
    final HttpResponse response = postEntity(jsonStr, uri, getToken(), true);
    assertResponse(response, 201, "failed to create term");
    return response.getFirstHeader("Location").getValue();
  }

  private ArrayNode getChildren(String taxonomyUuid, @Nullable String path) throws IOException {
    Object[] varargs;
    if (path != null) {
      varargs = new Object[] {"path", path};
    } else {
      varargs = new Object[0];
    }
    return (ArrayNode)
        getEntity(
            PathUtils.urlPath(
                context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, API_TERM_PATH_PART),
            getToken(),
            varargs);
  }

  private HttpResponse sortChildren(String taxonomyUuid, @Nullable String path) throws IOException {
    Object[] varargs;
    if (path != null) {
      varargs = new Object[] {"path", path};
    } else {
      varargs = new Object[0];
    }
    return postEntity(
        null,
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "sortchildren"),
        getToken(),
        true,
        varargs);
  }

  private void unlock(String taxonomyUuid) throws IOException {
    String uri = PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "lock");
    deleteResource(uri, getToken(), "force", true);
  }

  private void lock(String taxonomyUuid) throws IOException, URISyntaxException {
    String uri = PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "lock");
    final HttpPost request = new HttpPost(new URI(uri));
    execute(request, true, getToken());
  }
}
