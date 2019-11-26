package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.Lists;
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

    final List<String> rootTerms = new ArrayList<>();
    final List<String> testTermChildren = new ArrayList<>();

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
      int childCount = (isTestTerm ? 100 : 8);
      final String termUuid = (isTestTerm ? testTermUuid : UUID.randomUUID().toString());
      if (isTestTerm) {
        testTermPath = termName;
      }
      rootTerms.add(termName);
      createTerm(taxonomyUuid, termUuid, termName, rootUuid, -1);

      // Create second-level nodes
      for (int j = 0; j < childCount; j++) {
        final String subTermName = termName + "-" + randomString(8);
        if (isTestTerm) {
          testTermChildren.add(subTermName);
        }
        createTerm(taxonomyUuid, null, subTermName, termUuid, -1);
      }
    }

    // These are the orders we will expect
    alphaSort(rootTerms);
    alphaSort(testTermChildren);

    final String rootNodePath = ROOT_NODE_NAME;
    final String firstLevelNodePath = ROOT_NODE_NAME + "\\" + testTermPath;
    // Sort root
    final HttpResponse sortRootsResponse = sortChildren(taxonomyUuid, rootNodePath);
    assertResponse(sortRootsResponse, 200, "failed to sort root terms");
    // Sort test terms children
    final HttpResponse sortTestTermChildrenResponse =
        sortChildren(taxonomyUuid, firstLevelNodePath);
    assertResponse(sortTestTermChildrenResponse, 200, "failed to sort test term children");

    // Check root sorted
    final ArrayNode firstLevelNodes = getChildren(taxonomyUuid, ROOT_NODE_NAME);
    final List<String> returnedSortedRootTerms = getNodeNames(firstLevelNodes);
    assertEquals(rootTerms, returnedSortedRootTerms);

    // Check test terms children sorted
    final ArrayNode testTermSortedChildren = getChildren(taxonomyUuid, firstLevelNodePath);
    final List<String> returnedSortedChildTerms = getNodeNames(testTermSortedChildren);
    assertEquals(testTermChildren, returnedSortedChildTerms);

    // Check if the root sorting breaks the order of second-level nodes
    final JsonNode firstNode = firstLevelNodes.get(0);
    String firstNodePath = firstNode.get("fullTerm").asText();
    String firstNodeUuid = firstNode.get("uuid").asText();

    // Sort before any node movements
    sortChildren(taxonomyUuid, firstNodePath);
    ArrayNode firstNodeChildren = getChildren(taxonomyUuid, firstNodePath);
    final List<String> nodeNames = getNodeNames(firstNodeChildren);

    // Move a child node
    JsonNode lastChildTerm = firstNodeChildren.get(firstNodeChildren.size() - 1);
    moveNode((ObjectNode) lastChildTerm, firstNodeUuid, taxonomyUuid, 0);

    // Sort again
    sortChildren(taxonomyUuid, firstNodePath);
    firstNodeChildren = getChildren(taxonomyUuid, firstNodePath);
    final List<String> sortedNodeNames = getNodeNames(firstNodeChildren);

    assertEquals(nodeNames, sortedNodeNames);

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
    rootTermNames.addAll(Lists.transform(level0Terms, (t) -> t.getSecond()));

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
    for (int i = 0; i < levelTermData.size(); i++) {
      alphaSort(levelTermData.get(i).childrenNames);
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
      final String termName = node.get("term").getTextValue();
      returnedSortedRootTerms.add(termName);
    }
    assertEquals(rootTermNames, returnedSortedRootTerms);

    for (int i = 0; i < levelTermData.size(); i++) {
      final List<String> returnedSortedChildTerms = new ArrayList<>(nodesPerLevel);
      final ArrayNode testTermSortedChildren =
          getChildren(taxonomyUuid, levelTermData.get(i).termPath);
      for (int j = 0; j < testTermSortedChildren.size(); j++) {
        final JsonNode node = testTermSortedChildren.get(j);
        final String termName = node.get("term").getTextValue();
        returnedSortedChildTerms.add(termName);
      }
      assertEquals(levelTermData.get(i).childrenNames, returnedSortedChildTerms);
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
      testData.childrenNames = new ArrayList<>(Lists.transform(children, (t) -> t.getSecond()));
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

  /** @return A list of term UUID+Names */
  private List<Pair<String, String>> createChildTerms(
      String taxonomyUuid, String parentTermUuid, int numTerms, String currentPrefix)
      throws Exception {
    final List<Pair<String, String>> result = new ArrayList<>();
    for (int i = 0; i < numTerms; i++) {
      final String random = randomString(8);
      final String termName = (currentPrefix == null ? random : currentPrefix + "-" + random);
      final String termUuid = UUID.randomUUID().toString();

      createTerm(taxonomyUuid, termUuid, termName, parentTermUuid, -1);
      result.add(new Pair<String, String>(termUuid, termName));
    }
    return result;
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
      final String termName = node.get("term").getTextValue();
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
            node.get("uuid").getTextValue());
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
}
