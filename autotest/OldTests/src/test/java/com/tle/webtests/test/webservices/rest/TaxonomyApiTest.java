package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.testng.annotations.Test;

public class TaxonomyApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "TaxonomyApiTestClient";
  private static final String TAXONOMY_UUID = "a8475ae1-0382-a258-71c3-673e4597c3d2";
  private static final String TERM_1_UUID = "abbd2610-1c3e-489a-a107-1c16fa22b0a0";
  private static final String API_TAXONOMY_PATH = "api/taxonomy";
  private static final String API_TERM_PATH_PART = "term";
  private static final String API_TERM_DATA_PATH_PART = "data";
  private static final String ROOT_NODE_NAME = "root";

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

    moveNode(termNode, TERM_1_UUID, TAXONOMY_UUID, -1);

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

  private void alphaSort(List<String> terms) {
    // Sort using same mechanism as server
    final Collator collator = Collator.getInstance(Locale.getDefault());
    collator.setStrength(Collator.PRIMARY);
    terms.sort(collator);
  }

  @Test
  public void testSortRootTerms() throws Exception {
    final String taxonomyUuid = UUID.randomUUID().toString();
    // Create a new taxonomy
    createTaxononmy(taxonomyUuid, "Sort Roots Test Taxonomy");

    // Lock
    lock(taxonomyUuid);

    final List<String> rootTerms = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      final String termName = randomString(8);
      rootTerms.add(termName);
      createTerm(taxonomyUuid, null, termName);
    }

    // These are the orders we will expect
    alphaSort(rootTerms);

    // Sort root
    final HttpResponse sortRootsResponse = sortChildren(taxonomyUuid, null);
    assertResponse(sortRootsResponse, 200, "failed to sort root terms");

    final ArrayNode arrayNode = getChildren(taxonomyUuid, null);
    final List<String> returnedSortedRootTerms = getNodeNames(arrayNode);
    assertEquals(rootTerms, returnedSortedRootTerms);

    unlock(taxonomyUuid);

    // Delete the taxonomy
    final String taxonomyUri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid);
    deleteResource(taxonomyUri, getToken());
  }

  @Test
  public void testSortChildTerms() throws Exception {

    final String taxonomyUuid = UUID.randomUUID().toString();
    // Create a new taxonomy
    createTaxononmy(taxonomyUuid, "Sort Children Test Taxonomy");

    // Lock
    lock(taxonomyUuid);

    final List<String> firstLevelTerms = new ArrayList<>();
    final List<String> secondLevelTerms = new ArrayList<>();

    final String testTermUuid = UUID.randomUUID().toString();
    final int testTermIndex = 5;
    String testTermPath = null;

    // Create root node
    final String rootUuid = UUID.randomUUID().toString();
    createTerm(taxonomyUuid, rootUuid, ROOT_NODE_NAME);

    // Create first-level nodes
    for (int i = 0; i < 10; i++) {
      final String termName = randomString(8);
      final boolean isTestTerm = (i == testTermIndex);
      final int childCount = (isTestTerm ? 100 : 8);
      final String termUuid = (isTestTerm ? testTermUuid : UUID.randomUUID().toString());
      if (isTestTerm) {
        testTermPath = termName;
      }
      firstLevelTerms.add(termName);
      createTerm(taxonomyUuid, termUuid, termName, rootUuid, -1);

      // Create second-level nodes
      for (int j = 0; j < childCount; j++) {
        final String subTermName = termName + "-" + randomString(8);
        if (isTestTerm) {
          secondLevelTerms.add(subTermName);
        }
        createTerm(taxonomyUuid, null, subTermName, termUuid, -1);
      }
    }

    // These are the orders we will expect
    alphaSort(firstLevelTerms);
    alphaSort(secondLevelTerms);

    final String rootNodePath = ROOT_NODE_NAME;
    final String firstLevelNodePath = ROOT_NODE_NAME + "\\" + testTermPath;
    // Sort first-level nodes
    final HttpResponse sortFirstLevelResponse = sortChildren(taxonomyUuid, rootNodePath);
    assertResponse(sortFirstLevelResponse, 200, "failed to sort first-level terms");
    // Sort second-level nodes
    final HttpResponse sortSecondLevelResponse = sortChildren(taxonomyUuid, firstLevelNodePath);
    assertResponse(sortSecondLevelResponse, 200, "failed to sort second-level terms");

    // Check first-level sorted nodes
    final ArrayNode firstLevelNodes = getChildren(taxonomyUuid, rootNodePath);
    final List<String> sortedFirstLevelTerms = getNodeNames(firstLevelNodes);
    assertEquals(firstLevelTerms, sortedFirstLevelTerms);

    // Check second-level sorted nodes
    final ArrayNode secondLevelNodes = getChildren(taxonomyUuid, firstLevelNodePath);
    final List<String> sortedSecondLevelTerms = getNodeNames(secondLevelNodes);
    assertEquals(secondLevelTerms, sortedSecondLevelTerms);

    // Check if the first-level sorting breaks the indexes of second-level nodes
    final JsonNode node = firstLevelNodes.get(0);
    String nodePath = node.get("fullTerm").asText();
    String nodeUuid = node.get("uuid").asText();

    // Sort before any node movements
    sortChildren(taxonomyUuid, nodePath);
    ArrayNode childNodes = getChildren(taxonomyUuid, nodePath);
    final List<String> childNodeNames = getNodeNames(childNodes);

    // Move a child node
    JsonNode lastChildNode = childNodes.get(childNodes.size() - 1);
    moveNode((ObjectNode) lastChildNode, nodeUuid, taxonomyUuid, 0);

    // Sort again
    sortChildren(taxonomyUuid, nodePath);
    childNodes = getChildren(taxonomyUuid, nodePath);
    final List<String> sortedChildNodeNames = getNodeNames(childNodes);

    assertEquals(childNodeNames, sortedChildNodeNames);

    unlock(taxonomyUuid);

    // Delete the taxonomy
    final String taxonomyUri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid);
    deleteResource(taxonomyUri, getToken());
  }

  /**
   * Creates a 3^4 (81) node taxonomy (ie. 4 levels with each non-leaf node having 3 children)
   *
   * @throws Exception
   */
  @Test
  public void testSortWholeTaxonomy() throws Exception {

    final int levelCount = 3;
    final int nodesPerLevel = 3;
    final String taxonomyUuid = UUID.randomUUID().toString();
    final List<TermTestData> levelTermData = new ArrayList<>();
    final List<String> rootTermNames = new ArrayList<>();

    createNestedTermsTaxonomy(
        taxonomyUuid, rootTermNames, levelTermData, levelCount, nodesPerLevel);

    // Call sort on whole taxonomy
    final HttpResponse sortResponse =
        postEntity(
            null,
            PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "sort"),
            getToken(),
            true);
    assertResponse(sortResponse, 200, "failed to sort taxonomy");

    // Validate the ordering against what we have sorted ourselves
    validateTaxonomyOrdering(taxonomyUuid, rootTermNames, levelTermData, nodesPerLevel);

    unlock(taxonomyUuid);

    // Delete the taxonomy
    final String taxonomyUri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid);
    deleteResource(taxonomyUri, getToken());
  }

  private void createNestedTermsTaxonomy(
      String taxonomyUuid,
      List<String> rootTermNames,
      List<TermTestData> levelTermData,
      int levelCount,
      int nodesPerLevel)
      throws Exception {
    // Create a new taxonomy
    createTaxononmy(taxonomyUuid, "Sort Whole Taxonomy Test Taxonomy");

    // Lock
    lock(taxonomyUuid);

    // Various terms at different levels
    for (int i = 0; i < levelCount; i++) {
      levelTermData.add(new TermTestData((int) (Math.random() * nodesPerLevel)));
    }

    final List<Pair<String, String>> level0Terms =
        createChildTerms(taxonomyUuid, null, nodesPerLevel, null);
    rootTermNames.addAll(Lists.transform(level0Terms, Pair::getSecond));

    // You could do this recursively, but we want to keep track of certain nodes
    for (int level0Index = 0; level0Index < level0Terms.size(); level0Index++) {
      final Pair<String, String> level0Term = level0Terms.get(level0Index);

      final List<Pair<String, String>> level1Terms =
          createChildTerms(
              taxonomyUuid, level0Term.getFirst(), nodesPerLevel, level0Term.getSecond());
      for (int level1Index = 0; level1Index < level1Terms.size(); level1Index++) {
        final Pair<String, String> level1Term = level1Terms.get(level1Index);

        final List<Pair<String, String>> level2Terms =
            createChildTerms(
                taxonomyUuid, level1Term.getFirst(), nodesPerLevel, level1Term.getSecond());
        for (int level2Index = 0; level2Index < level2Terms.size(); level2Index++) {
          final Pair<String, String> level2Term = level2Terms.get(level2Index);

          final List<Pair<String, String>> level3Terms =
              createChildTerms(
                  taxonomyUuid, level2Term.getFirst(), nodesPerLevel, level2Term.getSecond());
          initTestData(
              levelTermData,
              2,
              level2Index,
              level0Term.getSecond() + "\\" + level1Term.getSecond(),
              level2Term,
              level3Terms);
        }
        initTestData(
            levelTermData, 1, level1Index, level0Term.getSecond(), level1Term, level2Terms);
      }
      initTestData(levelTermData, 0, level0Index, null, level0Term, level1Terms);
    }

    // We now know the sorted terms to expect at each level
    alphaSort(rootTermNames);
    for (TermTestData levelTermDatum : levelTermData) {
      alphaSort(levelTermDatum.childrenNames);
    }
  }

  private void validateTaxonomyOrdering(
      String taxonomyUuid,
      List<String> rootTermNames,
      List<TermTestData> levelTermData,
      int nodesPerLevel)
      throws Exception {
    // get the children at each level, and match them
    final List<String> returnedSortedRootTerms = new ArrayList<>(nodesPerLevel);
    final ArrayNode arrayNode = getChildren(taxonomyUuid, null);
    for (int i = 0; i < arrayNode.size(); i++) {
      final JsonNode node = arrayNode.get(i);
      final String termName = node.get("term").asText();
      returnedSortedRootTerms.add(termName);
    }
    assertEquals(rootTermNames, returnedSortedRootTerms);

    for (TermTestData levelTermDatum : levelTermData) {
      final List<String> returnedSortedChildTerms = new ArrayList<>(nodesPerLevel);
      final ArrayNode testTermSortedChildren = getChildren(taxonomyUuid, levelTermDatum.termPath);
      for (int j = 0; j < testTermSortedChildren.size(); j++) {
        final JsonNode node = testTermSortedChildren.get(j);
        final String termName = node.get("term").asText();
        returnedSortedChildTerms.add(termName);
      }
      assertEquals(levelTermDatum.childrenNames, returnedSortedChildTerms);
    }
  }

  private void initTestData(
      List<TermTestData> levelTermData,
      int level,
      int termIndex,
      String prefixPath,
      Pair<String, String> term,
      List<Pair<String, String>> children) {
    final TermTestData testData = levelTermData.get(level);
    if (termIndex == testData.termIndex) {
      testData.childrenNames = new ArrayList<>(Lists.transform(children, Pair::getSecond));
      testData.termUuid = term.getFirst();
      testData.termName = term.getSecond();
      testData.termPath =
          (prefixPath == null ? term.getSecond() : prefixPath + "\\" + term.getSecond());
    }
  }

  private static class TermTestData {
    public final int termIndex;
    public List<String> childrenNames = new ArrayList<>();
    public String termUuid = null;
    public String termName = null;
    public String termPath = null;

    public TermTestData(int termIndex) {
      this.termIndex = termIndex;
    }
  }

  /**
   * @return A list of term UUID+Names
   */
  private List<Pair<String, String>> createChildTerms(
      String taxonomyUuid, String parentTermUuid, int numTerms, String currentPrefix)
      throws Exception {
    final List<Pair<String, String>> result = new ArrayList<>();
    for (int i = 0; i < numTerms; i++) {
      final String random = randomString(8);
      final String termName = (currentPrefix == null ? random : currentPrefix + "-" + random);
      final String termUuid = UUID.randomUUID().toString();

      createTerm(taxonomyUuid, termUuid, termName, parentTermUuid, -1);
      result.add(new Pair<>(termUuid, termName));
    }
    return result;
  }

  @Test
  public void testAddDatum() throws IOException {
    final String taxUuid = UUID.randomUUID().toString();
    createTaxononmy(taxUuid, "Datum test");

    final String termUuid = UUID.randomUUID().toString();
    createTerm(taxUuid, termUuid, "TEST");

    final String TEST_KEY = "This is a key";
    final String TEST_VALUE = "This is a value";
    final String datumLoc = addDatum(taxUuid, termUuid, TEST_KEY, TEST_VALUE);

    final ObjectNode result = (ObjectNode) getEntity(datumLoc, getToken());
    assertEquals(result.get(TEST_KEY).asText(), TEST_VALUE);

    // Future tests:
    // Add another data
    // Call get all data
    // Check all keys and values
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

  private String addDatum(String taxonomyUuid, String termUuid, String key, String value)
      throws IOException {
    return addDatum(taxonomyUuid, termUuid, key, value, true);
  }

  private String addDatum(
      String taxonomyUuid, String termUuid, String key, String value, boolean create)
      throws IOException {
    final String uri =
        PathUtils.urlPath(
            context.getBaseUrl(),
            API_TAXONOMY_PATH,
            taxonomyUuid,
            API_TERM_PATH_PART,
            termUuid,
            API_TERM_DATA_PATH_PART,
            URLEncoder.encode(key, "utf-8").replace("+", "%20"),
            URLEncoder.encode(value, "utf-8").replace("+", "%20"));

    final HttpResponse response = putEntity(null, uri, getToken(), true);
    assertResponse(response, create ? 201 : 200, "failed to create term data");
    return response.getFirstHeader("Location").getValue();
  }

  private ArrayNode getChildren(String taxonomyUuid, @Nullable String path) throws IOException {
    final Object[] varargs;
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

  private List<String> getNodeNames(ArrayNode nodes) {
    final List<String> nodeNames = new ArrayList<>();
    for (int i = 0; i < nodes.size(); i++) {
      final JsonNode node = nodes.get(i);
      final String termName = node.get("term").asText();
      nodeNames.add(termName);
    }
    return nodeNames;
  }

  private void moveNode(ObjectNode node, String parentUuid, String taxonomyUuid, int index)
      throws IOException {
    node.put("parentUuid", parentUuid);
    if (index >= 0) {
      node.put("index", index);
    }
    String putUrl =
        PathUtils.urlPath(
            context.getBaseUrl(),
            API_TAXONOMY_PATH,
            taxonomyUuid,
            API_TERM_PATH_PART,
            node.get("uuid").asText());
    HttpResponse putRequest = getPut(putUrl, node, getToken());
    assertResponse(putRequest, 200, "failed to move term");
  }

  private HttpResponse sortChildren(String taxonomyUuid, @Nullable String path) throws IOException {
    final Object[] varargs;
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
    final String uri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "lock");
    deleteResource(uri, getToken(), "force", true);
  }

  private void lock(String taxonomyUuid) throws IOException, URISyntaxException {
    final String uri =
        PathUtils.urlPath(context.getBaseUrl(), API_TAXONOMY_PATH, taxonomyUuid, "lock");
    final HttpPost request = new HttpPost(new URI(uri));
    execute(request, true, getToken());
  }

  @Test
  public void testSetTermWithMultipleDataKeyValue() throws IOException {
    final String taxonomyTermUri =
        PathUtils.urlPath(
            context.getBaseUrl(),
            API_TAXONOMY_PATH,
            TAXONOMY_UUID,
            API_TERM_PATH_PART,
            TERM_1_UUID,
            API_TERM_DATA_PATH_PART);

    ObjectNode jsonObj = mapper.createObjectNode();
    jsonObj.put("data/key", "data/value");
    jsonObj.put("/datakey", "/datavalue");
    jsonObj.put("datakey/", "datavalue/");
    final String jsonStr = jsonObj.toString();
    final HttpResponse response = putEntity(jsonStr, taxonomyTermUri, getToken(), true);
    assertResponse(response, 201, "failed to create term");

    final JsonNode result = getEntity(taxonomyTermUri, getToken());
    assertEquals(result.asText(), jsonObj.asText());
  }

  @Test(description = "Set term with the key value already exists")
  public void testSetTermWithConflictDataKeyValue() throws IOException {
    final String taxonomyTermUri =
        PathUtils.urlPath(
            context.getBaseUrl(),
            API_TAXONOMY_PATH,
            TAXONOMY_UUID,
            API_TERM_PATH_PART,
            TERM_1_UUID,
            API_TERM_DATA_PATH_PART);
    final String dataKey = "data_key";

    // Clear all data from the target test term
    final String taxonomyTermDataUri = PathUtils.urlPath(taxonomyTermUri, dataKey);
    HttpResponse response = deleteResource(taxonomyTermDataUri, getToken());
    assertResponse(response, 200, "failed to delete term's data");
    final ObjectNode initialStateResponse = (ObjectNode) getEntity(taxonomyTermUri, getToken());
    assertNull(initialStateResponse.get(dataKey));

    // Add new key
    ObjectNode requestBodyObj = mapper.createObjectNode();
    requestBodyObj.put(dataKey, "data_value");
    final HttpResponse createdResponse =
        putEntity(requestBodyObj.toString(), taxonomyTermUri, getToken(), true);
    assertResponse(createdResponse, 201, "failed add data to term");
    final JsonNode getTaxonomytermResponse = getEntity(taxonomyTermUri, getToken());
    assertEquals(getTaxonomytermResponse.asText(), requestBodyObj.asText());

    final HttpResponse conflictResponse =
        putEntity(requestBodyObj.toString(), taxonomyTermUri, getToken(), true);
    assertResponse(
        conflictResponse, 409, "unexpected result when attempted to re-create existing term data");
  }
}
