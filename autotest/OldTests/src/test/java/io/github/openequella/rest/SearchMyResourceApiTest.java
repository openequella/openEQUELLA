package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.testng.annotations.Test;

public class SearchMyResourceApiTest extends AbstractRestApiTest {
  private final String MY_RESOURCES_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/search/myresources";

  @Test(description = "Verify the root level search types exist with correct counts and links")
  public void testRootSearchTypes() throws IOException {
    JsonNode result = makeRequest(MY_RESOURCES_ENDPOINT);

    assertNotNull(result);
    assertTrue(result.isArray());
    assertEquals(result.size(), 6);

    Map<String, JsonNode> nodesById = buildMapById(result);

    // Validate specific top-level nodes from the response
    validateNode(nodesById, "published", "Published");
    validateNode(nodesById, "draft", "Drafts");
    validateNode(nodesById, "scrapbook", "Scrapbook");
    validateNode(nodesById, "archived", "Archive");
    validateNode(nodesById, "all", "All resources");

    // Validate modqueue specifically to check for subSearches
    JsonNode modQueue =
        Optional.ofNullable(nodesById.get("modqueue"))
            .orElseThrow(() -> new AssertionError("Moderation queue node not found"));

    assertEquals(modQueue.get("name").asText(), "Moderation queue");
    assertTrue(modQueue.get("count").asInt() >= 0);
    assertTrue(modQueue.has("subSearches"), "Moderation queue must have 'subSearches'");
  }

  @Test(description = "Verify the Moderation Queue sub-search structure")
  public void testModerationQueueHierarchy() throws IOException {
    JsonNode root = makeRequest(MY_RESOURCES_ENDPOINT);

    Map<String, JsonNode> nodesById = buildMapById(root);

    JsonNode modQueue =
        Optional.ofNullable(nodesById.get("modqueue"))
            .orElseThrow(() -> new AssertionError("Moderation queue node not found"));

    JsonNode subSearches = modQueue.get("subSearches");
    assertTrue(subSearches.isArray(), "subSearches should be an array");
    assertEquals(subSearches.size(), 3, "Should have exactly 3 sub-search types");

    Map<String, JsonNode> subSearchesById = buildMapById(subSearches);

    validateSubSearchNode(subSearchesById, "modqueue_moderating", "In moderation");
    validateSubSearchNode(subSearchesById, "modqueue_review", "Under review");
    validateSubSearchNode(subSearchesById, "modqueue_rejected", "Rejected");
  }

  /**
   * Build a lookup map of JSON nodes keyed by their id field from a JSON array node.
   *
   * @param arrayNode JSON array containing search type objects
   * @return map from id to the corresponding JSON node
   */
  private Map<String, JsonNode> buildMapById(JsonNode arrayNode) {
    // Turn the array into a stream, keep only nodes with an "id", and map id -> node.
    return StreamSupport.stream(arrayNode.spliterator(), false)
        .filter(node -> node.has("id"))
        .collect(Collectors.toMap(node -> node.get("id").asText(), node -> node));
  }

  /**
   * Validate a top-level search type node against an expected id/name pair and required fields.
   *
   * @param nodesById lookup map of nodes keyed by id
   * @param id expected id of the node
   * @param expectedName expected display name for the node
   */
  private void validateNode(Map<String, JsonNode> nodesById, String id, String expectedName) {
    JsonNode node =
        Optional.ofNullable(nodesById.get(id))
            .orElseThrow(() -> new AssertionError("Node with id '" + id + "' not found"));

    assertEquals(node.get("name").asText(), expectedName);
    assertTrue(node.has("count"));
    assertTrue(node.get("count").isInt());
    assertTrue(node.has("links"));
  }

  /**
   * Validate a moderation sub-search node against an expected id/name pair and required fields.
   *
   * @param subSearchesById lookup map of sub-search nodes keyed by id
   * @param id expected sub-search id
   * @param expectedName expected display name for the sub-search
   */
  private void validateSubSearchNode(
      Map<String, JsonNode> subSearchesById, String id, String expectedName) {
    JsonNode node =
        Optional.ofNullable(subSearchesById.get(id))
            .orElseThrow(
                () -> new AssertionError("Sub-search node with id '" + id + "' not found"));

    assertEquals(node.get("name").asText(), expectedName, "Sub-search name mismatch");
    assertTrue(node.has("count"), "Sub-search count missing");
  }

  /**
   * Execute a GET request to the supplied URL and assert that a 200 OK is returned.
   *
   * @param url endpoint to call
   * @return parsed JSON body of the response
   * @throws IOException if the HTTP call or JSON parsing fails
   */
  private JsonNode makeRequest(String url) throws IOException {
    HttpMethod method = new GetMethod(url);
    int respCode = makeClientRequest(method);
    Assert.assertEquals(HttpStatus.SC_OK, respCode);
    return mapper.readTree(method.getResponseBodyAsStream());
  }
}
