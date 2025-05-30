package io.github.openequella.rest;

import static io.github.openequella.rest.JsonNodeHelper.getNode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class BrowseHierarchy2ApiTest extends AbstractRestApiTest {

  private static final Logger log = LoggerFactory.getLogger(BrowseHierarchy2ApiTest.class);
  private final String BROWSE_HIERARCHY_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/browsehierarchy2";
  private final String CLIENT_API_HIERARCHY_UUID = "43e60e9a-a3ed-497d-b79d-386fed23675c";
  // Topic name: A James
  private final String JAMES_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==";
  // Topic name: D, David
  private final String DAVID_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:RCwgRGF2aWQ=";
  // Topic name: Hobart
  private final String HOBART_HIERARCHY_UUID =
      "46249813-019d-4d14-b772-2a8ca0120c99:SG9iYXJ0,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==";
  private final String PARENT_HIERARCHY_UUID = "6135b550-ce1c-43c2-b34c-0a3cf793759d";
  private final String INVALID_HIERARCHY_UUID = "invalidUuid:123,456:!@";

  private final String INVALID_UUID_MESSAGE =
      "Failed to parse the compound UUID "
          + INVALID_HIERARCHY_UUID
          + ": Illegal base64 character 21";

  @Test(description = "Get all root hierarchies")
  public void browseRootHierarchies() throws IOException {
    JsonNode hierarchies = browseHierarchy(null, 200);

    // Should be able to get all top level hierarchies
    assertEquals(hierarchies.size(), 8);
    // Should be able to get all matched Items
    assertEquals(
        getTopic(hierarchies, CLIENT_API_HIERARCHY_UUID).get("matchingItemCount").asInt(), 12);
    // Should be able to get flag indicates if it has sub topics.
    assertTrue(getTopic(hierarchies, PARENT_HIERARCHY_UUID).get("hasSubTopic").asBoolean());
  }

  @Test(description = "Get all root hierarchies with low privilege user")
  public void browseRootHierarchiesWithLowPrivilegeUser() throws IOException {
    loginAsLowPrivilegeUser();
    JsonNode hierarchies = browseHierarchy(null, 200);
    // Should get an empty result.
    assertEquals(hierarchies.size(), 0);
    login();
  }

  @Test(description = "Get all sub hierarchies of a hierarchy")
  public void browseSubHierarchies() throws IOException {
    JsonNode hierarchies = browseHierarchy(PARENT_HIERARCHY_UUID, 200);
    assertEquals(hierarchies.size(), 3);
  }

  @Test(description = "Get all sub hierarchies of a hierarchy with low privilege user")
  public void browseSubHierarchiesWithLowPrivilegeUser() throws IOException {
    loginAsLowPrivilegeUser();
    JsonNode result = browseHierarchy(CLIENT_API_HIERARCHY_UUID, 403);
    assertEquals(
        getErrorMessage(result), "Permission denied to access topic " + CLIENT_API_HIERARCHY_UUID);
    login();
  }

  @Test(description = "Get sub hierarchies for hierarchy which has no sub hierarchies")
  public void browseEmptySubHierarchy() throws IOException {
    JsonNode hierarchies = browseHierarchy(CLIENT_API_HIERARCHY_UUID, 200);
    assertEquals(hierarchies.size(), 0);
  }

  @Test(description = "Get hierarchy topic with invalid compound UUID")
  public void browseSubHierarchiesWithInvalidUuid() throws IOException {
    JsonNode result = browseHierarchy(INVALID_HIERARCHY_UUID, 500);
    assertEquals(getErrorMessage(result), INVALID_UUID_MESSAGE);
  }

  @Test(description = "Get a hierarchy topic details")
  public void getHierarchyDetails() throws IOException {
    final String uuid = "bb9d5fbd-07e9-4e61-8eb6-e0c06ae39dfc";
    JsonNode results = browseHierarchyDetails(uuid, 200);

    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), uuid);
    // Should be able to get all parents info
    assertEquals(results.get("parents").size(), 2);
    // Should be able to get all children info
    assertEquals(results.get("children").size(), 1);
    // Should be able to get all key resources
    assertEquals(results.get("keyResources").size(), 2);
  }

  @Test(description = "Get a non-existent hierarchy topic")
  public void getNonExistentHierarchyDetails() throws IOException {
    final String uuid = "d0d58dca-6715-4496-b8c2-4aa970bb317c";
    browseHierarchyDetails(uuid, 404);
  }

  @Test(description = "Get hierarchy topic with invalid compound UUID")
  public void getHierarchyDetailsWithInvalidCompoundUuid() throws IOException {
    JsonNode result = browseHierarchyDetails(INVALID_HIERARCHY_UUID, 500);
    assertEquals(getErrorMessage(result), INVALID_UUID_MESSAGE);
  }

  @Test(description = "Get a virtual hierarchy topic")
  public void getVirtualHierarchyDetails() throws IOException {
    JsonNode results = browseHierarchyDetails(JAMES_HIERARCHY_UUID, 200);

    // Should be able to get the request hierarchy topic
    assertEquals(getCompoundUuid(results), JAMES_HIERARCHY_UUID);
  }

  @Test(description = "Get hierarchy key resource item version")
  public void getItemVersion() throws IOException {
    final String BOOK_A_V2_UUID = "cadcd296-a4d7-4024-bb5d-6c7507e6872a";
    final String BOOK_B_UUID = "e35390cf-7c45-4f71-bb94-e6ccc1f09394";

    JsonNode keyResources = browseHierarchyDetails(JAMES_HIERARCHY_UUID, 200).get("keyResources");

    // Key resource should point to the correct version
    assertFalse(getKeyResource(keyResources, BOOK_A_V2_UUID, 2).get("isLatest").asBoolean());
    assertEquals(
        getKeyResource(keyResources, BOOK_A_V2_UUID, 2).get("item").get("version").asInt(), 2);
    // BOOK_B has 2 versions and as an "Always latest key resource" it should point to the latest
    // version 2.
    assertTrue(getKeyResource(keyResources, BOOK_B_UUID, 2).get("isLatest").asBoolean());
    assertEquals(
        getKeyResource(keyResources, BOOK_B_UUID, 2).get("item").get("version").asInt(), 2);
  }

  @Test(description = "Get a virtual hierarchy topic which name contains comma")
  public void getVirtualHierarchyDetailsWithComma() throws IOException {
    JsonNode results = browseHierarchyDetails(DAVID_HIERARCHY_UUID, 200);
    // Should be able to get the request hierarchy topic
    assertEquals(getCompoundUuid(results), DAVID_HIERARCHY_UUID);
  }

  @Test(description = "Get a sub virtual hierarchy")
  public void getSubVirtualDetailsHierarchy() throws IOException {
    JsonNode results = browseHierarchyDetails(HOBART_HIERARCHY_UUID, 200);

    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), HOBART_HIERARCHY_UUID);
    // Should be able to get all parents info
    assertEquals(results.get("parents").size(), 1);
    // Should be able to get all children info
    assertEquals(results.get("children").size(), 1);
    // Should be able to get all key resources
    assertEquals(results.get("keyResources").size(), 2);
  }

  @Test(description = "Get hierarchy IDs with key resource")
  public void getHierarchyIdsWithKeyResource() throws IOException {
    final String ITEM_UUID = "7e633e1d-e343-4e51-babc-403265c7b7c4";
    final GetMethod method =
        new GetMethod(BROWSE_HIERARCHY_API_ENDPOINT + "/key-resource/" + ITEM_UUID + "/1");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    JsonNode result = mapper.readTree(method.getResponseBodyAsStream());
    assertEquals(result.get(0).asText(), "43e60e9a-a3ed-497d-b79d-386fed23675c");
  }

  private JsonNode request(String urlPrefix, String compoundUuid, int httpCode) throws IOException {
    String url =
        Optional.ofNullable(compoundUuid)
            .map(uuid -> BROWSE_HIERARCHY_API_ENDPOINT + urlPrefix + "/" + uuid)
            .orElse(BROWSE_HIERARCHY_API_ENDPOINT);

    final GetMethod method = new GetMethod(url);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, httpCode);
    return mapper.readTree(method.getResponseBodyAsStream());
  }

  private JsonNode browseHierarchy(String compoundUuid, int httpCode) throws IOException {
    return request("", compoundUuid, httpCode);
  }

  private JsonNode browseHierarchyDetails(String compoundUuid, int httpCode) throws IOException {
    return request("/details", compoundUuid, httpCode);
  }

  // get specific topic from endpoint result
  private JsonNode getTopic(JsonNode result, String topicUuid) {
    return getNode((ArrayNode) result, "compoundUuid", topicUuid);
  }

  private JsonNode getKeyResource(JsonNode result, String itemUuid, int version) {
    return getNode(
        (ArrayNode) result,
        n ->
            n.get("item").findParents("uuid").stream()
                .filter(item -> item.get("version").asInt() == version)
                .findFirst()
                .map(item -> item.get("uuid"))
                .orElse(null),
        itemUuid);
  }

  private String getCompoundUuid(JsonNode result) {
    return result.get("summary").get("compoundUuid").asText();
  }

  private String getErrorMessage(JsonNode result) {
    return result.get("errors").get(0).get("message").asText();
  }
}
