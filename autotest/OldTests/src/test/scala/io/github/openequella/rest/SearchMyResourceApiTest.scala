package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.testng.Assert._
import org.testng.annotations.Test

import scala.jdk.CollectionConverters._

class SearchMyResourceApiTest extends AbstractRestApiTest {
  private val MY_RESOURCES_ENDPOINT = getTestConfig.getInstitutionUrl + "api/search/myresources"

  @Test(description = "Verify the myresource search types exist with correct counts and links")
  def testSearchTypes(): Unit = {
    val nodesById = fetchMyResources()

    assertEquals(nodesById.size, 6)

    // Validate specific top-level nodes from the response
    assertNode(nodesById, "published", "Published", 47)
    assertNode(nodesById, "draft", "Drafts", 6)
    assertNode(nodesById, "scrapbook", "Scrapbook", 9)
    assertNode(nodesById, "archived", "Archive", 3)
    assertNode(nodesById, "all", "All resources", 74)

    // Validate modqueue specifically to check for subSearches
    val modQueue = nodesById.getOrElse(
      "modqueue",
      throw new AssertionError("Moderation queue node not found")
    )

    assertEquals(modQueue.get("name").asText(), "Moderation queue")
    assertEquals(modQueue.get("count").asInt(), 9)
    assertTrue(modQueue.has("subSearches"), "Moderation queue must have 'subSearches'")
  }

  @Test(description = "Verify the Moderation Queue sub-search structure")
  def testModerationQueueHierarchy(): Unit = {
    val nodesById = fetchMyResources()

    val modQueue = nodesById.getOrElse(
      "modqueue",
      throw new AssertionError("Moderation queue node not found")
    )

    val subSearches = modQueue.get("subSearches")
    assertTrue(subSearches.isArray, "subSearches should be an array")
    assertEquals(subSearches.size(), 3, "Should have exactly 3 sub-search types")

    val subSearchesById = buildMapById(subSearches)

    assertSubSearchNode(subSearchesById, "moderating", "In moderation", 9)
    assertSubSearchNode(subSearchesById, "review", "Under review", 0)
    assertSubSearchNode(subSearchesById, "rejected", "Rejected", 0)
  }

  /** Build a lookup map of JSON nodes keyed by their id field from a JSON array node.
    *
    * @param arrayNode
    *   JSON array containing search type objects
    * @return
    *   map from id to the corresponding JSON node
    */
  private def buildMapById(arrayNode: JsonNode): Map[String, JsonNode] = {
    arrayNode
      .elements()
      .asScala
      .filter(_.has("id"))
      .map(node => node.get("id").asText() -> node)
      .toMap
  }

  /** Asserts that a top-level search type node exists and has the expected properties.
    *
    * @param nodesById
    *   lookup map of nodes keyed by id
    * @param id
    *   expected id of the node
    * @param expectedName
    *   expected display name for the node
    */
  private def assertNode(
      nodesById: Map[String, JsonNode],
      id: String,
      expectedName: String,
      expectedCount: Int
  ): Unit = {
    val node = nodesById.getOrElse(id, throw new AssertionError(s"Node with id '$id' not found"))

    assertEquals(node.get("name").asText(), expectedName)
    assertEquals(node.get("count").asInt(), expectedCount)
    assertTrue(node.has("links"))
  }

  /** Asserts that a sub-search node exists and has the expected properties.
    *
    * @param subSearchesById
    *   lookup map of sub-search nodes keyed by id
    * @param id
    *   expected sub-search id
    * @param expectedName
    *   expected display name for the sub-search
    */
  private def assertSubSearchNode(
      subSearchesById: Map[String, JsonNode],
      id: String,
      expectedName: String,
      expectedCount: Int
  ): Unit = {
    val node = subSearchesById.getOrElse(
      id,
      throw new AssertionError(s"Sub-search node with id '$id' not found")
    )

    assertEquals(node.get("name").asText(), expectedName)
    assertEquals(node.get("count").asInt(), expectedCount)
  }

  /** Execute a GET request to the My Resources endpoint and return a map of nodes keyed by ID.
    *
    * @return
    *   map from id to the corresponding JSON node
    */
  private def fetchMyResources(): Map[String, JsonNode] = {
    val method   = new GetMethod(MY_RESOURCES_ENDPOINT)
    val respCode = makeClientRequest(method)
    assertEquals(respCode, HttpStatus.SC_OK)
    val result = mapper.readTree(method.getResponseBodyAsStream)

    assertNotNull(result)
    assertTrue(result.isArray)
    buildMapById(result)
  }
}
